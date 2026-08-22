package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableButtonTimer;
import weebify.dptb2utils.utils.ButtonTimerManager;

import java.util.Random;


public class ButtonTimerConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableButtonTimer textWidget;

    public ButtonTimerConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("Button Timer HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Enabled: %s", mod.getBoolConfig("buttonTimer.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Enabled: %s", mod.toggleBoolConfig("buttonTimer.enabled") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Text Shadow: %s", mod.getBoolConfig("buttonTimer.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Text Shadow: %s", mod.toggleBoolConfig("buttonTimer.textShadow") ? "ON" : "OFF")));
        }).bounds(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Render Background: %s", mod.getBoolConfig("buttonTimer.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Render Background: %s", mod.toggleBoolConfig("buttonTimer.renderBackground") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableButtonTimer(
                mod.getFloatConfig("buttonTimer.posX"),
                mod.getFloatConfig("buttonTimer.posY"),
                ButtonTimerManager.tickToTime((!mod.isInDPTB2 || ButtonTimerManager.buttonTimer < 0) ? new Random().nextInt(401) : ButtonTimerManager.buttonTimer)
        );
        this.textWidget.updatePosition(this.width, this.height);
        this.addRenderableWidget(this.textWidget);


        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.mod.setFloatConfig("buttonTimer.posX", this.textWidget.relX);
            this.mod.setFloatConfig("buttonTimer.posY", this.textWidget.relY);
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.mod.setFloatConfig("buttonTimer.posX", this.textWidget.relX);
        this.mod.setFloatConfig("buttonTimer.posY", this.textWidget.relY);
        this.mod.saveSettings();
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width/2, 20, CommonColors.WHITE);
        graphics.centeredText(this.font, Component.literal("(You can drag the timer HUD to move its position on this screen.)"), this.width / 2, 30, CommonColors.WHITE);
    }
}
