package weebify.dptb2utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Unique;
import weebify.dptb2utils.gui.screen.GuiButtonTimerConfig;
import weebify.dptb2utils.gui.screen.GuiModMenu;
import weebify.dptb2utils.utils.*;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Mod(modid = DPTB2Utils.MOD_ID, version = DPTB2Utils.VERSION)
public class DPTB2Utils {
    public static final String MOD_ID = "dptb2-utils";
    public static final String VERSION = "1.2.2";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ModConfigs config;
    private File saveFile;
    private long lastSaved;

    public boolean isInDPTB2 = false;
    public boolean isRamper = false;
    public boolean tryingToConnect = false;
    public boolean checkedJoin = false;
    public boolean isToggleBc = false;
    public boolean dptb2RecheckScheduled = false;

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

        ButtonTimerManager.initialize();
        ItemCooldownManager.initialize();
        ExternalIndicatorManager.initialize();
        MicroTimerManager.initialize();

        this.fetchDPTBotIP();

        // for external indicator file chooser
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.error("Failed to set Swing look and feel!", e);
        }
    }

    public static int hexToInt(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        try {
            return ((int) Long.parseLong(hex, 16)) | 0xFF000000;
        } catch (NumberFormatException e) {
            return 0;
        }
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
                    this.setStringConfig("others.dptbotHost", split[0]);
                    this.setIntConfig("others.dptbotPort", Integer.parseInt(split[1]));
                    LOGGER.info("Fetched DPTBot IP: {}:{}", this.getStringConfig("others.dptbotHost"), this.getIntConfig("others.dptbotPort"));
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

                if (DiscordWebSocketClient.timer > 0) {
                    DiscordWebSocketClient.timer--;
                }
                if (DiscordWebSocketClient.timer == 0) {
                    DiscordWebSocketClient.currentPitch = DiscordWebSocketClient.DEFAULT_PITCH;
                }
            }
        } else if (event.phase == TickEvent.Phase.END) {
            scheduledTasks.removeIf(DelayedTask::tick);
            if (this.dptb2RecheckScheduled) {
                this.dptb2RecheckScheduled = false;
                this.scheduleTask(600, this::dptb2Check);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote && event.entity == mc.thePlayer && !this.checkedJoin) {
            this.buttonTimerReset();
            this.checkedJoin = true;
            this.scheduleTask(10, () -> this.checkedJoin = false);

            this.scheduleTask(20, this::dptb2Check);
        }
    }

    public void dptb2Check() {
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) {
            this.isInDPTB2 = false;
            return;
        }

        if (!serverData.serverIP.toLowerCase().contains("hypixel.net")) {
            this.isInDPTB2 = false;
            return;
        }

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

            boolean alreadyInDPTB2 = this.isInDPTB2;
            this.isInDPTB2 = content.contains("don't press the button 2") && content.contains("by cyborg023");

            if (this.isInDPTB2 && !alreadyInDPTB2) {
                NotificationManager.getInstance().add("DPTB2 Utils", "You are in Don't Press The Button 2!", 0xD2FFC8, "random.levelup");
            }
            if (this.isInDPTB2) {
                this.dptb2RecheckScheduled = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
       this.buttonTimerReset();
       if (websocketClient != null && websocketClient.isOpen()) {
           websocketClient.close();
       }
    }



    private void initializeCommands() {
        ClientCommandHandler.instance.registerCommand(new CommandModMenu());
        ClientCommandHandler.instance.registerCommand(new CommandBroadcast());
        ClientCommandHandler.instance.registerCommand(new CommandTogglebc());
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
            DPTB2Utils mod = DPTB2Utils.getInstance();
            mod.scheduleTask(1, () -> Minecraft.getMinecraft().displayGuiScreen(new GuiModMenu(mod)));
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
            mod.handleBroadcast(args);
        }
        public int getRequiredPermissionLevel() {
            return 0;
        }
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }
    }

    public void handleBroadcast(String[] args) {
        if (mc.thePlayer != null) {
            if (this.websocketClient != null && this.websocketClient.isOpen()) {
                String msg = String.join(" ", args);
                try {
                    this.websocketClient.sendModMessage("playerBroadcast", DPTB2Utils.mapOf("text", msg, "name", mc.thePlayer.getGameProfile().getName(),"private", this.getBoolConfig("others.incognito")));
                    if (!this.getBoolConfig("others.broadcastChat")) {
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

    public static class CommandTogglebc extends CommandBase {
        @Override
        public String getCommandName() {
            return "togglebc";
        }
        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/" +getCommandName();
        }
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            DPTB2Utils.getInstance().isToggleBc = !DPTB2Utils.getInstance().isToggleBc;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Automatic chat broadcast mode is now " + (DPTB2Utils.getInstance().isToggleBc ? "§a§lenabled§r!" : "§c§ldisabled§r!")));
        }
        public int getRequiredPermissionLevel() {
            return 0;
        }
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }
    }

    public void refreshWptbStatus() {
        String host = this.getStringConfig("others.dptbotHost");
        int port = this.getIntConfig("others.dptbotPort");
        if (this.isInDPTB2 && this.getBoolConfig("others.discordRamper") && (this.websocketClient == null || !this.websocketClient.isOpen())) {
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

    public void reassessRamperStatus() {
        LOGGER.info("isInDPTB2: {}, consentRamper: {}", this.isInDPTB2, this.getBoolConfig("others.consentRamper"));
        if (this.isInDPTB2 && this.getBoolConfig("others.discordRamper")) {
            if (websocketClient != null && websocketClient.isOpen()) {
                websocketClient.sendModMessage("reassessConsent", mapOf("name", mc.thePlayer != null ? mc.thePlayer.getGameProfile().getName() : "Unknown", "consent", this.getBoolConfig("others.consentRamper")));
            }
        } else {
            this.isRamper = false;
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

    public <T> T getConfig(String prop) {
        return this.config.getConfig(prop);
    }
    public boolean getBoolConfig(String prop) {
        if (ModConfigs.propertyTypes.get(prop) != Boolean.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Boolean!");
        }
        return this.getConfig(prop);
    }
    public int getIntConfig(String prop) {
        if (ModConfigs.propertyTypes.get(prop) != Integer.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Integer!");
        }
        return this.getConfig(prop);
    }
    public float getFloatConfig(String prop) {
        if (ModConfigs.propertyTypes.get(prop) != Float.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Float!");
        }
        return this.getConfig(prop);
    }
    public String getStringConfig(String prop) {
        if (ModConfigs.propertyTypes.get(prop) != String.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type String!");
        }
        return this.getConfig(prop);
    }
    public List<String> getListConfig(String prop) {
        if (ModConfigs.propertyTypes.get(prop) != List.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type List!");
        }
        return this.getConfig(prop);
    }

    public <T> T setConfig(String prop, T value) {
        return this.config.setConfig(prop, value);
    }
    public boolean setBoolConfig(String prop, boolean value) {
        if (ModConfigs.propertyTypes.get(prop) != Boolean.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Boolean!");
        }
        return this.setConfig(prop, value);
    }
    public int setIntConfig(String prop, int value) {
        if (ModConfigs.propertyTypes.get(prop) != Integer.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Integer!");
        }
        return this.setConfig(prop, value);
    }
    public float setFloatConfig(String prop, float value) {
        if (ModConfigs.propertyTypes.get(prop) != Float.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type Float!");
        }
        return this.setConfig(prop, value);
    }
    public String setStringConfig(String prop, String value) {
        if (ModConfigs.propertyTypes.get(prop) != String.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type String!");
        }
        return this.setConfig(prop, value);
    }
    public List<String> setListConfig(String prop, List<String> value) {
        if (ModConfigs.propertyTypes.get(prop) != List.class) {
            throw new IllegalArgumentException("Property " + prop + " is not of type List!");
        }
        return this.setConfig(prop, value);
    }

    public boolean toggleBoolConfig(String prop) {
        return !this.setBoolConfig(prop, !this.getBoolConfig(prop));
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

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return Collections.unmodifiableMap(map);
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return Collections.unmodifiableMap(map);
    }
}
