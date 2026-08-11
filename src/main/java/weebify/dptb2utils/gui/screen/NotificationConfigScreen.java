package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;

public class NotificationConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;

    public NotificationConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("Notification Settings"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Don't Delay Sounds: %s", mod.getBoolConfig("notifs.dontDelaySfx") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Don't Delay Sounds: %s", mod.toggleBoolConfig("notifs.dontDelaySfx") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());


        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width/2, 20, CommonColors.WHITE);
    }
}
