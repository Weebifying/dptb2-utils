package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.MicroTimerConfigScreen;

public class MicroTimerManager {
    public static int microTimer = -1;
    public static int trafficTimer = -1;
    public static int doorTimer = -1;
    public static String lastEvent = "N/A";
    public static String currentTraffic = "N/A";
    public static String currentDoor = "N/A";
    public static final String eventPrefix = "Last event: ";
    public static final String trafficPrefix = "Traffic light: ";
    public static final String doorPrefix = "Door: ";
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

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (MicroTimerManager.microTimer >= 0) {
                MicroTimerManager.microTimer += 1;
            }
            if (MicroTimerManager.microTimer >= 6100) {
                MicroTimerManager.microTimer -= 6000;
                MicroTimerManager.lastEvent = "§f§lNOTHING";
            }

            if (MicroTimerManager.trafficTimer > 0) {
                MicroTimerManager.trafficTimer -= 1;
            }
            if (MicroTimerManager.doorTimer >= 0) {
                MicroTimerManager.doorTimer += 1;
            }
        });

//        HudLayerRegistrationCallback.EVENT.register((drawer) -> {
//            drawer.attachLayerAfter(
//                    IdentifiedLayer.HOTBAR_AND_BARS,
//                    Identifier.of(DPTB2Utils.MOD_ID, "manager/micro_timer"),
//                    MicroTimerManager::renderMicroTimer
//            );
//        });
        HudRenderCallback.EVENT.register(MicroTimerManager::renderMicroTimer);
    }

    private static void renderMicroTimer(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2 && mod.getBoolConfig("microTimer.enabled") && !(mc.currentScreen instanceof MicroTimerConfigScreen)) {
            int width = mc.getWindow().getScaledWidth();
            int height = mc.getWindow().getScaledHeight();
            int posX = (int)(mod.getFloatConfig("microTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("microTimer.posY")*height);

            String eventTime = MicroTimerManager.eventTickToTime(MicroTimerManager.microTimer);
            String trafficTime = MicroTimerManager.trafficTickToTime(MicroTimerManager.trafficTimer, !MicroTimerManager.currentTraffic.equals("§c§lRED"));
            String doorTime = MicroTimerManager.doorTickToTime(MicroTimerManager.doorTimer);
            int widgetWidth = Math.max(
                    mc.textRenderer.getWidth(String.format("%s%s§r (%s§r)", eventPrefix, lastEvent, eventTime)),
                    Math.max(
                        mc.textRenderer.getWidth(String.format("%s%s§r (%s§r)", trafficPrefix, currentTraffic, trafficTime)),
                        mc.textRenderer.getWidth(String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime))
            ));
            if (mod.getBoolConfig("microTimer.renderBackground")) {
                drawContext.fill(
                        posX,
                        posY,
                        posX + widgetWidth + 8,
                        posY + 21 + mc.textRenderer.fontHeight,
                        0x63000000 // ballin it, worked ig
                );
            }

            // TODO: MOVE CITY TIMERS TO ITS OWN THING
            // fuck mineguy lol
            int cursorY = posY + 4;
            drawContext.drawText(
                    mc.textRenderer, String.format("%s%s§r (%s§r)", eventPrefix, lastEvent, eventTime),
                    posX + 4,
                    cursorY,
                    Colors.WHITE,
                    mod.getBoolConfig("microTimer.textShadow")
            );
            if (mod.currentMap == 1) {
                cursorY +=  mc.textRenderer.fontHeight + 3;
                drawContext.drawText(
                        mc.textRenderer, String.format("%s%s§r (%s§r)", trafficPrefix, currentTraffic, trafficTime),
                        posX + 4,
                        cursorY,
                        Colors.WHITE,
                        mod.getBoolConfig("microTimer.textShadow")
                );
                cursorY +=  mc.textRenderer.fontHeight + 3;
                drawContext.drawText(
                        mc.textRenderer, String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime),
                        posX + 4,
                        cursorY,
                        Colors.WHITE,
                        mod.getBoolConfig("microTimer.textShadow")
                );
            }
        }
    }
}

