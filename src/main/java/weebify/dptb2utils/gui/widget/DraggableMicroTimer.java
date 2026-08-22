package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.MicroTimerManager;

public class DraggableMicroTimer extends AbstractWidget {
    private boolean dragging = false;
    private String event;
    private int dragOffsetX, dragOffsetY;
    public float relX, relY;

    public DraggableMicroTimer(float relX, float relY, String eventTime, String trafficTime, String doorTime, String event) {
        super(0, 0,
                Math.max(
                        Minecraft.getInstance().font.width(String.format("%s%s§r (%s§r)", MicroTimerManager.eventPrefix, event, eventTime)),
                        Math.max(
                                Minecraft.getInstance().font.width(String.format("%s%s§r (%s§r))", MicroTimerManager.trafficPrefix, MicroTimerManager.LIGHTS_LIST[1], trafficTime)),
                                Minecraft.getInstance().font.width(String.format("%s%s§r (%s§r)", MicroTimerManager.doorPrefix, "N/A", doorTime))
                        )
                ) + 8,
                3 * Minecraft.getInstance().font.lineHeight + 14,
                Component.nullToEmpty(eventTime));
        this.relX = relX;
        this.relY = relY;
        this.event = event;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.setX((int)(screenWidth * relX));
        this.setY((int)(screenHeight * relY));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Draw centered text manually
        Font renderer = Minecraft.getInstance().font;
        DPTB2Utils mod = DPTB2Utils.getInstance();
        if (mod.getBoolConfig("microTimer.renderBackground")) {
            graphics.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    0x63000000
            );
        }

        int cursorY = getY() + 4;

        // Event Line
        graphics.text(
                renderer, String.format("%s00:00 (%s)", MicroTimerManager.eventPrefix, this.event),
                getX() + 4,
                cursorY,
                CommonColors.WHITE,
                mod.getBoolConfig("microTimer.textShadow")
        );

        cursorY += renderer.lineHeight + 3;
        graphics.text(
                renderer, String.format("%s00:00 (%s)", MicroTimerManager.trafficPrefix, MicroTimerManager.LIGHTS_LIST[0]),
                getX() + 4,
                cursorY,
                CommonColors.WHITE,
                mod.getBoolConfig("microTimer.textShadow")
        );

        cursorY += renderer.lineHeight + 3;
        graphics.text(
                renderer, String.format("%s00:00", MicroTimerManager.doorPrefix),
                getX() + 4,
                cursorY,
                CommonColors.WHITE,
                mod.getBoolConfig("microTimer.textShadow")
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
