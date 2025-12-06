package weebify.dptb2utils.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;
import java.util.stream.Collectors;

public class ScrollableBootsList extends ScrollableWidget {
    private final List<OrderedText> lines;
    private final TextRenderer textRenderer;
    private final int lineSpacing;
    private final int padding;

    public ScrollableBootsList(int x, int y, int width, int height, int lineHeight, int padding, List<Text> lines, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.lineSpacing = lineHeight;
        this.padding = padding;
        this.lines = lines.stream()
                .flatMap(t -> MinecraftClient.getInstance().textRenderer.wrapLines(t, width - SCROLLBAR_WIDTH - 4).stream())
                .collect(Collectors.toList());
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return padding * 2
                + lines.size() * (textRenderer.fontHeight + this.lineSpacing);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return textRenderer.fontHeight + lineSpacing;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawContent(context);
        drawScrollbar(context, mouseX, mouseY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (checkScrollbarDragged(click)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void onRelease(Click click) {
        super.onRelease(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        // forward scroll wheel
        return super.mouseScrolled(mx, my, 0, dy);
    }

    private void drawContent(DrawContext context) {
        int startY = getY() + padding;
        int yOffset = startY - (int) getScrollY();

        for (int i = 0; i < lines.size(); i++) {
            int drawY = yOffset + i * (textRenderer.fontHeight + lineSpacing);
            // only draw visible lines
            if (drawY + textRenderer.fontHeight >= getY()
                    && drawY <= getY() + height) {
                context.drawTextWithShadow(
                        textRenderer,
                        lines.get(i),
                        getX() + 2,
                        drawY,
                        Colors.WHITE
                );
            }
        }
    }
}
