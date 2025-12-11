package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableMicroTimer;
import weebify.dptb2utils.utils.MicroTimerManager;

import java.util.Random;

public class MicroTimerConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableMicroTimer textWidget;


    public MicroTimerConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("Micro Event Timer HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Enabled: %s", mod.getBoolConfig("microTimer.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Enabled: %s", mod.toggleBoolConfig("microTimer.enabled") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Text Shadow: %s", mod.getBoolConfig("microTimer.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Text Shadow: %s", mod.toggleBoolConfig("microTimer.textShadow") ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Render Background: %s", mod.getBoolConfig("microTimer.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Render Background: %s", mod.toggleBoolConfig("microTimer.renderBackground") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableMicroTimer(
                mod.getFloatConfig("microTimer.posX"),
                mod.getFloatConfig("microTimer.posY"),
                MicroTimerManager.tickToTime((!mod.isInDPTB2 || MicroTimerManager.microTimer < 0) ? new Random().nextInt(8401) : MicroTimerManager.microTimer),
                (!mod.isInDPTB2 || MicroTimerManager.lastEvent.isBlank()) ? MicroTimerManager.eventsList[new Random().nextInt(0, MicroTimerManager.eventsList.length)] : MicroTimerManager.lastEvent
        );
        this.textWidget.updatePosition(this.width, this.height);
        this.addDrawableChild(this.textWidget);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
            this.mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
        this.mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
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
