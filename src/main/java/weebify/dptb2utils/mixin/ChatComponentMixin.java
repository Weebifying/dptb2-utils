package weebify.dptb2utils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;
import weebify.dptb2utils.utils.ButtonTimerManager;
import weebify.dptb2utils.utils.ItemCooldownManager;
import weebify.dptb2utils.utils.MicroTimerManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Unique
    private static final Random rand = new Random();

    @Unique
    private static void triggerNotif(String title, String message, int color, SoundEvent sfx) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Minecraft mc = Minecraft.getInstance();
        ToastManager toastManager = mc.getToastManager();
        if (mod.getBoolConfig("notifs.dontDelaySfx")) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sfx, 1, 1));
        }
        toastManager.addToast(new NotificationToast(title, message, color, mod.getBoolConfig("notifs.dontDelaySfx") ? null : sfx));
    }

    @Unique
    private static String toLegacyText(Component text) {
        StringBuilder sb = new StringBuilder();

        text.visit((style, str) -> {
            sb.append(styleToLegacy(style)).append(str).append("§r");
            return Optional.empty();
        }, Style.EMPTY);

        return sb.toString();
    }

    @Unique
    private static String styleToLegacy(Style style) {
        StringBuilder codes = new StringBuilder();

        if (style.getColor() != null) {
            ChatFormatting color = ChatFormatting.getByName(style.getColor().serialize());
            if (color != null) codes.append(color);
        }

        if (style.isBold()) codes.append(ChatFormatting.BOLD);
        if (style.isItalic()) codes.append(ChatFormatting.ITALIC);
        if (style.isUnderlined()) codes.append(ChatFormatting.UNDERLINE);
        if (style.isStrikethrough()) codes.append(ChatFormatting.STRIKETHROUGH);
        if (style.isObfuscated()) codes.append(ChatFormatting.OBFUSCATED);

        return codes.toString();
    }

    @Inject(method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V", at = @At("HEAD"))
    private void addVisibleMessageInject(GuiMessage message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Minecraft mc = Minecraft.getInstance();

        String msg = toLegacyText(message.content());
        String content = msg.replaceAll("§[0-9a-fk-or]", "").trim();
        SoundEvent sound = SoundEvents.PLAYER_LEVELUP;

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
            } else if (content.startsWith("* [!] Everybody received Jump Boost I for 10s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§a§lJUMP BOOST";
        } else if (content.startsWith("* [!] The Road is covered in SLIPPERY ICE for 10s!")) {
            MicroTimerManager.eventTimer = 0;
            MicroTimerManager.lastEvent = "§b§lSLIPPERY ICE";
        } else if (content.startsWith("* WOAH")) {
            if (mod.getBoolConfig("notifs.bootsCollected")) {
                // placeholders in case shit goes down
                String t = "Someone just found a rare boots!";
                String b = "Boots";
                Pattern pattern1 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) just found ([A-Z]+) (.+?)!");
                Pattern pattern2 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) received (.+?) from an \\[Admin]");
                Pattern pattern3 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) just found (.+?)!");

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

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Component text = Component.empty()
                    .append(Component.literal(String.format("[%s] ", timestamp)).withStyle(ChatFormatting.GRAY)
                    .append(message.content()));
            mod.bootsList.add(text);
        } else if (content.startsWith("* STOP! Traffic Lights are RED!")) {
            MicroTimerManager.trafficTimer = 180;
            MicroTimerManager.currentTraffic = "§c§lRED";
        } else if (content.startsWith("* GO! Traffic Lights are GREEN!")) {
            MicroTimerManager.trafficTimer = 3440;
            MicroTimerManager.currentTraffic = "§a§lGREEN";
        } else if (content.startsWith("* YAY! You choose the correct door!")) {
            if (MicroTimerManager.currentDoor.equals("N/A")) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                if (x >= 61.5 && x <= 66.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 1";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", Map.of("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else if (x >= 56.5 && x <= 61.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 2";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", Map.of("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else {
                    MicroTimerManager.currentDoor = "N/A";
                }

            }
        } else if (content.startsWith("* RIP! That was the wrong door!")) {
            if (MicroTimerManager.currentDoor.equals("N/A")) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                if (x >= 61.5 && x <= 66.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 2";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", Map.of("currentDoor", MicroTimerManager.currentDoor));
                    }
                } else if (x >= 56.5 && x <= 61.5 && y >= 13 && y <= 25 && z >= 81 && z <= 86.5) {
                    MicroTimerManager.currentDoor = "§a§lDoor 1";
                    if (mod.websocketClient != null) {
                        mod.websocketClient.sendModMessage("microEvents", Map.of("currentDoor", MicroTimerManager.currentDoor));
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
            if (mc.getConnection() != null) {
                mod.scheduleTask(rand.nextInt(26) + 5, () -> mc.getConnection().sendCommand("cheer"));
            }
        } else if (content.startsWith("* ➜ The BUTTON was pressed")) {
            ButtonTimerManager.buttonTimer = 0; // reset the button timer

            // chaos button handling
            if (content.endsWith("by CHAOS!")) {
                if (ButtonTimerManager.isChaos) {
                    ButtonTimerManager.chaosCounter -= 1;
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
        }

        if (content.startsWith("* Run started!") && (ItemCooldownManager.lastAdded.equals("Swap Crystal") || ItemCooldownManager.lastAdded.equals("Freeze Ray") || ItemCooldownManager.lastAdded.equals("Lasso"))) {
            ItemCooldownManager.currentCooldowns.remove(ItemCooldownManager.lastAdded);
            ItemCooldownManager.lastAdded = "";
        }

        if (mod.websocketClient != null && mod.isRamper && !content.isBlank()) {
            // inclusion
            if (content.matches("[^:]+:.+") && !content.startsWith("* ")) {
                if (
                        !content.startsWith("From ")
                     && !content.startsWith("To ")
                     && !content.startsWith("Party >")
                     && !content.startsWith("Guild >")
                     && !content.startsWith("Officer >")
                     && !content.startsWith("You'll be ")
                ) {
                    mod.websocketClient.sendModMessage("chat", Map.of("text", msg));
                }
            } else if (content.matches("\\* .+")) {
                mod.websocketClient.sendModMessage("chat", Map.of("text", msg));
            }
        }
    }
}

