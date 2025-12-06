package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.ButtonTimerConfigScreen;

public class ButtonTimerManager {
    public static int buttonTimer = -1;

    public static boolean isMayhem;
    public static boolean isDisabled;
    public static boolean isChaos;
    public static int chaosCounter = 0;

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
        if (ticks >= 230) isChaos = false;

        if (isMayhem) return timeText.formatted(Formatting.RED);
        if (isChaos) {
            if (ticks >= 140) return timeText.formatted(Formatting.DARK_PURPLE);
            if (ticks >= 120) return timeText.formatted(Formatting.LIGHT_PURPLE);
            if (ticks >= 100) return timeText.formatted(Formatting.DARK_AQUA);
        }
        if (isDisabled) return timeText;

        if (ticks >= 300) return timeText.formatted(Formatting.RED);
        else if (ticks >= 240) return timeText.formatted(Formatting.GOLD);
        else if (ticks >= 200) return timeText.formatted(Formatting.YELLOW);
        return timeText;
    }

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (DPTB2Utils.getInstance().isInDPTB2) {
                if (ButtonTimerManager.buttonTimer >= 0) {
                    ButtonTimerManager.buttonTimer += 1;
                }
            }
        });

//        HudLayerRegistrationCallback.EVENT.register((drawer) -> {
//            drawer.attachLayerAfter(
//                    IdentifiedLayer.HOTBAR_AND_BARS,
//                    Identifier.of(DPTB2Utils.MOD_ID, "button_timer"),
//                    ButtonTimerManager::renderButtonTimer
//            );
//        });
        HudRenderCallback.EVENT.register(ButtonTimerManager::renderButtonTimer);
    }

    private static void renderButtonTimer(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2 && mod.getBoolConfig("buttonTimer.enabled") && !(mc.currentScreen instanceof ButtonTimerConfigScreen)) {
            int width = mc.getWindow().getScaledWidth();
            int height = mc.getWindow().getScaledHeight();
            int posX = (int)(mod.getFloatConfig("buttonTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("buttonTimer.posY")*height);

            Text text = ButtonTimerManager.tickToTime(ButtonTimerManager.buttonTimer);
            int textWidth = mc.textRenderer.getWidth(text);
            if (mod.getBoolConfig("buttonTimer.renderBackground")) {
                drawContext.fill(
                        posX,
                        posY,
                        posX + textWidth + 8,
                        posY + 15,
                        0x63000000 // ballin it, worked ig
                );
            }

            drawContext.drawText(
                    mc.textRenderer, text,
                    posX + 4,
                    posY + 4,
                    Colors.WHITE,
                    mod.getBoolConfig("buttonTimer.textShadow")
            );
        }
    }
}
