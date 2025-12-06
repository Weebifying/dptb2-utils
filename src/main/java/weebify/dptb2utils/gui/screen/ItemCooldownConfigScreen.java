package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
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
        super(Text.literal("Item Cooldown HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Enabled: %s", mod.getBoolConfig("itemCooldown.enabled") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Enabled: %s", mod.toggleBoolConfig("itemCooldown.enabled") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Text Shadow: %s", mod.getBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Text Shadow: %s", mod.toggleBoolConfig("itemCooldown.textShadow") ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Render Background: %s", mod.getBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Render Background: %s", mod.toggleBoolConfig("itemCooldown.renderBackground") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Text Alignment: %s", mod.getStringConfig("itemCooldown.textAlign"))), (btn) -> {
            String next = alignOptions.get((alignOptions.indexOf(mod.getStringConfig("itemCooldown.textAlign")) + 1) % alignOptions.size());
            mod.setStringConfig("itemCooldown.textAlign", next);
            btn.setMessage(Text.of(String.format("Text Alignment: %s", next)));
        }).dimensions(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableItemCooldown(
                mod.getFloatConfig("itemCooldown.posX"),
                mod.getFloatConfig("itemCooldown.posY"),
                ItemCooldownManager.generateRandomCooldowns()
        );
        this.textWidget.updatePosition(this.width,  this.height);
        this.addDrawableChild(this.textWidget);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
            this.mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.setFloatConfig("itemCooldown.posX", this.textWidget.relX);
        this.mod.setFloatConfig("itemCooldown.posY", this.textWidget.relY);
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("(You can drag the timer HUD to move its position on this screen.)"), this.width / 2, 30, Colors.WHITE);
    }
}
