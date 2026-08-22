package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.ScrollableBootsList;

public class BootsListScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public ScrollableBootsList listWidget;

    public BootsListScreen(Screen parent, DPTB2Utils mod) {
        super(Component.nullToEmpty("Boots List"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        listWidget = new ScrollableBootsList(40, 40, this.width-80, this.height-80-30, 3, 5, this.mod.bootsList, this.font);
        this.addRenderableWidget(listWidget);

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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(this.listWidget.getX(), this.listWidget.getY(),
                     this.listWidget.getX() + this.listWidget.getWidth(),
                     this.listWidget.getY() + this.listWidget.getHeight(), 0x33000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width/2, 20, CommonColors.WHITE);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontalAmount, double verticalAmount) {
        if (listWidget.mouseScrolled(x, y, 0, verticalAmount)) return true;
        return super.mouseScrolled(x, y, horizontalAmount, verticalAmount);
    }
}
