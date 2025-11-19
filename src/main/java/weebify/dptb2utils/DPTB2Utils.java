package weebify.dptb2utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Unique;
import weebify.dptb2utils.gui.screen.GuiButtonTimerConfig;
import weebify.dptb2utils.gui.screen.GuiModMenu;
import weebify.dptb2utils.utils.ButtonTimerManager;
import weebify.dptb2utils.utils.DelayedTask;
import weebify.dptb2utils.utils.DiscordWebSocketClient;
import weebify.dptb2utils.utils.NotificationManager;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Mod(modid = DPTB2Utils.MOD_ID, version = DPTB2Utils.VERSION)
public class DPTB2Utils {
    public static final String MOD_ID = "dptb2-utils";
    public static final String VERSION = "1.1.3";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ModConfigs config;
    private File saveFile;
    private long lastSaved;

    public boolean isInDPTB2 = false;
    public boolean isRamper = false;
    public boolean tryingToConnect = false;
    public boolean checkedJoin = false;

    public List<DelayedTask> scheduledTasks = new ArrayList<>();

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static DPTB2Utils instance;
    public static final Gson GSON = new Gson();

    public DiscordWebSocketClient websocketClient;

    public List<String> bootsList = new ArrayList<>();

    public static DPTB2Utils getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;
        this.config = new ModConfigs();
        this.saveFile = new File(mc.mcDataDir + "/config", "weebify_dptb2utils.json");
        try {
            if (this.saveFile.createNewFile()) {
                try (FileWriter fw = new FileWriter(this.saveFile)) {
                    GSON.toJson(this.config, fw);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.lastSaved = saveFile.lastModified();
        this.loadSettings();

        this.initializeCommands();
        MinecraftForge.EVENT_BUS.register(this);

        this.fetchDPTBotIP();
    }

    public void scheduleTask(int ticks, Runnable task) {
        this.scheduledTasks.add(new DelayedTask(ticks, task));
    }

    public void buttonTimerReset() {
        ButtonTimerManager.buttonTimer = -1;
        ButtonTimerManager.isMayhem = false;
        ButtonTimerManager.isChaos = false;
        ButtonTimerManager.isDisabled = false;
        ButtonTimerManager.chaosCounter = 0;
    }


    public void fetchDPTBotIP() {
        new Thread(() -> {
            try {
                LOGGER.info("Fetching DPTBot IP from https://github.com/Weebifying/Weebifying/blob/main/dptbot.host");
                URL url = URI.create("https://raw.githubusercontent.com/Weebifying/Weebifying/refs/heads/main/dptbot.host").toURL();
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                String address = sb.toString().trim();
                String[] split = address.split(":");
                if (split.length == 2) {
                    this.setDPTBotHost(split[0]);
                    this.setDPTBotPort(Integer.parseInt(split[1]));
                    LOGGER.info("Fetched DPTBot IP: {}:{}", this.getDPTBotHost(), this.getDPTBotPort());
                } else {
                    LOGGER.error("Failed to fetch DPTBot IP! Invalid format: {}", address);
                }

            } catch (Exception e) {
                LOGGER.error("Failed to fetch DPTBot IP!", e);
            }
        }).start();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (this.saveFile.lastModified() > this.lastSaved) {
                this.loadSettings();
                this.lastSaved = this.saveFile.lastModified();
            }

            if (this.isInDPTB2) {
                if (ButtonTimerManager.buttonTimer >= 0) {
                    ButtonTimerManager.buttonTimer += 1;
                }
            }
        } else if (event.phase == TickEvent.Phase.END) {
            scheduledTasks.removeIf(DelayedTask::tick);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote && event.entity == mc.thePlayer && !this.checkedJoin) {
            this.buttonTimerReset();
            this.checkedJoin = true;
            this.scheduleTask(10, () -> this.checkedJoin = false);

            this.dptb2Check();
        }
    }

    public void dptb2Check() {
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) {
            this.isInDPTB2 = false;
            return;
        }
        LOGGER.info("server ip: {}", serverData.serverIP);

        if (!serverData.serverIP.toLowerCase().contains("hypixel.net")) {
            this.isInDPTB2 = false;
            return;
        }

        this.scheduleTask(20, () -> {
            if (mc.theWorld == null) return;

            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

            if (objective != null) {
                String title = objective.getDisplayName().toLowerCase();
                List<Score> scores = (List<Score>) scoreboard.getSortedScores(objective);
                Collections.reverse(scores);

                StringBuilder s = new StringBuilder();
                for (Score score : scores) {
                    ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                    String line = ScorePlayerTeam.formatPlayerName(team, "");
                    s.append(line);
                }

                String content = s.toString().toLowerCase().replaceAll("§\\w", "").trim();

                LOGGER.info("title: {}", title);
                LOGGER.info("content: {}", content);
                this.isInDPTB2 = title.contains("housing") && content.contains("don't press the button 2");

                if (this.isInDPTB2) NotificationManager.getInstance().add("DPTB2 Utils", "You are in Don't Press The Button 2!", 0xD2FFC8, "random.levelup");
                this.refreshRamperStatus();
            }
        });
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
       this.buttonTimerReset();
       if (websocketClient != null && websocketClient.isOpen()) {
           websocketClient.close();
       }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && this.isInDPTB2 && this.getButtonTimerEnabled() && !(mc.currentScreen instanceof GuiButtonTimerConfig)) {
            ScaledResolution scaledRes = new ScaledResolution(mc);
            int width = scaledRes.getScaledWidth();
            int height = scaledRes.getScaledHeight();
            String text = ButtonTimerManager.tickToTime(ButtonTimerManager.buttonTimer);
            int textWidth = mc.fontRendererObj.getStringWidth(text);
            if (this.getButtonTimerRenderBG()) {
                Gui.drawRect(
                        (int) (width*this.getButtonTimerConfigs("posX", Float.class)),
                        (int) (height*this.getButtonTimerConfigs("posY", Float.class)),
                        (int) (width*this.getButtonTimerConfigs("posX", Float.class)) + textWidth + 8,
                        (int) (height*this.getButtonTimerConfigs("posY", Float.class)) + 15,
                        0x63000000 // ballin it, worked ig
                );
            }

            mc.fontRendererObj.drawString(
                    text,
                    (int) (width*this.getButtonTimerConfigs("posX", Float.class)) + 4,
                    (int) (height*this.getButtonTimerConfigs("posY", Float.class)) + 4,
                    0xFFFFFFFF,
                    this.getButtonTimerTextShadow()
            );

            NotificationManager.getInstance().render(event.resolution);
        }
    }

    private void initializeCommands() {
        ClientCommandHandler.instance.registerCommand(new CommandModMenu());
        ClientCommandHandler.instance.registerCommand(new CommandBroadcast());
    }

    public static class CommandModMenu extends CommandBase {
        @Override
        public String getCommandName() {
            return "dptb2";
        }
        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/" +getCommandName();
        }
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                MinecraftForge.EVENT_BUS.unregister(this);
                Minecraft.getMinecraft().displayGuiScreen(new GuiModMenu(DPTB2Utils.getInstance()));
            }
        }
        public int getRequiredPermissionLevel() {
            return 0;
        }
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }
    }

    public static class CommandBroadcast extends CommandBase {
        @Override
        public String getCommandName() {
            return "broadcast";
        }
        @Override
        public List<String> getCommandAliases() {
            List<String> list = new ArrayList<>();
            list.add("bc");
            return list;
        }
        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/" +getCommandName();
        }
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            DPTB2Utils mod = DPTB2Utils.getInstance();
            if (mc.thePlayer != null) {
                if (mod.websocketClient != null && mod.websocketClient.isOpen()) {
                    String msg = String.join(" ", args);
                    try {
                        mod.websocketClient.sendModMessage("playerBroadcast", DPTB2Utils.mapOf("text", msg, "name", mc.thePlayer.getGameProfile().getName()));
                        if (!mod.getBroadcastChat()) {
                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Broadcast message: " + msg));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to send broadcast message!", e);
                        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to send broadcast message!"));
                    }
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not connected to DPTBot!"));
                }
            }
        }
        public int getRequiredPermissionLevel() {
            return 0;
        }
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }
    }

    public void refreshRamperStatus() {
        String host = this.getDPTBotHost();
        int port = this.getDPTBotPort();
        if (this.isInDPTB2 && this.getDiscordRamper()) {
            LOGGER.info("Attempting Websocket connection to ws://{}:{}", host, port);
            websocketClient = new DiscordWebSocketClient(String.format("ws://%s:%s", host, port));
            websocketClient.connect();
        } else {
            this.isRamper = false;
            if (websocketClient != null && websocketClient.isOpen()) {
                LOGGER.info("Closing Websocket connection to ws://{}:{}", host, port);
                websocketClient.close();
            }
        }
    }

    public void saveSettings() {
        try (FileWriter fw = new FileWriter(this.saveFile)) {
            GSON.toJson(this.config, fw);
            LOGGER.info("Settings saved!");
        } catch (IOException e) {
            LOGGER.error("Failed to save settings!", e);
            throw new RuntimeException(e);
        }
    }

    public void loadSettings() {
        try (FileReader fr = new FileReader(this.saveFile)) {
            this.config = GSON.fromJson(fr, ModConfigs.class);
            LOGGER.info("Settings loaded!");
        } catch (IOException e) {
            LOGGER.error("Failed to load settings!", e);
            throw new RuntimeException(e);
        }
    }

    public <T> T getConfig(Map<String, JsonElement> map, Map<String, JsonElement> defaultMap, String key, Class<T> clazz) {
        if (!map.containsKey(key)) {
            map.put(key, defaultMap.get(key));
        }
        return GSON.fromJson(map.get(key), clazz);
    }
    public <T> T setConfig(Map<String, JsonElement> map, String key, T value, Class<T> clazz) {
        JsonElement jsonValue = GSON.toJsonTree(value, clazz);
        JsonElement oldValue = map.put(key, jsonValue);
        return GSON.fromJson(oldValue, clazz);
    }

    public <T> T getNotifs(String key, Class<T> clazz) {
        return this.getConfig(this.config.notifsMap, ModConfigs.notifsDefaultMap, key, clazz);
    }
    public <T> T getButtonTimerConfigs(String key, Class<T> clazz) {
        return this.getConfig(this.config.buttonTimerMap, ModConfigs.buttonTimerDefaultMap, key, clazz);
    }
    public <T> T getItemCooldownConfigs(String key, Class<T> clazz) {
        return this.getConfig(this.config.itemCooldownMap, ModConfigs.itemCooldownDefaultMap, key, clazz);
    }
    public boolean getAutoCheer() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "autoCheer", Boolean.class);
    }
    public boolean getDiscordRamper() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "discordRamper", Boolean.class);
    }
    public String getDPTBotHost() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "dptbotHost", String.class);
    }
    public int getDPTBotPort() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "dptbotPort", Integer.class);
    }
    public boolean getBroadcastToast() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "broadcastToast", Boolean.class);
    }
    public boolean getBroadcastChat() {
        return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "broadcastChat", Boolean.class);
    }

    public boolean getBoolNotifs(String key) {
        return this.getNotifs(key, Boolean.class);
    }
    public boolean getButtonTimerEnabled() {
        return this.getButtonTimerConfigs("enabled", Boolean.class);
    }
    public boolean getButtonTimerTextShadow() {
        return this.getButtonTimerConfigs("textShadow", Boolean.class);
    }
    public boolean getButtonTimerRenderBG() {
        return this.getButtonTimerConfigs("renderBackground", Boolean.class);
    }

    public boolean getItemCooldownEnabled() {
        return this.getItemCooldownConfigs("enabled", Boolean.class);
    }

    public boolean setAutoCheer(boolean value) {
        return this.setConfig(this.config.othersMap, "autoCheer", value, Boolean.class);
    }
    public boolean setDiscordRamper(boolean value) {
        return this.setConfig(this.config.othersMap, "discordRamper", value, Boolean.class);
    }
    public String setDPTBotHost(String value) {
        return this.setConfig(this.config.othersMap, "dptbotHost", value, String.class);
    }
    public int setDPTBotPort(int value) {
        return this.setConfig(this.config.othersMap, "dptbotPort", value, Integer.class);
    }
    public boolean setBroadcastToast(boolean value) {
        return this.setConfig(this.config.othersMap, "broadcastToast", value, Boolean.class);
    }
    public boolean setBroadcastChat(boolean value) {
        return this.setConfig(this.config.othersMap, "broadcastChat", value, Boolean.class);
    }

    public <T> T setNotifs(String key, T value, Class<T> clazz) {
        return this.setConfig(this.config.notifsMap, key, value, clazz);
    }
    public <T> T setButtonTimerConfigs(String key, T value, Class<T> clazz) {
        return this.setConfig(this.config.buttonTimerMap, key, value, clazz);
    }
    public <T> T setItemCooldownConfigs(String key, T value, Class<T> clazz) {
        return this.setConfig(this.config.itemCooldownMap, key, value, clazz);
    }
    public boolean setBoolNotifs(String key, boolean value) {
        return this.setNotifs(key, value, Boolean.class);
    }
    public boolean setButtonTimerEnabled(boolean value) {
        return this.setButtonTimerConfigs("enabled", value, Boolean.class);
    }
    public boolean setButtonTimerTextShadow(boolean value) {
        return this.setButtonTimerConfigs("textShadow", value, Boolean.class);
    }
    public boolean setButtonTimerRenderBG(boolean value) {
        return this.setButtonTimerConfigs("renderBackground", value, Boolean.class);
    }
    public boolean setItemCooldownEnabled(boolean value) {
        return this.setItemCooldownConfigs("enabled", value, Boolean.class);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }
}
