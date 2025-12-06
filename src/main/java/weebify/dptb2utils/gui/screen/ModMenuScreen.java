package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class ModMenuScreen extends Screen {
    private final DPTB2Utils mod;
    private ButtonWidget checkBtn;

    public ModMenuScreen(DPTB2Utils mod) {
        super(Text.literal("DPTB2 Utils"));
        this.mod = mod;
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Session's Boots List"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(new BootsListScreen(this, mod));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("AutoCheer: %s", mod.getBoolConfig("others.autoCheer") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("AutoCheer: %s", mod.toggleBoolConfig("others.autoCheer") ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Notifications Config"), (btn) -> {
            mc.setScreen(new NotificationsScreen(this, mod));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Button Timer HUD"), (btn) -> {
            mc.setScreen(new ButtonTimerConfigScreen(this, mod));
        }).dimensions(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("DPTBot Config"), (btn) -> {
            mc.setScreen(new DPTBotConfigScreen(this, mod));
        }).dimensions(this.width/2 - 80 - 75, 125, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Item Cooldown HUD"), (btn) -> {
            mc.setScreen(new ItemCooldownConfigScreen(this, mod));
        }).dimensions(this.width/2 + 80 - 75, 125, 150, 20).build());

//        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Waypoints: %s", mod.getBoolConfig("waypoints.enabled") ? "ON" : "OFF")), (btn) -> {
//            btn.setMessage(Text.of(String.format("Waypoints: %s", mod.toggleBoolConfig("waypoints.enabled") ? "ON" : "OFF")));
//        }).dimensions(this.width/2 - 80 - 75, 150, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            this.close();
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
        this.checkBtn = ButtonWidget.builder(Text.of("Rerun DPTB2 Check"), (btn) -> {
            this.mod.dptb2Check(mc);
            this.checkBtn.active = false;
            this.mod.scheduleTask(25, () -> {
                this.checkBtn.active = true;
                this.checkBtn.visible = !mod.isInDPTB2;
            });
        }).dimensions(30, this.height - 30 - 10, 150, 20).build();
        this.checkBtn.visible = !mod.isInDPTB2;
        this.addDrawableChild(this.checkBtn);
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
        context.drawCenteredTextWithShadow(this.textRenderer, String.format("isInDPTB2: %b", mod.isInDPTB2), this.width/2, this.height - 45 - 10, mod.isInDPTB2 ? 0xFF55FF55 : 0xFFFF5555);
    }
}
