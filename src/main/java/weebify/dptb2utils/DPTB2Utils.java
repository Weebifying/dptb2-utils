package weebify.dptb2utils;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.*;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weebify.dptb2utils.gui.widget.NotificationToast;
import weebify.dptb2utils.gui.screen.ModMenuScreen;
import weebify.dptb2utils.utils.*;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

public class DPTB2Utils implements ClientModInitializer {	
	public static final String MOD_ID = "dptb2-utils";
	public static final String VERSION = "1.2.3";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ModConfigs config;
	private File saveFile;
	private long lastSaved;

	private boolean displayScreen = false;
	public boolean isInDPTB2 = false;
	public boolean isRamper = false;
	public boolean tryingToConnect = false;
	public boolean isToggleBc = false;
	public boolean dptb2RecheckScheduled = false;

	public int currentMap = 0;
	public static String[] MAPS_LIST = {
			"N/A",
			"Wild West",
			"City"
	};

	public List<DelayedTask> scheduledTasks = new ArrayList<>();

	private static final Minecraft mc = Minecraft.getInstance();
	private static DPTB2Utils instance;
	public static final Gson GSON = new Gson();

	@Nullable
	public DiscordWebSocketClient websocketClient;

	public List<Component> bootsList = new ArrayList<>();

	public static DPTB2Utils getInstance() {
		return instance;
	}

