package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.MicroTimerManager;

public class DraggableMicroTimer extends GuiButton {
    private boolean dragging = false;
    private String event;
    private int dragOffsetX, dragOffsetY;
    public float relX, relY;

    public DraggableMicroTimer(float relX, float relY, String text, String event) {
        super(-1, 0, 0, Math.max(Minecraft.getMinecraft().fontRendererObj.getStringWidth(text), Minecraft.getMinecraft().fontRendererObj.getStringWidth(MicroTimerManager.prefix + event)) + 8, 21 + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, text);
        this.relX = relX;
        this.relY = relY;
        this.event = event;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.xPosition = (int) (screenWidth * relX);
        this.yPosition = (int) (screenHeight * relY);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            DPTB2Utils mod = DPTB2Utils.getInstance();

            if (mod.getBoolConfig("microTimer.renderBackground")) {
                drawRect(
                        this.xPosition, this.yPosition,
                        this.xPosition + this.width,
                        this.yPosition + this.height,
                        0x63000000
                );
            }

            mc.fontRendererObj.drawString(
                    this.displayString,
                    this.xPosition + 4,
                    this.yPosition + 4,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            mc.fontRendererObj.drawString(
                    MicroTimerManager.prefix + this.event,
                    this.xPosition + 4,
                    this.yPosition + 4 + mc.fontRendererObj.FONT_HEIGHT + 3,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 &&
                mouseX >= xPosition && mouseX < xPosition + width &&
                mouseY >= yPosition && mouseY < yPosition + height) {
            dragging = true;
            dragOffsetX = mouseX - this.xPosition;
            dragOffsetY = mouseY - this.yPosition;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (dragging && mouseButton == 0) {
            dragging = false;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(int mouseX, int mouseY) {
        if (dragging) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();

            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;

            // Clamp inside screen
            newX = Math.max(0, Math.min(screenWidth - this.width, newX));
            newY = Math.max(0, Math.min(screenHeight - this.height, newY));

            this.xPosition = newX;
            this.yPosition = newY;

            relX = (float) newX / screenWidth;
            relY = (float) newY / screenHeight;
            return true;
        }
        return false;
    }
}
