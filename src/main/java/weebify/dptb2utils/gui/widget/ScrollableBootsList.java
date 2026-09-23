package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.List;
import java.util.stream.Collectors;

public class ScrollableBootsList extends AbstractScrollArea {
    private final List<FormattedCharSequence> lines;
    private final Font textRenderer;
    private final int lineSpacing;
    private final int padding;

    public ScrollableBootsList(int x, int y, int width, int height, int lineHeight, int padding, List<Component> lines, Font textRenderer) {
        super(x, y, width, height, Component.empty());
        this.textRenderer = textRenderer;
        this.lineSpacing = lineHeight;
        this.padding = padding;
        this.lines = lines.stream()
                .flatMap(t -> Minecraft.getInstance().font.split(t, width - SCROLLBAR_WIDTH - 4).stream())
                .collect(Collectors.toList());
    }

    @Override
    protected int contentHeight() {
        return padding * 2
                + lines.size() * (textRenderer.lineHeight + this.lineSpacing);
    }

    @Override
    protected double scrollRate() {
        return textRenderer.lineHeight + lineSpacing;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        drawContent(graphics);
        renderScrollbar(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (updateScrolling(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        // forward scroll wheel
        return super.mouseScrolled(mx, my, 0, dy);
    }

    private void drawContent(GuiGraphics graphics) {
        int startY = getY() + padding;
        int yOffset = startY - (int) scrollAmount();

        for (int i = 0; i < lines.size(); i++) {
            int drawY = yOffset + i * (textRenderer.lineHeight + lineSpacing);
            // only draw visible lines
            if (drawY + textRenderer.lineHeight >= getY()
                    && drawY <= getY() + height) {
                graphics.drawString(
                        textRenderer,
                        lines.get(i),
                        getX() + 2,
                        drawY,
                        CommonColors.WHITE
                );
            }
        }
    }
}
