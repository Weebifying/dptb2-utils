package weebify.dptb2utils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.GuiMicroTimerConfig;

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
    public static String[] eventsList = {
            "§7§lMAYHEM",
            "§f§lDISABLED",
            "§c§lIMMUNITY",
            "§a§lJUMP BOOST",
            "§b§lSLIPPERY ICE",
            "§f§lNOTHING"
    };
    public static String[] lightsList = {
            "§a§lGREEN",
            "§c§lRED"
    };

    public static MicroTimerManager instance;

    public static void initialize() {
        DPTB2Utils.LOGGER.info("Initializing MicroTimerManager");
        instance = new MicroTimerManager();
        MinecraftForge.EVENT_BUS.register(instance);
    }

    public static String eventTickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (ticks >= 6000) return "§c" + timeText;
        else if (ticks >= 5100) return "§6" + timeText;
        else if (ticks >= 4200) return "§e" + timeText;
        return timeText;
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

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (!isGreen) return "§c" + timeText;

        if (ticks <= 160) return "§6" + timeText;
        if (ticks <= 320) return "§e" + timeText;
        return timeText;
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

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (ticks >= 12000) return "§c" + timeText;
        else if (ticks >= 11100) return "§6" + timeText;
        else if (ticks >= 10200) return "§e" + timeText;
        return timeText;
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

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);

        if (ticks <= 60) return "§c" + timeText;
        if (ticks <= 100) return "§6" + timeText;
        if (ticks <= 200) return "§e" + timeText;
        return timeText;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
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
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (event.type == RenderGameOverlayEvent.ElementType.ALL && mod.isInDPTB2 && mod.getBoolConfig("microTimer.enabled") && !(mc.currentScreen instanceof GuiMicroTimerConfig)) {
            ScaledResolution scaledRes = new ScaledResolution(mc);
            int width = scaledRes.getScaledWidth();
            int height = scaledRes.getScaledHeight();
            int posX = (int)(mod.getFloatConfig("microTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("microTimer.posY")*height);

            String eventTime = MicroTimerManager.eventTickToTime(MicroTimerManager.eventTimer);
            String trafficTime = MicroTimerManager.trafficTickToTime(MicroTimerManager.trafficTimer, !MicroTimerManager.currentTraffic.equals("§c§lRED"));
            String doorTime = MicroTimerManager.doorTickToTime(MicroTimerManager.doorTimer);
            String blessingTime = MicroTimerManager.blessingTickToTime(MicroTimerManager.blessingTimer);

            boolean showTrafficDoor = mod.currentMap == 2;

            String eventLine = String.format("%s%s§r (%s§r)", eventPrefix, lastEvent, eventTime);
            String blessingLine = String.format("%s%s", blessingPrefix, blessingTime);
            String trafficLine = String.format("%s%s§r (%s§r)", trafficPrefix, currentTraffic, trafficTime);
            String doorLine = String.format("%s%s§r (%s§r)", doorPrefix, currentDoor, doorTime);

            int widgetWidth = Math.max(mc.fontRendererObj.getStringWidth(eventLine), mc.fontRendererObj.getStringWidth(blessingLine));
            if (showTrafficDoor) {
                widgetWidth = Math.max(widgetWidth, Math.max(mc.fontRendererObj.getStringWidth(trafficLine), mc.fontRendererObj.getStringWidth(doorLine)));
            }

            int lineCount = 2 + (showTrafficDoor ? 2 : 0);
            int widgetHeight = lineCount * (mc.fontRendererObj.FONT_HEIGHT + 3) + 5;

            if (mod.getBoolConfig("microTimer.renderBackground")) {
                Gui.drawRect(
                        posX,
                        posY,
                        posX + widgetWidth + 8,
                        posY + widgetHeight,
                        0x63000000 // ballin it, worked ig
                );
            }

            int cursorY = posY + 4;
            mc.fontRendererObj.drawString(
                    eventLine,
                    posX + 4,
                    cursorY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
            mc.fontRendererObj.drawString(
                    blessingLine,
                    posX + 4,
                    cursorY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            if (showTrafficDoor) {
                cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
                mc.fontRendererObj.drawString(
                        trafficLine,
                        posX + 4,
                        cursorY,
                        0xFFFFFFFF,
                        mod.getBoolConfig("microTimer.textShadow")
                );

                cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
                mc.fontRendererObj.drawString(
                        doorLine,
                        posX + 4,
                        cursorY,
                        0xFFFFFFFF,
                        mod.getBoolConfig("microTimer.textShadow")
                );
            }

            NotificationManager.getInstance().render(event.resolution);
        }
    }
}
