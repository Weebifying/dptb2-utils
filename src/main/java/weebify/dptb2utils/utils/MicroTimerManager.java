package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import java.util.Arrays;
import java.util.Collections;

import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.MicroTimerConfigScreen;

public class MicroTimerManager {
    public static int eventTimer = -1;
    public static int trafficTimer = -1;
    public static int doorTimer = -1;
    public static int blessingTimer = -1;
    public static String lastEvent = "N/A";
    public static String currentTraffic = "N/A";
    public static String currentDoor = "N/A";

    public static final String eventPrefix = "Last event: ";
    public static final String trafficPrefix = "Traffic light: ";
    public static final String doorPrefix = "Door: ";
    public static final String blessingPrefix = "Button Blessing: ";
    public static String[] EVENTS_LIST = {
            "§7§lMAYHEM",
            "§f§lDISABLED",
            "§c§lIMMUNITY",
            "§a§lJUMP BOOST",
            "§b§lSLIPPERY ICE",
            "§f§lNOTHING"
    };
    public static String[] LIGHTS_LIST = {
            "§a§lGREEN",
            "§c§lRED"
    };


    public static String eventTickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);


        if (ticks >= 6000) return "§c" + timeString;
        else if (ticks >= 5100) return "§6" + timeString;
        else if (ticks >= 4200) return "§e" + timeString;
        return timeString;
    }

    public static String trafficTickToTime(int ticks, boolean isGreen) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (!isGreen) return "§c" + timeString;

        if (ticks <= 160) return "§6" + timeString;
        if (ticks <= 320) return "§e" + timeString;
        return timeString;
    }

    public static String doorTickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (ticks >= 12000) return "§c" + timeString;
        else if (ticks >= 11100) return "§6" + timeString;
        else if (ticks >= 10200) return "§e" + timeString;
        return timeString;
    }

    public static String blessingTickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (ticks <= 60) return "§c" + timeString;
        if (ticks <= 100) return "§6" + timeString;
        if (ticks <= 200) return "§e" + timeString;
        return timeString;
    }

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (MicroTimerManager.eventTimer >= 0) {
                MicroTimerManager.eventTimer += 1;
            }
            if (MicroTimerManager.eventTimer >= 6100) {
                MicroTimerManager.eventTimer -= 6000;
                MicroTimerManager.lastEvent = "§f§lNOTHING";
            }

            if (MicroTimerManager.trafficTimer > 0) {
                MicroTimerManager.trafficTimer -= 1;
            }
            if (MicroTimerManager.doorTimer >= 0) {
                MicroTimerManager.doorTimer += 1;
            }

            if (MicroTimerManager.blessingTimer >= 0) {
                MicroTimerManager.blessingTimer -= 1;
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, "microTimer"),
                MicroTimerManager::renderMicroTimer
        );
    }

    private static void renderMicroTimer(GuiGraphicsExtractor graphics, DeltaTracker renderTickCounter) {
        Minecraft mc = Minecraft.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2 && mod.getBoolConfig("microTimer.enabled") && !(mc.screen instanceof MicroTimerConfigScreen)) {
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            int posX = (int)(mod.getFloatConfig("microTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("microTimer.posY")*height);

            String eventTime = MicroTimerManager.eventTickToTime(MicroTimerManager.eventTimer);
            String trafficTime = MicroTimerManager.trafficTickToTime(MicroTimerManager.trafficTimer, !MicroTimerManager.currentTraffic.equals("§c§lRED"));
            String doorTime = MicroTimerManager.doorTickToTime(MicroTimerManager.doorTimer);
            String blessingTime = MicroTimerManager.blessingTickToTime(MicroTimerManager.blessingTimer);
            int widgetWidth = Collections.max(Arrays.asList(
                    mc.font.width(String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime)),
                    mc.font.width(String.format("%s%s", blessingPrefix, blessingTime)),
                    mc.font.width(String.format("%s%s§r (%s§r)", trafficPrefix, currentTraffic, trafficTime)),
                    mc.font.width(String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime))
            ));
            if (mod.getBoolConfig("microTimer.renderBackground")) {
                graphics.fill(
                        posX,
                        posY,
                        posX + widgetWidth + 8,
                        posY + 21 + mc.font.lineHeight,
                        0x63000000 // ballin it, worked ig
                );
            }

            int cursorY = posY + 4;
            graphics.text(
                    mc.font, String.format("%s%s§r (%s§r)", eventPrefix, lastEvent, eventTime),
                    posX + 4,
                    cursorY,
                    CommonColors.WHITE,
                    mod.getBoolConfig("microTimer.textShadow")
            );
            if (MicroTimerManager.blessingTimer >= 0) {
                cursorY += mc.font.lineHeight + 3;
                graphics.text(
                        mc.font, String.format("%s%s", blessingPrefix, blessingTime),
                        posX + 4,
                        cursorY,
                        CommonColors.WHITE,
                        mod.getBoolConfig("microTimer.textShadow")
                );
            }

            if (mod.currentMap == 2) {
                cursorY += mc.font.lineHeight + 3;
                graphics.text(
                        mc.font, String.format("%s%s§r (%s§r)", trafficPrefix, currentTraffic, trafficTime),
                        posX + 4,
                        cursorY,
                        CommonColors.WHITE,
                        mod.getBoolConfig("microTimer.textShadow")
                );
                cursorY += mc.font.lineHeight + 3;
                graphics.text(
                        mc.font, String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime),
                        posX + 4,
                        cursorY,
                        CommonColors.WHITE,
                        mod.getBoolConfig("microTimer.textShadow")
                );
            }
        }
    }
}

