package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableItemCooldown;
import weebify.dptb2utils.utils.ItemCooldownManager;

import java.util.List;
import java.util.Map;

// placeholder
public class ItemCooldownConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableItemCooldown textWidget;
    private final List<String> alignOptions = List.of("left", "right");

    public ItemCooldownConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("Item Cooldown HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Enabled: %s", mod.getBoolConfig("itemCooldown.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Enabled: %s", mod.toggleBoolConfig("itemCooldown.enabled") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Text Shadow: %s", mod.getBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Text Shadow: %s", mod.toggleBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF")));
        }).bounds(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Render Background: %s", mod.getBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Render Background: %s", mod.toggleBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Text Alignment: %s", mod.getStringConfig("itemCooldown.textAlign"))), (btn) -> {
            String next = alignOptions.get((alignOptions.indexOf(mod.getStringConfig("itemCooldown.textAlign")) + 1) % alignOptions.size());
            mod.setStringConfig("itemCooldown.textAlign", next);
            btn.setMessage(Component.nullToEmpty(String.format("Text Alignment: %s", next)));
        }).bounds(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableItemCooldown(
                mod.getFloatConfig("itemCooldown.posX"),
                mod.getFloatConfig("itemCooldown.posY"),
                ItemCooldownManager.generateRandomCooldowns()
        );
        this.textWidget.updatePosition(this.width,  this.height);
        this.addRenderableWidget(this.textWidget);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
            this.mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
        this.mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
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
