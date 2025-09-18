package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();
    public List<String> clientsList = new ArrayList<>();

    public DiscordWebSocketClient(String uri) {
        super(URI.create(uri));
    }

    // run when the connection is established
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mod.tryingToConnect = false;
        if (MC.player != null) {
            this.sendModMessage("greet", Map.of("name", MC.player.getGameProfile().getName(), "version", DPTB2Utils.VERSION, "mc", MC.getGameVersion()));
        }
        MC.execute(() -> MC.getToastManager().add(new NotificationToast("DPTBot", "Connected!", Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF)));
    }

    // run when a message is received from the server
    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        String type = (String) data.get("type");
        String text = (String) data.get("text");
        MinecraftClient.getInstance().execute(() -> {
                if (type.equalsIgnoreCase("delegate")) {
                    if (mod.getDiscordRamper() && MC.player != null) {
                        MC.getToastManager().add(new NotificationToast("DPTBot", text, Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
                        mod.isRamper = true;
                        this.sendModMessage("confirm", Map.of("text", MC.player.getGameProfile().getName()));
                    }
                } else if (type.equalsIgnoreCase("revoke")) {
                    if (mod.getDiscordRamper()) {
                        MC.getToastManager().add(new NotificationToast("DPTBot", text, Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
                        mod.isRamper = false;
                    }
                } else if (type.equalsIgnoreCase("broadcast")) {
                    String source = data.get("source") != null ? (String) data.get("source") : "???";
                    String name = data.get("name") != null ? (String) data.get("name") : "Unknown";

                    StringBuilder sb = new StringBuilder("§8[");
                    if (source.equalsIgnoreCase("DISC")) {
                        sb.append("§9DISC§r").append("§8]§r ").append(String.format("§9%s§r", name));
                    } else if (source.equalsIgnoreCase("WPTB")) {
                        sb.append("§6WPTB§r").append("§8]§r ").append(String.format("§6%s§r", name));
                    } else if (source.equalsIgnoreCase("CONSOLE")) {
                        sb.append("§cCONSOLE§r").append("§8]§r ").append(String.format("§c%s§r", name));
                    } else {
                        sb.append("???").append("§8]§r ").append(name);
                    }
                    sb.append(": ").append(text);

                    if (mod.getBroadcastToast()) {
                        int color = source.equalsIgnoreCase("DISC") ? 0xFF5555FF : (source.equalsIgnoreCase("WPTB") ? 0xFFFFAA00 : (source.equalsIgnoreCase("CONSOLE") ? 0xFFFF5555 : 0xFFFFFFFF));
                        MC.getToastManager().add(new NotificationToast(String.format("[%s] %s", source, name), text, color, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()));
                    }

                    if (MC.player != null && mod.getBroadcastChat()) {
                        MC.player.sendMessage(Text.literal(sb.toString()), false);
                        if (!mod.getBroadcastToast()) {
                            MC.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1, 1));
                        }
                    }
                } else if (type.equalsIgnoreCase("askTabList")) {
                    String id = (String) data.get("id");
                    if (MC.getNetworkHandler() != null) {
                        List<String> players = MC.getNetworkHandler().getPlayerList().stream()
                                .map(player -> player.getProfile().getName())
                                .toList();
                        this.sendModMessage("tabList", Map.of("id", id, "players", players));
                    }
                } else if (type.equalsIgnoreCase("updateClients")) {
                    this.clientsList = (List<String>) data.get("clients");
                }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (!mod.tryingToConnect) {
            MC.getToastManager().add(new NotificationToast("DPTBot", String.format("Disconnected: %s (code:%s)", reason, code), Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
        }
        DPTB2Utils.LOGGER.error("WebSocket connection closed: {} (code:{}, remote:{})", reason, code, remote);
        this.retryConnection();
    }

    @Override
    public void onError(Exception ex) {
        if (!mod.tryingToConnect) {
            MC.execute(() -> MC.getToastManager().add(new NotificationToast("DPTBot", "Connecting to DPTBot failed!", Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF)));
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
            if ((mod.websocketClient == null || mod.websocketClient.isClosed()) & mod.getDiscordRamper() && mod.tryingToConnect && mod.isInDPTB2) {
                String host = mod.getDPTBotHost();
                int port = mod.getDPTBotPort();

                DPTB2Utils.LOGGER.info("Attempting Websocket connection to ws://{}:{}", host, port);
                mod.websocketClient = new DiscordWebSocketClient(String.format("ws://%s:%d", host, port));
                mod.websocketClient.connect();
            }
        });
    }
}
