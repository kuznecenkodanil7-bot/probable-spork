package ru.wqkcpf.moderationhelper.obs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Util;
import ru.wqkcpf.moderationhelper.ModerationHelperClient;
import ru.wqkcpf.moderationhelper.config.ModConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class ObsController implements WebSocket.Listener {
    private final ModConfig config;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final Map<String, CompletableFuture<Boolean>> pending = new ConcurrentHashMap<>();
    private final StringBuilder buffer = new StringBuilder();
    private final AtomicBoolean connecting = new AtomicBoolean(false);

    private volatile WebSocket socket;
    private volatile boolean identified;

    public ObsController(ModConfig config) {
        this.config = config;
    }

    public void startRecording() {
        sendRequest("StartRecord");
    }

    public void stopRecording() {
        sendRequest("StopRecord");
    }

    private void sendRequest(String requestType) {
        if (!config.obsEnabled) {
            ModerationHelperClient.message("§7OBS-интеграция выключена в конфиге.");
            return;
        }

        connectIfNeeded().thenAccept(ok -> {
            if (!ok || socket == null || !identified) {
                ModerationHelperClient.message("§cOBS недоступен. Проверь obs-websocket.");
                return;
            }

            String requestId = UUID.randomUUID().toString();
            JsonObject d = new JsonObject();
            d.addProperty("requestType", requestType);
            d.addProperty("requestId", requestId);
            d.add("requestData", new JsonObject());

            JsonObject payload = new JsonObject();
            payload.addProperty("op", 6);
            payload.add("d", d);

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            pending.put(requestId, future);
            socket.sendText(payload.toString(), true);

            future.orTimeout(5, java.util.concurrent.TimeUnit.SECONDS).exceptionally(ex -> {
                ModerationHelperClient.LOGGER.warn("OBS request {} timed out/failed", requestType, ex);
                return false;
            });
        });
    }

    private CompletableFuture<Boolean> connectIfNeeded() {
        if (socket != null && identified) {
            return CompletableFuture.completedFuture(true);
        }
        if (!connecting.compareAndSet(false, true)) {
            return CompletableFuture.supplyAsync(() -> {
                long start = System.currentTimeMillis();
                while (connecting.get() && System.currentTimeMillis() - start < 5000) {
                    sleep(50);
                }
                return socket != null && identified;
            });
        }

        URI uri = URI.create("ws://" + config.obsHost + ":" + config.obsPort);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        httpClient.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(3)).buildAsync(uri, this)
                .thenAccept(ws -> {
                    this.socket = ws;
                    long start = System.currentTimeMillis();
                    CompletableFuture.runAsync(() -> {
                        while (!identified && System.currentTimeMillis() - start < 5000) {
                            sleep(50);
                        }
                        connecting.set(false);
                        result.complete(identified);
                    }, Util.getIoWorkerExecutor());
                })
                .exceptionally(ex -> {
                    connecting.set(false);
                    socket = null;
                    identified = false;
                    ModerationHelperClient.LOGGER.warn("Could not connect to OBS websocket", ex);
                    ModerationHelperClient.message("§cНе удалось подключиться к OBS: " + ex.getMessage());
                    result.complete(false);
                    return null;
                });
        return result;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String message = buffer.toString();
            buffer.setLength(0);
            handleMessage(webSocket, message);
        }
        webSocket.request(1);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        identified = false;
        socket = null;
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        identified = false;
        socket = null;
        ModerationHelperClient.LOGGER.warn("OBS websocket error", error);
    }

    private void handleMessage(WebSocket webSocket, String message) {
        try {
            JsonObject root = JsonParser.parseString(message).getAsJsonObject();
            int op = root.get("op").getAsInt();
            JsonObject d = root.has("d") && root.get("d").isJsonObject() ? root.getAsJsonObject("d") : new JsonObject();

            if (op == 0) { // Hello
                sendIdentify(webSocket, d);
            } else if (op == 2) { // Identified
                identified = true;
                ModerationHelperClient.LOGGER.info("OBS websocket identified");
            } else if (op == 7) { // RequestResponse
                String requestId = getString(d, "requestId");
                boolean ok = false;
                if (d.has("requestStatus")) {
                    JsonObject status = d.getAsJsonObject("requestStatus");
                    ok = status.has("result") && status.get("result").getAsBoolean();
                    if (!ok) {
                        String code = status.has("code") ? status.get("code").getAsString() : "unknown";
                        String comment = status.has("comment") ? status.get("comment").getAsString() : "";
                        ModerationHelperClient.message("§cOBS ответил ошибкой: " + code + " " + comment);
                    }
                }
                CompletableFuture<Boolean> future = pending.remove(requestId);
                if (future != null) future.complete(ok);
            }
        } catch (Exception e) {
            ModerationHelperClient.LOGGER.warn("Failed to handle OBS message: {}", message, e);
        }
    }

    private void sendIdentify(WebSocket webSocket, JsonObject helloData) throws Exception {
        JsonObject identify = new JsonObject();
        identify.addProperty("rpcVersion", 1);

        if (helloData.has("authentication") && helloData.get("authentication").isJsonObject()) {
            JsonObject auth = helloData.getAsJsonObject("authentication");
            String challenge = getString(auth, "challenge");
            String salt = getString(auth, "salt");
            if (!config.obsPassword.isBlank()) {
                identify.addProperty("authentication", buildObsAuthentication(config.obsPassword, salt, challenge));
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("op", 1);
        root.add("d", identify);
        webSocket.sendText(root.toString(), true);
    }

    /** obs-websocket v5 authentication: base64(sha256(base64(sha256(password + salt)) + challenge)). */
    private static String buildObsAuthentication(String password, String salt, String challenge) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] secretHash = sha256.digest((password + salt).getBytes(StandardCharsets.UTF_8));
        String secret = Base64.getEncoder().encodeToString(secretHash);
        byte[] authHash = sha256.digest((secret + challenge).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(authHash);
    }

    private static String getString(JsonObject obj, String key) {
        JsonElement e = obj.get(key);
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
