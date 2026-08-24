package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableMicroTimer;
import weebify.dptb2utils.utils.MicroTimerManager;

import java.util.Random;

public class MicroTimerConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableMicroTimer textWidget;


    public MicroTimerConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("Micro Event Timer HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Enabled: %s", mod.getBoolConfig("microTimer.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Enabled: %s", mod.toggleBoolConfig("microTimer.enabled") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Text Shadow: %s", mod.getBoolConfig("microTimer.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Text Shadow: %s", mod.toggleBoolConfig("microTimer.textShadow") ? "ON" : "OFF")));
        }).bounds(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Render Background: %s", mod.getBoolConfig("microTimer.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Render Background: %s", mod.toggleBoolConfig("microTimer.renderBackground") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 100, 150, 20).build());

        Random r = new Random();

        this.textWidget = new DraggableMicroTimer(
                mod.getFloatConfig("microTimer.posX"),
                mod.getFloatConfig("microTimer.posY"),
                MicroTimerManager.eventTickToTime((!mod.isInDPTB2 || MicroTimerManager.eventTimer < 0) ? r.nextInt(7201) : MicroTimerManager.eventTimer),
                MicroTimerManager.trafficTickToTime((!mod.isInDPTB2 || MicroTimerManager.trafficTimer < 0) ? r.nextInt(13201) : MicroTimerManager.trafficTimer, true),
                MicroTimerManager.doorTickToTime((!mod.isInDPTB2 || MicroTimerManager.doorTimer < 0) ? r.nextInt(4801) : MicroTimerManager.doorTimer),
                MicroTimerManager.blessingTickToTime((!mod.isInDPTB2 || MicroTimerManager.blessingTimer < 0) ? r.nextInt(201) : MicroTimerManager.blessingTimer),
                (!mod.isInDPTB2 || MicroTimerManager.lastEvent.isBlank()) ? MicroTimerManager.EVENTS_LIST[new Random().nextInt(0, MicroTimerManager.EVENTS_LIST.length)] : MicroTimerManager.lastEvent
        );
        this.textWidget.updatePosition(this.width, this.height);
        this.addRenderableWidget(this.textWidget);


        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
            this.mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
        this.mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
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