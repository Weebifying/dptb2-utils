package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;

public class NotificationsScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;

    protected NotificationsScreen(Screen parent, DPTB2Utils mod) {
        super(Component.nullToEmpty("Notifications Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Shop Update: %s", mod.getBoolConfig("notifs.shopUpdate") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Shop Update: %s", mod.toggleBoolConfig("notifs.shopUpdate") ? "ON" : "OFF")));
        }).bounds(this.width / 2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("City Door Switch: %s", mod.getBoolConfig("notifs.doorSwitch") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("City Door Switch: %s", mod.toggleBoolConfig("notifs.doorSwitch") ? "ON" : "OFF")));
        }).bounds(this.width / 2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Button Mayhem: %s", mod.getBoolConfig("notifs.buttonMayhem") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Button Mayhem: %s", mod.toggleBoolConfig("notifs.buttonMayhem") ? "ON" : "OFF")));
        }).bounds(this.width / 2 - 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Button Disabled: %s", mod.getBoolConfig("notifs.buttonDisable") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Button Disabled: %s", mod.toggleBoolConfig("notifs.buttonDisable") ? "ON" : "OFF")));
        }).bounds(this.width / 2 + 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Button Immunity: %s", mod.getBoolConfig("notifs.buttonImmunity") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Button Immunity: %s", mod.toggleBoolConfig("notifs.buttonImmunity") ? "ON" : "OFF")));
        }).bounds(this.width / 2 - 80 - 75, 125, 150, 20).build());

        Button slimeBtn = Button.builder(Component.nullToEmpty(String.format("Slime Boots Notify: %s", mod.getBoolConfig("notifs.slimeBoots") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Slime Boots Notify: %s", mod.toggleBoolConfig("notifs.slimeBoots") ? "ON" : "OFF")));
        }).bounds(this.width / 2 - 80 - 75, 150, 150, 20).build();
        slimeBtn.active = mod.getBoolConfig("notifs.bootsCollected");
        this.addRenderableWidget(slimeBtn);

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Boots Tracking: %s", mod.getBoolConfig("notifs.bootsCollected") ? "ON" : "OFF")), (btn) -> {
            boolean a = mod.toggleBoolConfig("notifs.bootsCollected");
            btn.setMessage(Component.nullToEmpty(String.format("Boots Tracking: %s", a ? "ON" : "OFF")));
            slimeBtn.active = a;
        }).bounds(this.width / 2 + 80 - 75, 125, 150, 20).build());


        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 80 - 75, this.height - 30 - 10, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Notification Settings"), (btn) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new NotificationConfigScreen(this, mod));
        }).bounds(this.width / 2 + 80 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.mod.saveSettings();
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width/2, 20, CommonColors.WHITE);
    }
}
