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
    public static int microTimer = -1;
    public static String lastEvent = "N/A";
    public static final String prefix = "Last event: ";
    public static String[] eventsList = {
            "§4§lMAYHEM",
            "§b§lDISABLED",
            "§c§lIMMUNITY",
            "§a§lJUMP BOOST",
            "§e§lSPEED"
    };

    public static MicroTimerManager instance;

    public static void initialize() {
        DPTB2Utils.LOGGER.info("Initializing MicroTimerManager");
        instance = new MicroTimerManager();
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

        if (ticks >= 6000) return "§c" + timeText;
        else if (ticks >= 5100) return "§6" + timeText;
        else if (ticks >= 4200) return "§e" + timeText;
        return timeText;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (DPTB2Utils.getInstance().isInDPTB2) {
                if (MicroTimerManager.microTimer >= 0) {
                    MicroTimerManager.microTimer += 1;
                }
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

            String text = MicroTimerManager.tickToTime(MicroTimerManager.microTimer);
            int textWidth = Math.max(mc.fontRendererObj.getStringWidth(text), mc.fontRendererObj.getStringWidth(prefix + lastEvent));
            if (mod.getBoolConfig("microTimer.renderBackground")) {
                Gui.drawRect(
                        posX,
                        posY,
                        posX + textWidth + 8,
                        posY + 21 + mc.fontRendererObj.FONT_HEIGHT,
                        0x63000000 // ballin it, worked ig
                );
            }

            mc.fontRendererObj.drawString(
                    text,
                    posX + 4,
                    posY + 4,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );
            mc.fontRendererObj.drawString(
                    prefix + lastEvent,
                    posX + 4,
                    posY + 4 + mc.fontRendererObj.FONT_HEIGHT + 3,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            NotificationManager.getInstance().render(event.resolution);
        }
    }
}
