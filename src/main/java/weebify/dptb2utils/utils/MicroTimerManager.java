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
    public static String lastEvent = "N/A";
    public static final String prefix = "Last event: ";
    public static String[] eventsList = {
            "§7§lMAYHEM",
            "§f§lDISABLED",
            "§c§lIMMUNITY",
            "§a§lJUMP BOOST",
            "§b§lSLIPPERY ICE",
            "§f§lNOTHING"
    };

    public static Text tickToTime(int ticks) {
        if (ticks < 0) {
            return Text.of("N/A");
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
        MutableText timeText = Text.literal(timeString);

        if (ticks >= 6000) return timeText.formatted(Formatting.RED);
        else if (ticks >= 5100) return timeText.formatted(Formatting.GOLD);
        else if (ticks >= 4200) return timeText.formatted(Formatting.YELLOW);
        return timeText;
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

            Text text = MicroTimerManager.tickToTime(MicroTimerManager.microTimer);
            int widgetWidth = Math.max(mc.textRenderer.getWidth(text), mc.textRenderer.getWidth(Text.of(prefix + lastEvent)));
            if (mod.getBoolConfig("microTimer.renderBackground")) {
                drawContext.fill(
                        posX,
                        posY,
                        posX + widgetWidth + 8,
                        posY + 21 + mc.textRenderer.fontHeight,
                        0x63000000 // ballin it, worked ig
                );
            }

            drawContext.drawText(
                    mc.textRenderer, text,
                    posX + 4,
                    posY + 4,
                    Colors.WHITE,
                    mod.getBoolConfig("microTimer.textShadow")
            );
            drawContext.drawText(
                    mc.textRenderer, prefix + lastEvent,
                    posX + 4,
                    posY + 4 + mc.textRenderer.fontHeight + 3,
                    Colors.WHITE,
                    mod.getBoolConfig("microTimer.textShadow")
            );
        }
    }
}

