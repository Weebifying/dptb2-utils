package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();

    /**
     * TLS context pinned to DPTBot's own CA (bundled as a mod resource), used
     * for the wss:// connection to the bot so the traffic can't be MITM'd/spied
     * on by anything other than the bot's own certificate authority.
     */
    public static final SSLContext TRUSTED_CONTEXT;
    /**
     * Cache of "host:port" -> trusted key, so once the client has proven its
     * identity to a given DPTBot instance via the Mojang session-join handshake,
     * subsequent (re)connects can skip hitting the Mojang API again and avoid
     * potential ratelimiting.
     */
    public static final Map<String, String> TRUSTED_KEY = new HashMap<>();

    private volatile String currentServerId;

    static {
        try {
            TRUSTED_CONTEXT = buildTrustedContext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> clientsList = new ArrayList<>();

    public static final float DEFAULT_PITCH = 1.0f;
    public static final float PITCH_STEP = 0.05f;
    public static final int DEFAULT_TIME_THRESHOLD = 60;
    public static float currentPitch = 1.0f;
    public static int timer = 0;

    public DiscordWebSocketClient(String serverUri) {
        super(URI.create(serverUri));
    }

    public static DiscordWebSocketClient getInstance() {
        return mod.websocketClient;
    }

    private static SSLContext buildTrustedContext() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert;
        try (InputStream in = DiscordWebSocketClient.class.getResourceAsStream("/assets/dptb2-utils/dptbot-ca.crt")) {
            cert = cf.generateCertificate(in);
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("dptbot-ca", cert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx;
    }

    // run when the connection is established. Greeting is now deferred until
    // the server issues a "challenge"/"challengeRequired" handshake message,
    // instead of greeting immediately, to stop scripts from impersonating
    // other clients.
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mod.tryingToConnect = false;
        MC.addScheduledTask(() -> NotificationManager.getInstance().add("DPTBot", "Connected!", 0xFFFFFFFF, "mob.bat.takeoff"));
    }

    private String serverKey() {
        String host = mod.getStringConfig("others.dptbotHost");
        int port = mod.getIntConfig("others.dptbotPort");
        return host + ":" + port;
    }

    private void handleChallenge(final String serverId) {
        if (MC.thePlayer == null || serverId == null) return;
        this.currentServerId = serverId;

        String trustedKey = TRUSTED_KEY.get(serverKey());
        if (trustedKey != null) {
            sendTrustedGreet(trustedKey);
            return;
        }

        final String accessToken = MC.getSession().getToken();
        final String profileId = MC.thePlayer.getGameProfile().getId().toString().replace("-", "");

        // Run the Mojang joinServer call off the client thread so we never
        // block the game while waiting on the network.
        new Thread(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("accessToken", accessToken);
                body.put("selectedProfile", profileId);
                body.put("serverId", serverId);
                byte[] payload = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);

                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/join");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);

                try (BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream())) {
                    out.write(payload);
                }

                int status = connection.getResponseCode();
                connection.disconnect();

                if (status == 200 || status == 204) {
                    MC.addScheduledTask(() -> sendGreet(serverId));
                } else {
                    DPTB2Utils.LOGGER.error("Mojang joinServer call rejected with status {}", status);
                }
            } catch (Exception ex) {
                DPTB2Utils.LOGGER.error("Mojang joinServer call failed", ex);
            }
        }, "dptb2-utils-mojang-handshake").start();
    }

    private void sendGreet(String serverId) {
        if (MC.thePlayer == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", MC.thePlayer.getGameProfile().getName());
        data.put("currentName", MC.thePlayer.getDisplayName().getFormattedText());
        data.put("id", MC.thePlayer.getGameProfile().getId().toString().replace("-", ""));
        data.put("version", DPTB2Utils.VERSION);
        data.put("mc", MC.getVersion());
        data.put("serverId", serverId);
        this.sendModMessage("greet", data);

        sendMicroEvents();
    }

    private void sendTrustedGreet(String trustedKey) {
        if (MC.thePlayer == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", MC.thePlayer.getGameProfile().getName());
        data.put("currentName", MC.thePlayer.getDisplayName().getFormattedText());
        data.put("id", MC.thePlayer.getGameProfile().getId().toString().replace("-", ""));
        data.put("version", DPTB2Utils.VERSION);
        data.put("mc", MC.getVersion());
        data.put("trustedKey", trustedKey);
        this.sendModMessage("greet", data);

        sendMicroEvents();
    }

    private void sendMicroEvents() {
        Map<String, Object> data = new HashMap<>();
        data.put("eventTimer", MicroTimerManager.eventTimer);
        data.put("trafficTimer", MicroTimerManager.trafficTimer);
        data.put("doorTimer", MicroTimerManager.doorTimer);
        data.put("lastEvent", MicroTimerManager.lastEvent);
        data.put("currentTraffic", MicroTimerManager.currentTraffic);
        data.put("currentDoor", MicroTimerManager.currentDoor);
        this.sendModMessage("microEvents", data);
    }

    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        NotificationManager notifManager = NotificationManager.getInstance();
        final String type = (String) data.get("type");

        if ("challenge".equalsIgnoreCase(type) || "challengeRequired".equalsIgnoreCase(type)) {
            if (Boolean.TRUE.equals(data.get("trustedKeyRejected"))) {
                TRUSTED_KEY.remove(serverKey());
            }
            handleChallenge((String) data.get("serverId"));
            return;
        }

        if ("trustedKey".equalsIgnoreCase(type)) {
            String key = (String) data.get("key");
            if (key != null && !key.trim().isEmpty()) {
                TRUSTED_KEY.put(serverKey(), key);
            }
            return;
        }

        final String text = (String) data.get("text");
        final Integer col = (Integer) data.get("color");
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (type.equalsIgnoreCase("delegate")) {
                if (mod.getBoolConfig("others.consentRamper")) {
                    notifManager.add("DPTBot", text, col != null ? col : 0xFFC8FFC8, "mob.bat.takeoff");
                    mod.isRamper = true;
                    this.sendModMessage("confirm", DPTB2Utils.mapOf("text", MC.thePlayer.getGameProfile().getName()));
                } else {
                    notifManager.add("DPTBot", "Ramper request denied.", 0xFFFF0000, "mob.bat.takeoff");
                    mod.isRamper = false;
                    this.sendModMessage("deny", DPTB2Utils.mapOf("text", MC.thePlayer != null ? MC.thePlayer.getGameProfile().getName() : "Unknown"));
                }
            } else if (type.equalsIgnoreCase("revoke")) {
                if (mod.getBoolConfig("others.consentRamper")) {
                    notifManager.add("DPTBot", text, col != null ? col : 0xFFFFC8C8, "mob.bat.takeoff");
                    mod.isRamper = false;
                }
            } else if (type.equalsIgnoreCase("broadcast")) {
                String source = data.get("source") != null ? (String) data.get("source") : "???";
                String name = data.get("name") != null ? (String) data.get("name") : "Unknown";

                StringBuilder sb = new StringBuilder("§8[");
                if (source.equalsIgnoreCase("DISC")) {
                    sb.append("§xDISC§r").append("§8]§r ").append(String.format("§x%s§r", name));
                } else if (source.equalsIgnoreCase("WPTB")) {
                    sb.append("§yWPTB§r").append("§8]§r ").append(String.format("§y%s§r", name));
                } else if (source.equalsIgnoreCase("CONSOLE")) {
                    sb.append("§cCONSOLE§r").append("§8]§r ").append(String.format("§c%s§r", name));
                } else {
                    sb.append("???").append("§8]§r ").append(name);
                }
                sb.append(": ").append(text);

                if (timer > 0) {
                    currentPitch += PITCH_STEP;
                }
                timer = DEFAULT_TIME_THRESHOLD;

                if (mod.getBoolConfig("others.broadcastToast")) {
                    int color = source.equalsIgnoreCase("DISC") ? DPTB2Utils.hexToInt(mod.getStringConfig("others.discColor")) : (source.equalsIgnoreCase("WPTB") ? DPTB2Utils.hexToInt(mod.getStringConfig("others.wptbColor")) : (source.equalsIgnoreCase("CONSOLE") ? 0xFFFF5555 : 0xFFFFFFFF));
                    notifManager.add(String.format("[%s] %s", source, name), text, col != null ? col : color, mod.getBoolConfig("others.broadcastSounds") ? "note.pling" : null, 1, currentPitch);
                }

                if (MC.thePlayer != null && mod.getBoolConfig("others.broadcastChat")) {
                    MC.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                    if (!mod.getBoolConfig("others.broadcastToast") && mod.getBoolConfig("others.broadcastSounds")) {
                        MC.thePlayer.playSound("note.pling", 1, currentPitch);
                    }
                }
            } else if (type.equalsIgnoreCase("askTabList")) {
                String id = (String) data.get("id");
                if (MC.getNetHandler() != null) {
                    List<String> players = MC.getNetHandler().getPlayerInfoMap().stream()
                            .map(player -> player.getGameProfile().getName())
                            .collect(Collectors.toList());
                    this.sendModMessage("tabList", DPTB2Utils.mapOf("id", id, "players", players));
                }
            } else if (type.equalsIgnoreCase("updateClients")) {
                this.clientsList = (List<String>) data.get("clients");
            } else if (type.equalsIgnoreCase("queryIdResponse")) {
                String id = (String) data.get("id");
                String username = (String) data.get("username");
                String kind = (String) data.get("kind");
//                if (!id.isBlank()) {
//                    if (kind.equalsIgnoreCase("DISC")) {
//                        BlockListManager.putDiscUsername(id, username);
//                    } else if (kind.equalsIgnoreCase("WPTB")) {
//                        BlockListManager.putWptbUsername(id, username);
//                    }
//                } else {
//                    // error handling
//                }
            } else if (type.equalsIgnoreCase("queryNameResponse")) {
                String username = (String) data.get("username");
                String id = (String) data.get("id");
                String kind = (String) data.get("kind");
//                if (!username.isBlank()) {
//                    if (kind.equalsIgnoreCase("DISC")) {
//                        BlockListManager.putDiscUsername(id, username);
//                    } else if (kind.equalsIgnoreCase("WPTB")) {
//                        BlockListManager.putWptbUsername(id, username);
//                    }
//                } else {
//                    // error handling
//                }
            } else if (type.equalsIgnoreCase("microEvents")) {
                Object eventTimer = data.get("eventTimer");
                Object trafficTimer = data.get("trafficTimer");
                Object doorTimer = data.get("doorTimer");
                Object lastEvent = data.get("lastEvent");
                Object currentTraffic = data.get("currentTraffic");
                Object currentDoor = data.get("currentDoor");
                if (eventTimer instanceof Double) MicroTimerManager.eventTimer = ((Double) eventTimer).intValue();
                if (trafficTimer instanceof Double) MicroTimerManager.trafficTimer = ((Double) trafficTimer).intValue();
                if (doorTimer instanceof Double) MicroTimerManager.doorTimer = ((Double) doorTimer).intValue();
                if (lastEvent instanceof String) MicroTimerManager.lastEvent = (String) lastEvent;
                if (currentTraffic instanceof String) MicroTimerManager.currentTraffic = (String) currentTraffic;
                if (currentDoor instanceof String) MicroTimerManager.currentDoor = (String) currentDoor;
            } else if (type.equalsIgnoreCase("askGameVar")) {
                if (MC.thePlayer != null) {
                    MC.thePlayer.sendChatMessage("/ᴡᴇᴇʙ◆⚅⚀βΓγ-ΔδενΞo-oΨ");
                }
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (!mod.tryingToConnect) {
            NotificationManager.getInstance().add("DPTBot", String.format("Disconnected: %s (code:%s)", reason, code), 0xFFBABABA, "mob.bat.takeoff");
        }
        DPTB2Utils.LOGGER.error("WebSocket connection closed: {} (code:{}, remote:{})", reason, code, remote);
        this.clientsList = new ArrayList<>();
        this.retryConnection();
    }

    @Override
    public void onError(Exception ex) {
        if (!mod.tryingToConnect) {
            MC.addScheduledTask(() -> NotificationManager.getInstance().add("DPTBot", "Connecting to DPTBot failed!", 0xFFFF0000, "mob.bat.takeoff"));
        }
        ex.printStackTrace();
        this.retryConnection();
    }

    public void sendModMessage(String type, Map<String, Object> data) {
        data = new HashMap<>(data);
        data.put("type", type);
        data.put("version", DPTB2Utils.VERSION);
        if (this.isOpen() && MC.thePlayer != null) {
            this.send(GSON.toJson(data));
        }
    }

    public void retryConnection() {
        mod.tryingToConnect = true;
        mod.scheduleTask( 1200, () -> {
            if ((mod.websocketClient == null || mod.websocketClient.isClosed()) & mod.getBoolConfig("others.consentRamper") && mod.tryingToConnect && mod.isInDPTB2) {
                String host = mod.getStringConfig("others.dptbotHost");
                int port = mod.getIntConfig("others.dptbotPort");

                DPTB2Utils.LOGGER.info("Attempting Websocket connection to wss://{}:{}", host, port);
                mod.websocketClient = new DiscordWebSocketClient(String.format("wss://%s:%d", host, port));
                mod.websocketClient.setSocketFactory(TRUSTED_CONTEXT.getSocketFactory());
                mod.websocketClient.connect();
            }
        });
    }
}
