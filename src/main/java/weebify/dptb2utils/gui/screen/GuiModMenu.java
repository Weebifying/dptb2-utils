package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import weebify.dptb2utils.DPTB2Utils;

public class GuiModMenu extends GuiScreen {
    private final DPTB2Utils mod;
    private GuiButton checkBtn;
    public GuiModMenu(DPTB2Utils mod) {
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, this.width / 2 - 80 - 75, 75, 150, 20, "Session's Boots List"));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 80 - 75, 75, 150, 20, String.format("AutoCheer: %s", mod.getBoolConfig("others.autoCheer") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 80 - 75, 100, 150, 20, "Notifications Config"));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 80 - 75, 100, 150, 20, "Button Timer HUD"));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 80 - 75, 125, 150, 20, "DPTBot Config"));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 80 - 75, 125, 150, 20, "Item Cooldown HUD"));
        this.buttonList.add(new GuiButton(7, this.width / 2 - 80 - 75, 150, 150, 20, "Micro Event Timer HUD"));

        this.buttonList.add(new GuiButton(999, this.width / 2 - 75, this.height - 30 - 10, 150, 20, I18n.format("gui.done")));
        this.checkBtn = new GuiButton(1000, 30, this.height - 30 - 10, 150, 20, "Rerun DPTB2 Check");
        this.checkBtn.visible = !mod.isInDPTB2;
        this.buttonList.add(this.checkBtn);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        this.mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                this.mc.displayGuiScreen(new GuiBootsList(this, this.mod));
                break;
            case 2:
                button.displayString = String.format("AutoCheer: %s", mod.toggleBoolConfig("others.autoCheer") ? "ON" : "OFF");
                break;
            case 3:
                this.mc.displayGuiScreen(new GuiNotifications(this, this.mod));
                break;
            case 4:
                this.mc.displayGuiScreen(new GuiButtonTimerConfig(this, this.mod));
                break;
            case 5:
                this.mc.displayGuiScreen(new GuiDPTBotConfig(this, this.mod));
                break;
            case 6:
                this.mc.displayGuiScreen(new GuiItemCooldownConfig(this, this.mod));
                break;
            case 7:
                this.mc.displayGuiScreen(new GuiMicroTimerConfig(this, this.mod));
                break;
            case 999:
                this.mc.displayGuiScreen(null);
                break;
            case 1000:
                this.mod.dptb2Check();
                this.checkBtn.enabled = false;
                this.mod.scheduleTask(25, () -> {
                    this.checkBtn.enabled = true;
                    this.checkBtn.visible = !mod.isInDPTB2;
                });
                break;
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "DPTB2 Utils", this.width/2, 20, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, String.format("isInDPTB2: %b", mod.isInDPTB2), this.width/2, this.height - 45 - 10, mod.isInDPTB2 ? 0x55FF55 : 0xFF5555);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}