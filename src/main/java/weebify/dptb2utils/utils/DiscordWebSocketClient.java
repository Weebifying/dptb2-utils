package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.ChatComponentText;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();
    public List<String> clientsList = new ArrayList<>();

    public DiscordWebSocketClient(String serverUri) {
        super(URI.create(serverUri));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mod.tryingToConnect = false;
        if (MC.thePlayer != null) {
            this.sendModMessage("greet", DPTB2Utils.mapOf("name", MC.thePlayer.getGameProfile().getName(), "version", DPTB2Utils.VERSION, "mc", MC.getVersion()));
        }
        MC.addScheduledTask(() -> NotificationManager.getInstance().add("DPTBot", "Connected!", 0xFFFFFFFF, "mob.bat.takeoff"));
    }

    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        NotificationManager notifManager = NotificationManager.getInstance();
        String type = (String) data.get("type");
        String text = (String) data.get("text");
        Integer col = (Integer) data.get("color");
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (type.equalsIgnoreCase("delegate")) {
                if (mod.getDiscordRamper() && MC.thePlayer != null) {
                    notifManager.add("DPTBot", text, col != null ? col : 0xFFC8FFC8, "mob.bat.takeoff");
                    mod.isRamper = true;
                    this.sendModMessage("confirm", DPTB2Utils.mapOf("text", MC.thePlayer.getGameProfile().getName()));
                }
            } else if (type.equalsIgnoreCase("revoke")) {
                if (mod.getDiscordRamper()) {
                    notifManager.add("DPTBot", text, col != null ? col : 0xFFFFC8C8, "mob.bat.takeoff");
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
                    notifManager.add(String.format("[%s] %s", source, name), text, col != null ? col : color, "note.pling");
                }

                if (MC.thePlayer != null && mod.getBroadcastChat()) {
                    MC.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                    if (!mod.getBroadcastToast()) {
                        MC.thePlayer.playSound("note.pling", 1, 1);
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
