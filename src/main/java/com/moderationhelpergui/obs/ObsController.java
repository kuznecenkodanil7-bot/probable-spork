package com.moderationhelpergui.obs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moderationhelpergui.ModerationHelperClient;
import com.moderationhelpergui.config.ModConfig;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ObsController {
    private final ModConfig config;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private WebSocket webSocket;
    private CompletableFuture<Void> readyFuture;
    private final AtomicBoolean recording = new AtomicBoolean(false);

    public ObsController(ModConfig config) {
        this.config = config;
    }

    public boolean isRecording() {
        return recording.get();
    }

    public void startRecording() {
        if (!config.obsEnabled) {
            ModerationHelperClient.notifyClient("OBS-интеграция выключена в конфиге.");
            return;
        }
        ensureConnected()
                .thenRun(() -> {
                    sendRequest("StartRecord");
                    recording.set(true);
                })
                .exceptionally(error -> {
                    notifyObsError("OBS недоступен: запись не запущена", error);
                    return null;
                });
    }

    public void stopRecording() {
        if (!config.obsEnabled) {
            ModerationHelperClient.notifyClient("OBS-интеграция выключена в конфиге.");
            return;
        }
        ensureConnected()
                .thenRun(() -> {
                    sendRequest("StopRecord");
                    recording.set(false);
                })
                .exceptionally(error -> {
                    notifyObsError("OBS недоступен: запись не остановлена", error);
                    recording.set(false);
                    return null;
                });
    }

    private CompletableFuture<Void> ensureConnected() {
        if (webSocket != null && readyFuture != null && !readyFuture.isCompletedExceptionally() && readyFuture.isDone()) {
            return CompletableFuture.completedFuture(null);
        }

        readyFuture = new CompletableFuture<>();
        URI uri = URI.create("ws://" + config.obsHost + ":" + config.obsPort);
        httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .buildAsync(uri, new ObsListener())
                .thenAccept(socket -> this.webSocket = socket)
                .exceptionally(error -> {
                    readyFuture.completeExceptionally(error);
                    return null;
                });
        return readyFuture;
    }

    private void sendRequest(String requestType) {
        if (webSocket == null) return;
        JsonObject d = new JsonObject();
        d.addProperty("requestType", requestType);
        d.addProperty("requestId", "mhg-" + requestType + "-" + UUID.randomUUID());

        JsonObject message = new JsonObject();
        message.addProperty("op", 6);
        message.add("d", d);
        webSocket.sendText(message.toString(), true);
    }

    private void sendIdentify(String authentication) {
        if (webSocket == null) return;

        JsonObject d = new JsonObject();
        d.addProperty("rpcVersion", 1);
        if (authentication != null && !authentication.isBlank()) {
            d.addProperty("authentication", authentication);
        }

        JsonObject message = new JsonObject();
        message.addProperty("op", 1);
        message.add("d", d);
        webSocket.sendText(message.toString(), true);
    }

    private String makeAuth(String password, String salt, String challenge) throws Exception {
        String secret = base64Sha256(password + salt);
        return base64Sha256(secret + challenge);
    }

    private String base64Sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private void notifyObsError(String message, Throwable error) {
        ModerationHelperClient.LOGGER.warn(message, error);
        MinecraftClient.getInstance().execute(() -> ModerationHelperClient.notifyClient(message));
    }

    private final class ObsListener implements WebSocket.Listener {
        private final StringBuilder partial = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (!last) return WebSocket.Listener.super.onText(webSocket, data, last);

            String raw = partial.toString();
            partial.setLength(0);

            try {
                JsonObject message = JsonParser.parseString(raw).getAsJsonObject();
                int op = message.get("op").getAsInt();

                if (op == 0) {
                    JsonObject d = message.getAsJsonObject("d");
                    JsonObject auth = d.has("authentication") ? d.getAsJsonObject("authentication") : null;
                    if (auth != null && config.obsPassword != null && !config.obsPassword.isBlank()) {
                        String salt = auth.get("salt").getAsString();
                        String challenge = auth.get("challenge").getAsString();
                        sendIdentify(makeAuth(config.obsPassword, salt, challenge));
                    } else {
                        sendIdentify(null);
                    }
                }

                if (op == 2) {
                    readyFuture.complete(null);
                    ModerationHelperClient.LOGGER.info("Connected to OBS websocket");
                }
            } catch (Exception e) {
                if (readyFuture != null && !readyFuture.isDone()) {
                    readyFuture.completeExceptionally(e);
                }
            }

            webSocket.request(1);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (readyFuture != null && !readyFuture.isDone()) readyFuture.completeExceptionally(error);
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}
