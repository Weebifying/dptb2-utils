package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.MicroTimerManager;

public class DraggableMicroTimer extends GuiButton {
    private boolean dragging = false;
    private final String event;
    private final String eventTime, trafficTime, doorTime, blessingTime;
    private int dragOffsetX, dragOffsetY;
    public float relX, relY;

    public DraggableMicroTimer(float relX, float relY, String eventTime, String trafficTime, String doorTime, String blessingTime, String event) {
        super(-1, 0, 0, computeWidth(event, eventTime, trafficTime, doorTime, blessingTime), computeHeight(), eventTime);
        this.relX = relX;
        this.relY = relY;
        this.event = event;
        this.eventTime = eventTime;
        this.trafficTime = trafficTime;
        this.doorTime = doorTime;
        this.blessingTime = blessingTime;
    }

    private static int computeWidth(String event, String eventTime, String trafficTime, String doorTime, String blessingTime) {
        net.minecraft.client.gui.FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        return Math.max(
                Math.max(
                        font.getStringWidth(String.format("%s%s§r (%s§r)", MicroTimerManager.eventPrefix, event, eventTime)),
                        font.getStringWidth(String.format("%s%s§r (%s§r)", MicroTimerManager.trafficPrefix, MicroTimerManager.lightsList[1], trafficTime))
                ),
                Math.max(
                        font.getStringWidth(String.format("%s%s§r (%s§r)", MicroTimerManager.doorPrefix, "N/A", doorTime)),
                        font.getStringWidth(String.format("%s%s", MicroTimerManager.blessingPrefix, blessingTime))
                )
        ) + 8;
    }

    private static int computeHeight() {
        int lineCount = 4;
        return lineCount * (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 3) + 5;
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

            int cursorY = this.yPosition + 4;

            mc.fontRendererObj.drawString(
                    String.format("%s%s§r (%s§r)", MicroTimerManager.eventPrefix, this.event, this.eventTime),
                    this.xPosition + 4,
                    cursorY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
            mc.fontRendererObj.drawString(
                    String.format("%s%s", MicroTimerManager.blessingPrefix, this.blessingTime),
                    this.xPosition + 4,
                    cursorY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
            mc.fontRendererObj.drawString(
                    String.format("%s%s§r (%s§r)", MicroTimerManager.trafficPrefix, MicroTimerManager.lightsList[0], this.trafficTime),
                    this.xPosition + 4,
                    cursorY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("microTimer.textShadow")
            );

            cursorY += mc.fontRendererObj.FONT_HEIGHT + 3;
            mc.fontRendererObj.drawString(
                    String.format("%s%s§r (%s§r)", MicroTimerManager.doorPrefix, "N/A", this.doorTime),
                    this.xPosition + 4,
                    cursorY,
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
