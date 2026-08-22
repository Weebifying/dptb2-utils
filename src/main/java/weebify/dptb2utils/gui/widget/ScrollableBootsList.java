package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(10));
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawContent(graphics);
        extractScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (updateScrolling(click)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void onRelease(MouseButtonEvent click) {
        super.onRelease(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        // forward scroll wheel
        return super.mouseScrolled(mx, my, 0, dy);
    }

    private void drawContent(GuiGraphicsExtractor graphics) {
        int startY = getY() + padding;
        int yOffset = startY - (int) scrollAmount();

        for (int i = 0; i < lines.size(); i++) {
            int drawY = yOffset + i * (textRenderer.lineHeight + lineSpacing);
            // only draw visible lines
            if (drawY + textRenderer.lineHeight >= getY()
                    && drawY <= getY() + height) {
                graphics.text(
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
