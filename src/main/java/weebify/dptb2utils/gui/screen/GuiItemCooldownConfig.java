package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableItemCooldown;
import weebify.dptb2utils.utils.ButtonTimerManager;
import weebify.dptb2utils.utils.ItemCooldownManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GuiItemCooldownConfig extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    public DraggableItemCooldown textWidget;
    private final List<String> alignOptions = Arrays.asList("left", "right");

    public GuiItemCooldownConfig(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, width / 2 - 80 - 75, height / 2 - 100 - 10, 150, 20, String.format("Enabled: %s", mod.getBoolConfig("itemCooldown.enabled") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(2, width / 2 + 80 - 75, height / 2 - 100 - 10, 150, 20, String.format("Text Shadow: %s", mod.getBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, width / 2 - 80 - 75, height / 2 - 75 - 10, 150, 20, String.format("Render Background: %s", mod.getBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(4, width / 2 + 80 - 75, height / 2 - 75 - 10, 150, 20, String.format("Text Alignment: %s", mod.getStringConfig("itemCooldown.textAlign"))));
        this.textWidget = new DraggableItemCooldown(
                mod.getFloatConfig("itemCooldown.posX"),
                mod.getFloatConfig("itemCooldown.posY"),
                ItemCooldownManager.generateRandomCooldowns()
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
        mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
        mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
        mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                button.displayString = String.format("Enabled: %s", mod.toggleBoolConfig("itemCooldown.enabled") ? "ON" : "OFF");
                break;
            case 2:
                button.displayString = String.format("Text Shadow: %s", mod.toggleBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF");
                break;
            case 3:
                button.displayString = String.format("Render Background: %s", mod.toggleBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF");
                break;
            case 4:
                String next = alignOptions.get((alignOptions.indexOf(mod.getStringConfig("itemCooldown.textAlign")) + 1) % alignOptions.size());
                mod.setStringConfig("itemCooldown.textAlign", next);
                button.displayString = String.format("Text Alignment: %s", next);
                break;
            case 999:
                mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
                mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
                this.mc.displayGuiScreen(this.parent);
                break;
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRendererObj, "Item Cooldown HUD Config", width / 2, 20, 0xFFFFFF);
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
