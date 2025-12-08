package weebify.dptb2utils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.GuiButtonTimerConfig;

public class ButtonTimerManager {
    public static int buttonTimer = -1;

    public static boolean isMayhem;
    public static boolean isDisabled;
    public static boolean isChaos;
    public static int chaosCounter = 0;

    public static ButtonTimerManager instance;

    public static void initialize() {
        DPTB2Utils.LOGGER.info("Initializing ButtonTimerManager");
        instance = new ButtonTimerManager();
        MinecraftForge.EVENT_BUS.register(instance);
    }

    public static String tickToTime(int ticks) {
        if (ticks < 0) {
            return "N/A";
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeText = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
        if (ticks >= 230) isChaos = false;

        if (isMayhem) return "§c" + timeText;
        if (isChaos) {
            if (ticks >= 140) return "§5" + timeText;
            if (ticks >= 120) return "§d" + timeText;
            if (ticks >= 100) return "§3" + timeText;
        }
        if (isDisabled) return timeText;

        if (ticks >= 300) return "§c" + timeText;
        else if (ticks >= 240) return "§6" + timeText;
        else if (ticks >= 200) return "§e" + timeText;
        return timeText;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (DPTB2Utils.getInstance().isInDPTB2) {
                if (ButtonTimerManager.buttonTimer >= 0) {
                    ButtonTimerManager.buttonTimer += 1;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (event.type == RenderGameOverlayEvent.ElementType.ALL && mod.isInDPTB2 && mod.getBoolConfig("buttonTimer.enabled") && !(mc.currentScreen instanceof GuiButtonTimerConfig)) {
            ScaledResolution scaledRes = new ScaledResolution(mc);
            int width = scaledRes.getScaledWidth();
            int height = scaledRes.getScaledHeight();
            int posX = (int)(mod.getFloatConfig("buttonTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("buttonTimer.posY")*height);

            String text = ButtonTimerManager.tickToTime(ButtonTimerManager.buttonTimer);
            int textWidth = mc.fontRendererObj.getStringWidth(text);
            if (mod.getBoolConfig("buttonTimer.renderBackground")) {
                Gui.drawRect(
                        posX,
                        posY,
                        posX + textWidth + 8,
                        posY + 15,
                        0x63000000 // ballin it, worked ig
                );
            }

            mc.fontRendererObj.drawString(
                    text,
                    posX + 4,
                    posY + 4,
                    0xFFFFFFFF,
                    mod.getBoolConfig("buttonTimer.textShadow")
            );

            NotificationManager.getInstance().render(event.resolution);
        }
    }
}
