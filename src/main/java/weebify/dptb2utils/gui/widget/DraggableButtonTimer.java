package weebify.dptb2utils.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class DraggableButtonTimer extends ClickableWidget {
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    public float relX, relY;

    public DraggableButtonTimer(float relX, float relY, Text message) {
        super(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(message) + 8, 15, message);
        this.relX = relX;
        this.relY = relY;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.setX((int)(screenWidth * relX));
        this.setY((int)(screenHeight * relY));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw centered text manually
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        DPTB2Utils mod = DPTB2Utils.getInstance();
        if (mod.getBoolConfig("buttonTimer.renderBackground")) {
            context.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    0x63000000
            );
        }
        context.drawText(
                renderer, getMessage(),
                getX() + 4,
                getY() + 4,
                Colors.WHITE,
                mod.getBoolConfig("buttonTimer.textShadow")
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == 0) {
            dragging = true;
            dragOffsetX = (int)(mouseX - this.getX());
            dragOffsetY = (int)(mouseY - this.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            MinecraftClient client = MinecraftClient.getInstance();
            int newX = (int)(mouseX - dragOffsetX);
            int newY = (int)(mouseY - dragOffsetY);
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Clamp to screen and update
            this.setX(newX);
            this.setY(newY);
            relX = (float)newX / screenWidth;
            relY = (float)newY / screenHeight;
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
