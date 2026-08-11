package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.ButtonTimerConfigScreen;

public class ButtonTimerManager {
    public static int buttonTimer = -1;

    public static boolean isMayhem;
    public static boolean isDisabled;
    public static boolean isChaos;
    public static int chaosCounter = 0;

    public static Component tickToTime(int ticks) {
        if (ticks < 0) {
            return Component.nullToEmpty("N/A");
        }

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        String timeString = hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
        MutableComponent timeText = Component.literal(timeString);
        if (ticks >= 230) isChaos = false;

        if (isMayhem) return timeText.withStyle(ChatFormatting.RED);
        if (isChaos) {
            if (ticks >= 140) return timeText.withStyle(ChatFormatting.DARK_PURPLE);
            if (ticks >= 120) return timeText.withStyle(ChatFormatting.LIGHT_PURPLE);
            if (ticks >= 100) return timeText.withStyle(ChatFormatting.DARK_AQUA);
        }
        if (isDisabled) return timeText;

        if (ticks >= 300) return timeText.withStyle(ChatFormatting.RED);
        else if (ticks >= 240) return timeText.withStyle(ChatFormatting.GOLD);
        else if (ticks >= 200) return timeText.withStyle(ChatFormatting.YELLOW);
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

    private static void renderButtonTimer(GuiGraphics drawContext, DeltaTracker renderTickCounter) {
        Minecraft mc = Minecraft.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2 && mod.getBoolConfig("buttonTimer.enabled") && !(mc.screen instanceof ButtonTimerConfigScreen)) {
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            int posX = (int)(mod.getFloatConfig("buttonTimer.posX")*width);
            int posY = (int)(mod.getFloatConfig("buttonTimer.posY")*height);

            Component text = ButtonTimerManager.tickToTime(ButtonTimerManager.buttonTimer);
            int textWidth = mc.font.width(text);
            if (mod.getBoolConfig("buttonTimer.renderBackground")) {
                drawContext.fill(
                        posX,
                        posY,
                        posX + textWidth + 8,
                        posY + 15,
                        0x63000000 // ballin it, worked ig
                );
            }

            drawContext.drawString(
                    mc.font, text,
                    posX + 4,
                    posY + 4,
                    CommonColors.WHITE,
                    mod.getBoolConfig("buttonTimer.textShadow")
            );
        }
    }
}
