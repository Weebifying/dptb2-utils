package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableButtonTimer;
import weebify.dptb2utils.utils.ButtonTimerManager;

import java.util.Random;


public class ButtonTimerConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableButtonTimer textWidget;

    public ButtonTimerConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("Button Timer HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Enabled: %s", mod.getBoolConfig("buttonTimer.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Enabled: %s", mod.toggleBoolConfig("buttonTimer.enabled") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Text Shadow: %s", mod.getBoolConfig("buttonTimer.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Text Shadow: %s", mod.toggleBoolConfig("buttonTimer.textShadow") ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Render Background: %s", mod.getBoolConfig("buttonTimer.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Render Background: %s", mod.toggleBoolConfig("buttonTimer.renderBackground") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableButtonTimer(
                mod.getFloatConfig("buttonTimer.posX"),
                mod.getFloatConfig("buttonTimer.posY"),
                ButtonTimerManager.tickToTime((!mod.isInDPTB2 || ButtonTimerManager.buttonTimer < 0) ? new Random().nextInt(401) : ButtonTimerManager.buttonTimer)
        );
        this.textWidget.updatePosition(this.width, this.height);
        this.addDrawableChild(this.textWidget);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.mod.setFloatConfig("buttonTimer.posX", this.textWidget.relX);
            this.mod.setFloatConfig("buttonTimer.posY", this.textWidget.relY);
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.setFloatConfig("buttonTimer.posX", this.textWidget.relX);
        this.mod.setFloatConfig("buttonTimer.posY", this.textWidget.relY);
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("(You can drag the timer HUD to move its position on this screen.)"), this.width / 2, 30, Colors.WHITE);
    }
}
