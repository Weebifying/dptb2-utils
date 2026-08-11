package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;

public class DraggableButtonTimer extends AbstractWidget {
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    public float relX, relY;

    public DraggableButtonTimer(float relX, float relY, Component message) {
        super(0, 0, Minecraft.getInstance().font.width(message) + 8, 15, message);
        this.relX = relX;
        this.relY = relY;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.setX((int)(screenWidth * relX));
        this.setY((int)(screenHeight * relY));
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Draw centered text manually
        Font renderer = Minecraft.getInstance().font;
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
        context.drawString(
                renderer, getMessage(),
                getX() + 4,
                getY() + 4,
                CommonColors.WHITE,
                mod.getBoolConfig("buttonTimer.textShadow")
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (this.isMouseOver(mouseX, mouseY) && button == 0) {
            dragging = true;
            dragOffsetX = (int)(mouseX - this.getX());
            dragOffsetY = (int)(mouseY - this.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        int button = click.button();
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (dragging) {
            Minecraft client = Minecraft.getInstance();
            int newX = (int)(mouseX - dragOffsetX);
            int newY = (int)(mouseY - dragOffsetY);
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();

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
    protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
