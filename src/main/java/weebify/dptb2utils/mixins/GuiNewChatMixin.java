package weebify.dptb2utils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.ButtonTimerManager;
import weebify.dptb2utils.utils.ItemCooldownManager;
import weebify.dptb2utils.utils.MicroTimerManager;
import weebify.dptb2utils.utils.NotificationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {
    @Unique
    private static final Random rand = new Random();
    @Unique
    private static boolean excludeThisAndNext = false;
    @Unique
    private static int counter = 0;
    @Unique
    private static List<String> bulks = new ArrayList<>();

    @Unique
    private static final List<Pattern> AUTOWELCOME_PATTERNS = Arrays.asList(
            Pattern.compile("\\* \\+ \\| >> \\[XI] ([\\w_]+) has joined! <"),
            Pattern.compile("\\* \\+ \\| >>> \\[Builder] ([\\w_]+) constructs a path into the world! <<<"),
            Pattern.compile("\\* \\+ \\| >>> \\[Staff] ([\\w_]+) arrives into the world! <<<"),
            Pattern.compile("\\* \\+ \\| >>> \\[Admin] ([\\w_]+) descends into the world! <<"),
            Pattern.compile("\\* \\+ \\| >>> The \\[Owner], ([\\w_]+) has joined! <<<")
    );

    @Unique
    private static void triggerNotif(String title, String message, int color, String sfx) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        NotificationManager manager = NotificationManager.getInstance();
        if (mod.getBoolConfig("notifs.dontDelaySfx")) {
            mc.thePlayer.playSound(sfx, 1, 1);
        }

        manager.add(title, message, color, sfx);
    }

    @Inject(method = {"printChatMessage"}, at = {@At("HEAD")}, cancellable = true)
    private void printChatMessageInject(IChatComponent component, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Minecraft mc = Minecraft.getMinecraft();

        String message = component.getFormattedText();
        String content = message.replaceAll("§[0-9a-fk-or]", "").trim();
        String sound = "random.levelup";

        if (mod.getBoolConfig("notifs.shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
            triggerNotif("Shop Update!", "New items available at the Rotating Shop!", 0xFF55FF, sound);
        } else if (content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§4§lMAYHEM";

            ButtonTimerManager.isMayhem = true;
            mod.scheduleTask(200, () -> ButtonTimerManager.isMayhem = false);

            if (mod.getBoolConfig("notifs.buttonMayhem")) {
                triggerNotif("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFF0000, sound);
            }
        } else if (content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§7§lDISABLED";

            ButtonTimerManager.isDisabled = true;
            mod.scheduleTask(100, () -> ButtonTimerManager.isDisabled = false);

            if (mod.getBoolConfig("notifs.buttonDisable")) {
                triggerNotif("Button Disabled!", "The BUTTON has been disabled for 5s!", 0x00FF00, sound);
            }
        } else if (content.startsWith("* [!] Whoever clicks the BUTTON next will not die!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§c§lIMMUNITY";

            if (mod.getBoolConfig("notifs.buttonImmunity")) {
                triggerNotif("Button Immunity!", "Whoever clicks the BUTTON next will not die!", 0x55FFFF, sound);
            }
        } else if (content.startsWith("* [!] Everybody received Jump Boost V for 10s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§a§lJUMP BOOST";
        } else if (content.startsWith("* [!] The Road is covered in SLIPPERY ICE for 10s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§b§lSLIPPERY ICE";
        } else if (content.startsWith("* WOAH")) {
            String t = "Someone just found a rare boots!";
            String b = "Boots";
            if (mod.getBoolConfig("notifs.bootsCollected")) {
                Pattern pattern1 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] ([\\w_]+) just found ([A-Z]+) (.+?)!");
                Pattern pattern2 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] ([\\w_]+) received (.+?) from an \\[Admin]");
                Pattern pattern3 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] ([\\w_]+) just found (.+?)!");

                Matcher matcher1 = pattern1.matcher(content);
                Matcher matcher2 = pattern2.matcher(content);
                Matcher matcher3 = pattern3.matcher(content);

                if (matcher1.find()) {
                    t = String.format("[%s] %s found %s %s!", matcher1.group(1), matcher1.group(2), matcher1.group(3), matcher1.group(4));
                    b = matcher1.group(4);
                } else if (matcher2.find()) {
                    t = String.format("[%s] %s received %s!", matcher2.group(1), matcher2.group(2), matcher2.group(3));
                    b = matcher2.group(3);
                } else if (matcher3.find()) {
                    t = String.format("[%s] %s found %s!", matcher3.group(1), matcher3.group(2), matcher3.group(3));
                    b = matcher3.group(3);
                }

                if (mod.getBoolConfig("notifs.slimeBoots") || !b.equalsIgnoreCase("Slime Boots")) {
                    triggerNotif(b + " Found!", t, 0xFFFF55, sound);
                }
            }

            t = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            b = String.format("§7[%s] §r%s", t, message);
            mod.bootsList.add(b);
        } else if (content.startsWith("* STOP! Traffic Lights are RED!")) {
            MicroTimerManager.trafficTimer = 180;
            MicroTimerManager.currentTraffic = "§c§lRED";
        } else if (content.startsWith("* GO! Traffic Lights are GREEN!")) {
            MicroTimerManager.trafficTimer = 3440;
            MicroTimerManager.currentTraffic = "§a§lGREEN";
        } else if (content.startsWith("* YAY! You choose the correct door!")) {
            if (MicroTimerManager.currentDoor.equals("N/A") && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ;
                if (x >= 61.5 && x <= 66.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 1";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", DPTB2Utils.mapOf("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else if (x >= 56.5 && x <= 61.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 2";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", DPTB2Utils.mapOf("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else {
                    MicroTimerManager.currentDoor = "N/A";
                }
            }
        } else if (content.startsWith("* RIP! That was the wrong door!")) {
            if (MicroTimerManager.currentDoor.equals("N/A") && mc.thePlayer != null) {
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ;
                if (x >= 61.5 && x <= 66.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 2";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", DPTB2Utils.mapOf("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else if (x >= 56.5 && x <= 61.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 1";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", DPTB2Utils.mapOf("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else {
                    MicroTimerManager.currentDoor = "N/A";
                }
            }
        } else if (content.startsWith("* [!] The DOOR has cycled! Which one is it now?")) {
            MicroTimerManager.doorTimer = 0;
            MicroTimerManager.currentDoor = "N/A";

            if (mod.getBoolConfig("notifs.doorSwitch")) {
                triggerNotif("Door Switch!", "The DOOR has cycled! Which one is it now?", 0xFFAA00, sound);
            }
        } else if (mod.getBoolConfig("others.autoCheer") && content.startsWith("* COMMUNITY GOAL!")) {
            mod.scheduleTask(rand.nextInt(30) + 10, () -> mc.thePlayer.sendChatMessage("/cheer"));
        } else if (content.startsWith("* ➜ The BUTTON was pressed")) {
            ButtonTimerManager.buttonTimer = 0;
            if (content.endsWith("by CHAOS!")) {
                if (ButtonTimerManager.isChaos) {
                    --ButtonTimerManager.chaosCounter;
                    if (ButtonTimerManager.chaosCounter <= 0) {
                        ButtonTimerManager.isChaos = false;
                    }
                } else {
                    ButtonTimerManager.isChaos = true;
                    ButtonTimerManager.chaosCounter = 32;
                }
            }
        } else if (content.startsWith("*   MINOR EVENT! ➜ CHAOS BUTTON")) {
            ButtonTimerManager.buttonTimer = 0;
            ButtonTimerManager.isChaos = true;
            ButtonTimerManager.chaosCounter = 33;
        } else if (content.startsWith("* [WPTB]")) {
            // * [WPTB] Raycast! ▒ <-- mini raycast confirmation from the server
            if (content.contains("Raycast!")) {
                if (ItemCooldownManager.RAYCAST_ITEMS.contains(ItemCooldownManager.lastRaycast)) {
                    ItemCooldownManager.addCooldown(ItemCooldownManager.lastRaycast);
                }
            } else {
                // * [WPTB] 2 | 5,525 | 243,535 | Stargazer
                Pattern p = Pattern.compile("\\* \\[WPTB] (\\d) \\| ([\\d,]+) \\| ([\\d,]+) \\| ([\\w\\s?!&]+)");
                Matcher m = p.matcher(content);
                if (mod.websocketClient != null && m.find()) {
                    mod.currentMap = Integer.parseInt(m.group(1));
                    int pkCiv = Integer.parseInt(m.group(2).replace(",", ""));
                    int bank = Integer.parseInt(m.group(3).replace(",", ""));
                    String routeJp = m.group(4);
                    mod.websocketClient.sendModMessage("gameVar", DPTB2Utils.mapOf(
                            "currentMap", mod.currentMap,
                            "currentPkCiv", pkCiv,
                            "currentBank", bank,
                            "currentRouteJp", routeJp
                    ));
                }
            }
            ci.cancel();
        } else if (content.startsWith("* [!] A k Button Blessing k has spawned")) {
            MicroTimerManager.blessingTimer = 1200;
        } else if (content.startsWith("* + | >>") && mod.getBoolConfig("others.autoWelcome")) {
            for (Pattern p : AUTOWELCOME_PATTERNS) {
                Matcher m = p.matcher(content);
                if (m.find() && mc.thePlayer != null && !m.group(1).equals(mc.thePlayer.getGameProfile().getName())) {
                    mod.scheduleTask(rand.nextInt(30) + 10, () -> mc.thePlayer.sendChatMessage("/welcome"));
                    break;
                }
            }
        }

        if (content.startsWith("* Run started!") && ItemCooldownManager.RAYCAST_ITEMS.contains(ItemCooldownManager.lastAdded)) {
            ItemCooldownManager.currentCooldowns.remove(ItemCooldownManager.lastAdded);
            ItemCooldownManager.lastAdded = "";
        }

        if (mod.isRamper && mod.websocketClient != null && content.length() > 0) {
            if (content.matches("[^:]+:.+") && !content.startsWith("* ")) {
                if (!content.startsWith("From ") && !content.startsWith("To ") && !content.startsWith("Party >") && !content.startsWith("Guild >") && !content.startsWith("Officer >") && !content.startsWith("You'll be ")) {
                    mod.websocketClient.sendModMessage("chat", DPTB2Utils.mapOf("text", message));
                }
            } else if (content.matches("\\* .+")) {
                mod.websocketClient.sendModMessage("chat", DPTB2Utils.mapOf("text", message));
            }
        }
    }
}
