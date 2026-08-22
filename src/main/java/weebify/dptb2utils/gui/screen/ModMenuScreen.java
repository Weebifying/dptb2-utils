package weebify.dptb2utils.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;

public class ModMenuScreen extends Screen {
    private final DPTB2Utils mod;
    private Button checkBtn;

    public ModMenuScreen(DPTB2Utils mod) {
        super(Component.literal("DPTB2 Utils"));
        this.mod = mod;
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Session's Boots List"), (btn) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new BootsListScreen(this, mod));
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("AutoCheer: %s", mod.getBoolConfig("others.autoCheer") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("AutoCheer: %s", mod.toggleBoolConfig("others.autoCheer") ? "ON" : "OFF")));
        }).bounds(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Notifications Config"), (btn) -> {
            mc.setScreen(new NotificationsScreen(this, mod));
        }).bounds(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Button Timer HUD"), (btn) -> {
            mc.setScreen(new ButtonTimerConfigScreen(this, mod));
        }).bounds(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("DPTBot Config"), (btn) -> {
            mc.setScreen(new DPTBotConfigScreen(this, mod));
        }).bounds(this.width/2 - 80 - 75, 125, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Item Cooldown HUD"), (btn) -> {
            mc.setScreen(new ItemCooldownConfigScreen(this, mod));
        }).bounds(this.width/2 + 80 - 75, 125, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Micro Event Timer HUD"), (btn) -> {
            mc.setScreen(new MicroTimerConfigScreen(this, mod));
        }).bounds(this.width/2 - 80 - 75, 150, 150, 20).build());

//        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Waypoints: %s", mod.getBoolConfig("waypoints.enabled") ? "ON" : "OFF")), (btn) -> {
//            btn.setMessage(Text.of(String.format("Waypoints: %s", mod.toggleBoolConfig("waypoints.enabled") ? "ON" : "OFF")));
//        }).dimensions(this.width/2 - 80 - 75, 150, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            this.onClose();
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
        this.checkBtn = Button.builder(Component.nullToEmpty("Run DPTB2 Check"), (btn) -> {
            try {
                this.mod.dptb2Check(mc);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.checkBtn.active = false;
            this.mod.scheduleTask(25, () -> {
                this.checkBtn.active = true;
//                this.checkBtn.visible = !mod.isInDPTB2;
            });
        }).bounds(30, this.height - 30 - 10, 150, 20).build();
//        this.checkBtn.visible = !mod.isInDPTB2;
        this.addRenderableWidget(this.checkBtn);
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
        graphics.centeredText(this.font, String.format("isInDPTB2: %b", mod.isInDPTB2), this.width/2, this.height - 45 - 10, mod.isInDPTB2 ? 0xFF55FF55 : 0xFFFF5555);
        graphics.centeredText(this.font, String.format("currentMap: %s", DPTB2Utils.MAPS_LIST[mod.currentMap]), this.width/2, this.height - 65 - 10, mod.currentMap == 0 ? 0xFFFF5555 : 0xFFFFFFFF);
    }
}
