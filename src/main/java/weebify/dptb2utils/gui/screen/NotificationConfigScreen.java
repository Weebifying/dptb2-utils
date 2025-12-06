package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class NotificationConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;

    public NotificationConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("Notification Settings"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Don't Delay Sounds: %s", mod.getBoolConfig("notifs.dontDelaySfx") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Don't Delay Sounds: %s", mod.toggleBoolConfig("notifs.dontDelaySfx") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
    }
}