	@Override
	public void onInitializeClient() {
		instance = this;
		this.config = new ModConfigs();
		this.saveFile = new File(mc.gameDirectory + "/config", "weebify_dptb2utils.json");
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
		this.initializeEvents();
		ButtonTimerManager.initialize();
		ItemCooldownManager.initialize();
		ExternalIndicatorManager.initialize();
		MicroTimerManager.initialize();

		this.fetchDPTBotIP();

//		WaypointManager.initializeEvents();
//		WaypointManager.initializeWaypoints();

		// for external indicator file chooser
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOGGER.error("Failed to set Swing look and feel!", e);
		}
	}

	public static int checkMap(double x, double y, double z) {
		// city: 124 7 -113 -> -1 72 140
		if (x >= -1 && x <= 124 && y >= 7 && y <= 72 && z >= -113 && z <= 140) {
			return 1;
		}
		// wild west: -18 120 -108 -> -105 195 138
		if (x >= -105 && x <= -18 && y >= 120 && y <= 195 && z >= -108 && z <= 138) {
			return 2;
		}

		return 0;
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

	private void initializeEvents() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register((var) -> {
			scheduledTasks.removeIf(DelayedTask::tick);
			if (this.dptb2RecheckScheduled) {
				this.dptb2RecheckScheduled = false;
				this.scheduleTask(600, () -> {
                    try {
                        this.dptb2Check(var);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
			}
		});
		// detecting whether the player is in DPTB2
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			this.buttonTimerReset();
			this.scheduleTask(20, () -> {
                try {
                    this.dptb2Check(client);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			this.buttonTimerReset();
			if (websocketClient != null && websocketClient.isOpen()) {
				websocketClient.close();
			}
		});

		ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
			if (this.isToggleBc) {
				this.handleBroadcast(message);
				return false;
			}

			return true;
		});
	}

	public void 	dptb2Check(Minecraft client) throws InterruptedException {
		ServerData serverEntry = client.getCurrentServer();
		if (serverEntry == null) {
			this.isInDPTB2 = false;
			return;
		}
		if (!serverEntry.ip.toLowerCase().contains("hypixel.net")) {
			this.isInDPTB2 = false;
			return;
		}

		if (client.level == null) return;

		Scoreboard scoreboard = client.level.getScoreboard();
		Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);

		if (objective != null) {
			String title = objective.getDisplayName().getString().toLowerCase();
			Component[] sidebarEntries = scoreboard.listPlayerScores(objective)
					.stream()
					.filter(score -> !score.isHidden())
					.sorted(Comparator.comparing(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
					.map(scoreboardEntry -> {
						PlayerTeam team = scoreboard.getPlayersTeam(scoreboardEntry.owner());
						Component textx = scoreboardEntry.ownerName();
						return (Component) PlayerTeam.formatNameForTeam(team, textx);
					})
					.toArray(Component[]::new);

			StringBuilder s = new StringBuilder();
			for (Component entry : sidebarEntries) {
				s.append(entry.getString());
			}

			String scoreboardContent = s.toString().toLowerCase().replaceAll("§\\w", "");

//			this.isInDPTB2 = title.contains("housing") && scoreboardContent.contains("don't press the button 2");
			boolean alreadyInDPTB2 = this.isInDPTB2;
			this.isInDPTB2 = scoreboardContent.contains("don't press the button 2") && scoreboardContent.contains("cyborg023");

			if (this.isInDPTB2 && !alreadyInDPTB2) {
				client.getToastManager().addToast(new NotificationToast("DPTB2 Utils", "You are in Don't Press The Button 2!", 0xD2FFC8, SoundEvents.PLAYER_LEVELUP));
				client.getConnection().sendCommand("ᴡᴇᴇʙ◆⚅⚀βΓγ-ΔδενΞo-oΨ");
				// * [WPTB] 2 | 5,525 | 243,535 | Stargazer
			}

			if (this.isInDPTB2 != alreadyInDPTB2) {
				this.refreshWptbStatus();
			}
//			if (this.isInDPTB2) {
//				this.dptb2RecheckScheduled = true;
//				this.currentMap = checkMap(client.player.getX(), client.player.getY(), client.player.getZ());
//			}
		}
	}

	private void initializeCommands() {
		ClientCommandRegistrationCallback.EVENT.register(this::commandModMenu);
		ClientCommandRegistrationCallback.EVENT.register(this::commandBroadcast);
//		ClientCommandRegistrationCallback.EVENT.register(this::commandAddWP);
		ClientCommandRegistrationCallback.EVENT.register(this::commandToggleBc);
	}

	private void onClientTick(Minecraft var) {
		if (this.saveFile.lastModified() > this.lastSaved) {
			this.loadSettings();
			this.lastSaved = this.saveFile.lastModified();
		}

		if (this.displayScreen) {
			this.displayScreen = false;
			mc.setScreen(new ModMenuScreen(this));
		}

		if (DiscordWebSocketClient.timer > 0) {
			DiscordWebSocketClient.timer--;
		}
		if (DiscordWebSocketClient.timer == 0) {
			DiscordWebSocketClient.currentPitch = DiscordWebSocketClient.DEFAULT_PITCH;
		}
	}

	private void commandToggleBc(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
				ClientCommands.literal("togglebc")
						.executes(graphics -> {
							this.isToggleBc = !this.isToggleBc;
							if (mc.player != null) {
								mc.player.sendSystemMessage(Component.nullToEmpty("Automatic chat broadcast is now " + (this.isToggleBc ? "§a§lenabled§r!" : "§c§ldisabled§r!")));
							}
							return 1;
						})
		);
	}

	private void commandModMenu(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
				ClientCommands.literal("dptb2")
						.executes(graphics -> {
							this.displayScreen = true; // necessary to open the config screen 1 tick late, stupid shit idk why
							return 1;
						})
		);
	}

//	private void commandAddWP(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
//		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
//				ClientCommandManager.literal("addwp")
//						.then(ClientCommandManager.argument("id", StringArgumentType.string())
//						.then(ClientCommandManager.argument("coordX", FloatArgumentType.floatArg())
//						.then(ClientCommandManager.argument("coordY", FloatArgumentType.floatArg())
//						.then(ClientCommandManager.argument("coordZ", FloatArgumentType.floatArg())
//						.then(ClientCommandManager.argument("label", StringArgumentType.greedyString())
//						.executes(graphics -> {
//							String id = StringArgumentType.getString(graphics, "id");
//							float coordX = FloatArgumentType.getFloat(graphics, "coordX");
//							float coordY = FloatArgumentType.getFloat(graphics, "coordY");
//							float coordZ = FloatArgumentType.getFloat(graphics, "coordZ");
//							String label = StringArgumentType.getString(graphics, "label");
//
//							mc.player.sendMessage(Text.literal(String.format("Added waypoint %s at (%f, %f, %f) with label '%s'", id, coordX, coordY, coordZ, label)).formatted(Formatting.GREEN), false);
//							WaypointManager.addWaypoint(id, coordX, coordY, coordZ, label, 0xD2FFC8);
//							return 1;
//						}
//						))))))
//		);
//	}

	private void handleBroadcast(String msg) {
		if (mc.player != null) {
			if (websocketClient != null && websocketClient.isOpen()) {
				try {
					websocketClient.sendModMessage("playerBroadcast", Map.of("text", msg, "name", mc.player.getGameProfile().name(), "private", this.getBoolConfig("others.incognito")));
					if (!this.getBoolConfig("others.broadcastChat")) {
						mc.player.sendSystemMessage(Component.literal("Broadcast message: " + msg).withStyle(ChatFormatting.GREEN));
					}
				} catch (Exception e) {
					LOGGER.error("Failed to send broadcast message!", e);
					mc.player.sendSystemMessage(Component.literal("Failed to send broadcast message!").withStyle(ChatFormatting.RED));
				}
			} else {
				mc.player.sendSystemMessage(Component.literal("Not connected to DPTBot!").withStyle(ChatFormatting.RED));
			}
		}
	}

	private void commandBroadcast(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
				ClientCommands.literal("broadcast")
						.then(ClientCommands.argument("message", StringArgumentType.greedyString())
								// could be faulty, need urgent testing
								.suggests((ctx, builder) -> {
									int lastSpace = builder.getRemaining().lastIndexOf(' ');
									SuggestionsBuilder sb = builder.createOffset(builder.getStart() + lastSpace + 1);
									return SharedSuggestionProvider.suggest(ctx.getSource().getOnlinePlayerNames(), sb);
								})
						.executes(graphics -> {
							this.handleBroadcast(StringArgumentType.getString(graphics, "message"));
							return 1;
						})
					)
		);
		dispatcher.register(
				ClientCommands.literal("bc")
						.then(ClientCommands.argument("message", StringArgumentType.greedyString())
								.suggests((ctx, builder) -> {
									int lastSpace = builder.getRemaining().lastIndexOf(' ');
									SuggestionsBuilder sb = builder.createOffset(builder.getStart() + lastSpace + 1);
									return SharedSuggestionProvider.suggest(ctx.getSource().getOnlinePlayerNames(), sb);
								})
						.executes(graphics -> {
							this.handleBroadcast(StringArgumentType.getString(graphics, "message"));
							return 1;
						}).redirect(c)
					)
		);
	}

	public void refreshWptbStatus() throws InterruptedException {
		String host = this.getStringConfig("others.dptbotHost");
		int port = this.getIntConfig("others.dptbotPort");
		if (this.isInDPTB2 && this.getBoolConfig("others.discordRamper") && (this.websocketClient == null || !this.websocketClient.isOpen())) {
			LOGGER.info("Attempting Websocket connection to wss://{}:{}", host, port);
			this.websocketClient = new DiscordWebSocketClient(String.format("wss://%s:%s", host, port));
			this.websocketClient.setSocketFactory(DiscordWebSocketClient.TRUSTED_CONTEXT.getSocketFactory());
			this.websocketClient.connect();
		} else {
			this.isRamper = false;
			if (this.websocketClient != null && this.websocketClient.isOpen()) {
				LOGGER.info("Closing Websocket connection to wss://{}:{}", host, port);
				this.websocketClient.closeBlocking();
			}
		}
	}

	public void reassessRamperStatus() {
		LOGGER.info("isInDPTB2: {}, consentRamper: {}", this.isInDPTB2, this.getBoolConfig("others.consentRamper"));
		if (this.isInDPTB2 && this.getBoolConfig("others.discordRamper")) {
			if (websocketClient != null && websocketClient.isOpen()) {
				websocketClient.sendModMessage("reassessConsent", Map.of("name", mc.player != null ? mc.player.getGameProfile().name() : "Unknown", "consent", this.getBoolConfig("others.consentRamper")));
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
}