package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();
    public static final SSLContext TRUSTED_CONTEXT;

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

    public DiscordWebSocketClient(String uri) {
        super(URI.create(uri));
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

    // run when the connection is established
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mod.tryingToConnect = false;
        if (MC.player != null) {
            this.sendModMessage("greet", Map.of(
                    "name", MC.player.getGameProfile().name(),
                    "currentName", MC.player.getDisplayName().getString(),
                    "id", MC.player.getGameProfile().id().toString().replace("-", ""),
                    "version", DPTB2Utils.VERSION, "mc", MC.getLaunchedVersion(),
                    "x", Double.toString(MC.player.getX()),
                    "y", Double.toString(MC.player.getY()),
                    "z", Double.toString(MC.player.getZ())
            ));

            this.sendModMessage("microEvents", Map.of(
                        "eventTimer", MicroTimerManager.eventTimer,
                        "trafficTimer", MicroTimerManager.trafficTimer,
                        "doorTimer", MicroTimerManager.doorTimer,
                        "lastEvent", MicroTimerManager.lastEvent,
                        "currentTraffic", MicroTimerManager.currentTraffic,
                        "currentDoor", MicroTimerManager.currentDoor
            ));
        }
        MC.execute(() -> MC.getToastManager().addToast(new NotificationToast("DPTBot", "Connected!", CommonColors.WHITE, SoundEvents.BAT_TAKEOFF)));
    }

    // run when a message is received from the server
    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        String type = (String) data.get("type");
        String text = (String) data.get("text");
        Integer col = (Integer) data.get("color");
        Minecraft.getInstance().execute(() -> {
                if (type.equalsIgnoreCase("delegate")) {
                    if (mod.getBoolConfig("others.consentRamper")) {
                        MC.getToastManager().addToast(new NotificationToast("DPTBot", text, col != null ? col : 0xFFC8FFC8, SoundEvents.BAT_TAKEOFF));
                        mod.isRamper = true;
                        this.sendModMessage("confirm", Map.of("text", MC.player != null ? MC.player.getGameProfile().name() : "Unknown"));
                    } else {
                        MC.getToastManager().addToast(new NotificationToast("DPTBot", "Ramper request denied.", CommonColors.RED, SoundEvents.BAT_TAKEOFF));
                        mod.isRamper = false;
                        this.sendModMessage("deny", Map.of("text", MC.player != null ? MC.player.getGameProfile().name() : "Unknown"));
                    }
                } else if (type.equalsIgnoreCase("revoke")) {
                    if (mod.getBoolConfig("others.discordRamper")) {
                        MC.getToastManager().addToast(new NotificationToast("DPTBot", text, col != null ? col : 0xFFFFC8C8, SoundEvents.BAT_TAKEOFF));
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
                        MC.getToastManager().addToast(new NotificationToast(String.format("[%s] %s", source, name), text, col != null ? col : color, mod.getBoolConfig("others.broadcastSounds") ? SoundEvents.NOTE_BLOCK_PLING.value() : null, currentPitch, 1));
                    }

                    if (MC.player != null && mod.getBoolConfig("others.broadcastChat")) {
                        MC.player.sendSystemMessage(Component.literal(sb.toString()));
                        if (!mod.getBoolConfig("others.broadcastToast") && mod.getBoolConfig("others.broadcastSounds")) {
                            MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), currentPitch, 1));
                        }
                    }
                } else if (type.equalsIgnoreCase("askTabList")) {
                    String id = (String) data.get("id");
                    if (MC.getConnection() != null) {
                        List<String> players = MC.getConnection().getOnlinePlayers().stream()
                                .map(player -> player.getProfile().name())
                                .toList();
                        this.sendModMessage("tabList", Map.of("id", id, "players", players));
                    }
                } else if (type.equalsIgnoreCase("updateClients")) {
                    this.clientsList = (List<String>) data.get("clients");
                } else if (type.equalsIgnoreCase("queryIdResponse")) {
                    String id = (String) data.get("id");
                    String username = (String) data.get("username");
                    String kind = (String) data.get("kind");
                    if (!id.isBlank()) {
                        if (kind.equalsIgnoreCase("DISC")) {
                            BlockListManager.putDiscUsername(id, username);
                        } else if (kind.equalsIgnoreCase("WPTB")) {
                            BlockListManager.putWptbUsername(id, username);
                        }
                    } else {
                        // error handling
                    }
                } else if (type.equalsIgnoreCase("queryNameResponse")) {
                    String username = (String) data.get("username");
                    String id = (String) data.get("id");
                    String kind = (String) data.get("kind");
                    if (!username.isBlank()) {
                        if (kind.equalsIgnoreCase("DISC")) {
                            BlockListManager.putDiscUsername(id, username);
                        } else if (kind.equalsIgnoreCase("WPTB")) {
                            BlockListManager.putWptbUsername(id, username);
                        }
                    } else {
                        // error handling
                    }
                } else if (type.equalsIgnoreCase("microEvents")) {
                    if (data.get("eventTimer") instanceof Double d) MicroTimerManager.eventTimer = d.intValue();
                    if (data.get("trafficTimer") instanceof Double d) MicroTimerManager.trafficTimer = d.intValue();
                    if (data.get("doorTimer") instanceof Double d) MicroTimerManager.doorTimer = d.intValue();
                    if (data.get("lastEvent") instanceof String s) MicroTimerManager.lastEvent = s;
                    if (data.get("currentTraffic") instanceof String s) MicroTimerManager.currentTraffic = s;
                    if (data.get("currentDoor") instanceof String s) MicroTimerManager.currentDoor = s;
                }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (!mod.tryingToConnect) {
            MC.getToastManager().addToast(new NotificationToast("DPTBot", String.format("Disconnected: %s (code:%s)", reason, code), CommonColors.LIGHTER_GRAY, SoundEvents.BAT_TAKEOFF));
        }
        DPTB2Utils.LOGGER.error("WebSocket connection closed: {} (code:{}, remote:{})", reason, code, remote);
        this.clientsList = new ArrayList<>();
        this.retryConnection();
    }

    @Override
    public void onError(Exception ex) {
        if (!mod.tryingToConnect) {
            MC.execute(() -> MC.getToastManager().addToast(new NotificationToast("DPTBot", "Connecting to DPTBot failed!", CommonColors.RED, SoundEvents.BAT_TAKEOFF)));
        }
        ex.printStackTrace();
        this.retryConnection();
    }

//    public void sendModMessage(String type, String message) {
//        if (this.isOpen() && MC.player != null) {
//            this.send(GSON.toJson(Map.of("type", type, "text", message)));
//        }
//    }

    public void sendModMessage(String type, Map<String, Object> data) {
        data = new HashMap<>(data);
        data.put("type", type);
        data.put("version", DPTB2Utils.VERSION);
//        DPTB2Utils.LOGGER.info("Sending message to DPTBot: {}", data);
        if (this.isOpen() && MC.player != null) {
            this.send(GSON.toJson(data));
        }
    }

    public void retryConnection() {
        mod.tryingToConnect = true;
        mod.scheduleTask( 1200, () -> {
            if ((mod.websocketClient == null || mod.websocketClient.isClosed()) & mod.getBoolConfig("others.discordRamper") && mod.tryingToConnect && mod.isInDPTB2) {
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
