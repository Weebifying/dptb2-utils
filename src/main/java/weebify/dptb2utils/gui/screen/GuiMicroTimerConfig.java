package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableMicroTimer;
import weebify.dptb2utils.utils.MicroTimerManager;

import java.io.IOException;
import java.util.Random;

public class GuiMicroTimerConfig extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    public DraggableMicroTimer textWidget;

    public GuiMicroTimerConfig(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, width / 2 - 80 - 75, height / 2 - 100 - 10, 150, 20, String.format("Enabled: %s", mod.getBoolConfig("microTimer.enabled") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(2, width / 2 + 80 - 75, height / 2 - 100 - 10, 150, 20, String.format("Text Shadow: %s", mod.getBoolConfig("microTimer.textShadow") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, width / 2 - 80 - 75, height / 2 - 75 - 10, 150, 20, String.format("Render Background: %s", mod.getBoolConfig("microTimer.renderBackground") ? "ON" : "OFF")));
        this.textWidget = new DraggableMicroTimer(
                mod.getFloatConfig("microTimer.posX"),
                mod.getFloatConfig("microTimer.posY"),
                MicroTimerManager.tickToTime(this.mod.isInDPTB2 && MicroTimerManager.microTimer >= 0 ? MicroTimerManager.microTimer : (new Random()).nextInt(8401)),
                (!mod.isInDPTB2 || MicroTimerManager.lastEvent.isEmpty()) ? MicroTimerManager.eventsList[new Random().nextInt(MicroTimerManager.eventsList.length)] : MicroTimerManager.lastEvent
        );
        this.textWidget.updatePosition(width, height);
        this.buttonList.add(this.textWidget);
        this.buttonList.add(new GuiButton(999, width / 2 - 75, height - 30 - 10, 150, 20, I18n.format("gui.done")));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
        mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
        mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                button.displayString = String.format("Enabled: %s", mod.toggleBoolConfig("microTimer.enabled") ? "ON" : "OFF");
                break;
            case 2:
                button.displayString = String.format("Text Shadow: %s", mod.toggleBoolConfig("microTimer.textShadow") ? "ON" : "OFF");
                break;
            case 3:
                button.displayString = String.format("Render Background: %s", mod.toggleBoolConfig("microTimer.renderBackground") ? "ON" : "OFF");
                break;
            case 999:
                mod.setFloatConfig("microTimer.posX", this.textWidget.relX);
                mod.setFloatConfig("microTimer.posY", this.textWidget.relY);
                this.mc.displayGuiScreen(this.parent);
                break;
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRendererObj, "Micro Event Timer HUD Config", width / 2, 20, 0xFFFFFF);
        this.drawCenteredString(fontRendererObj, "(You can drag the timer HUD to move its position on this screen.)", width / 2, 30, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.textWidget.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.textWidget.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.textWidget.mouseDragged(mouseX, mouseY);
    }
}
