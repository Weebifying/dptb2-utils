package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import weebify.dptb2utils.DPTB2Utils;

public class GuiNotifications extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    private GuiButton slimeBtn;

    public GuiNotifications(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, width / 2 - 80 - 75, 75, 150, 20, String.format("Shop Update: %s", mod.getBoolConfig("notifs.shopUpdate") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(2, width / 2 + 80 - 75, 75, 150, 20, String.format("City Door Switch:  %s", mod.getBoolConfig("notifs.doorSwitch") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, width / 2 - 80 - 75, 100, 150, 20, String.format("Button Mayhem: %s", mod.getBoolConfig("notifs.buttonMayhem") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(4, width / 2 + 80 - 75, 100, 150, 20, String.format("Button Disabled:  %s", mod.getBoolConfig("notifs.buttonDisable") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(5, width / 2 - 80 - 75, 125, 150, 20, String.format("Button Immunity: %s", mod.getBoolConfig("notifs.buttonImmunity") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(6, width / 2 + 80 - 75, 125, 150, 20, String.format("Boots Tracking:  %s", mod.getBoolConfig("notifs.bootsCollected") ? "ON" : "OFF")));

        this.slimeBtn = new GuiButton(7, width / 2 - 80 - 75, 150, 150, 20, String.format("Slime Boots Notify:  %s", mod.getBoolConfig("notifs.slimeBoots") ? "ON" : "OFF"));
        this.slimeBtn.enabled = mod.getBoolConfig("notifs.bootsCollected");
        this.buttonList.add(this.slimeBtn);

        this.buttonList.add(new GuiButton(999, width / 2 - 80 - 75, height - 30 - 10, 150, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1000, width / 2 + 80 - 75, height - 30 - 10, 150, 20, "Notification Settings"));
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
                button.displayString = String.format("Shop Update: %s", mod.toggleBoolConfig("notifs.shopUpdate") ? "ON" : "OFF");
                break;
            case 2:
                button.displayString = String.format("City Door Switch: %s", mod.toggleBoolConfig("notifs.doorSwitch"));
                break;
            case 3:
                button.displayString = String.format("Button Mayhem: %s", mod.toggleBoolConfig("notifs.buttonMayhem") ? "ON" : "OFF");
                break;
            case 4:
                button.displayString = String.format("Button Disabled: %s", mod.toggleBoolConfig("notifs.buttonDisable") ? "ON" : "OFF");
                break;
            case 5:
                button.displayString = String.format("Button Immunity: %s", mod.toggleBoolConfig("notifs.buttonImmunity") ? "ON" : "OFF");
                break;
            case 6:
                boolean a = mod.toggleBoolConfig("notifs.bootsCollected");
                button.displayString = String.format("Boots Tracking: %s", a ? "ON" : "OFF");
                this.slimeBtn.enabled = a;
                break;
            case 7:
                button.displayString = String.format("Slime Boots Notify: %s", mod.toggleBoolConfig("notifs.slimeBoots") ? "ON" : "OFF");
                break;
            case 999:
                this.mc.displayGuiScreen(this.parent);
                break;
            case 1000:
                this.mc.displayGuiScreen(new GuiNotificationConfig(this, this.mod));
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRendererObj, "Notifications Configs", width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}