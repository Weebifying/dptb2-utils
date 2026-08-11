package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.ItemCooldownManager;

import java.util.Map;

public class DraggableItemCooldown extends AbstractWidget {
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private final Map<String, Integer> itemCooldowns;
    public float relX, relY;

    private static final int padding = 5;
    private static final int lineHeight = 20;

    public DraggableItemCooldown(float relX, float relY, Map<String, Integer> itemCooldowns) {
        super(0, 0, 0, 0, Component.nullToEmpty(""));
        int maxWidth = 0;
        for (String itemName : itemCooldowns.keySet()) {
            int ticksLeft = itemCooldowns.get(itemName);
            ItemCooldownManager.Items item = ItemCooldownManager.Items.NAME_MAP.get(itemName);
            int barWidth = (int) (0.2 * item.cooldown);
            int textWidth = Minecraft.getInstance().font.width((ticksLeft / 20) + "s");
            maxWidth = Math.max(maxWidth, padding + 20 + barWidth + 6 + textWidth + padding);
        }
        int totalHeight = itemCooldowns.size() * lineHeight + padding;

        this.setWidth(maxWidth);
        this.setHeight(totalHeight);
        this.relX = relX;
        this.relY = relY;
        this.itemCooldowns = itemCooldowns;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.setX((int)(screenWidth * relX));
        this.setY((int)(screenHeight * relY));
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");

        if (mod.getBoolConfig("itemCooldown.renderBackground")) {
            context.fill(
                    alignLeft ? getX() : getX() - getWidth(),
                    getY(),
                    alignLeft ? getX() + getWidth() : getX(),
                    getY() + getHeight(),
                    0x63000000
            );
        }

        int i = 0;
        for (Map.Entry<String, Integer> entry : itemCooldowns.entrySet()) {
            String itemName = entry.getKey();
            int ticksLeft = entry.getValue();
            ItemCooldownManager.Items item = ItemCooldownManager.Items.NAME_MAP.get(itemName);

            int x = alignLeft ? getX() + padding : getX() - padding - 16;
            int y = getY() + padding + i * lineHeight;
            context.blit(RenderPipelines.GUI_TEXTURED, item.texture, x, y, 0, 0, 16, 16, 16, 16);

            int barWidth = (int) (0.2 * item.cooldown);
            int barHeight = 8;
            int barX = alignLeft ? x + 20 : x - 4 - barWidth;
            int barY = y + 4;
            int total = item.cooldown;
            float progress = (float)ticksLeft / total;
            int filled = (int)(barWidth * progress);

            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            if (alignLeft) context.fill(barX, barY, barX + filled, barY + barHeight, lerpColor(0xFF55FF55, 0xFFFF5555, progress));
            else context.fill(barX + barWidth - filled, barY, barX + barWidth, barY + barHeight, lerpColor(0xFF55FF55, 0xFFFF5555, progress));

            int seconds = ticksLeft / 20;
            String text = seconds + "s";
            int textX = alignLeft ? barX + barWidth + 6 : barX - 6 - mc.font.width(text);
            context.drawString(mc.font, text, textX, barY, 0xFFFFFFFF, mod.getBoolConfig("itemCooldown.textShadow"));

            i++;
        }
    }

    private static int lerpColor(int startColor, int endColor, float t) {
        // Clamp t between 0 and 1
        t = Math.max(0.0f, Math.min(1.0f, t));

        int a1 = (startColor >> 24) & 0xFF;
        int r1 = (startColor >> 16) & 0xFF;
        int g1 = (startColor >> 8) & 0xFF;
        int b1 = startColor & 0xFF;

        int a2 = (endColor >> 24) & 0xFF;
        int r2 = (endColor >> 16) & 0xFF;
        int g2 = (endColor >> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");
        if (alignLeft) {
            return mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() &&
                   mouseY >= this.getY() && mouseY < this.getY() + this.getHeight();
        } else {
            return mouseX >= this.getX() - this.getWidth() && mouseX < this.getX() &&
                   mouseY >= this.getY() && mouseY < this.getY() + this.getHeight();
        }
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
