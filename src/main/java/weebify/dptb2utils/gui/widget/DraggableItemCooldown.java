package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.ItemCooldownManager;

import java.util.Map;

public class DraggableItemCooldown extends GuiButton {
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private final Map<String, Integer> itemCooldowns;
    public float relX, relY;

    private static final int padding = 5;
    private static final int lineHeight = 20;

    public DraggableItemCooldown(float relX, float relY, Map<String, Integer> itemCooldowns) {
        super(-1, 0, 0, 0, 0, "");
        this.relX = relX;
        this.relY = relY;
        this.itemCooldowns = itemCooldowns;
        recalculateSize();
    }

    private void recalculateSize() {
        int maxWidth = 0;
        for (String itemName : itemCooldowns.keySet()) {
            int ticksLeft = itemCooldowns.get(itemName);
            ItemCooldownManager.Items item = ItemCooldownManager.Items.NAME_MAP.get(itemName);
            int barWidth = (int) (0.2 * item.cooldown);
            int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth((ticksLeft / 20) + "s");
            maxWidth = Math.max(maxWidth, padding + 20 + barWidth + 6 + textWidth + padding);
        }
        int totalHeight = itemCooldowns.size() * lineHeight + padding;

        this.width = maxWidth;
        this.height = totalHeight;
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        this.xPosition = (int) (screenWidth * relX);
        this.yPosition = (int) (screenHeight * relY);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        DPTB2Utils mod = DPTB2Utils.getInstance();
        boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");

        // Draw background
        if (mod.getBoolConfig("itemCooldown.renderBackground")) {
            drawRect(
                    alignLeft ? xPosition : xPosition - width,
                    yPosition,
                    alignLeft ? xPosition + width : xPosition,
                    yPosition + height,
                    0x63000000
            );
        }

        int i = 0;
        for (Map.Entry<String, Integer> entry : itemCooldowns.entrySet()) {
            String itemName = entry.getKey();
            int ticksLeft = entry.getValue();
            ItemCooldownManager.Items item = ItemCooldownManager.Items.NAME_MAP.get(itemName);

            int iconX = alignLeft ? xPosition + padding : xPosition - padding - 16;
            int iconY = yPosition + padding + i * lineHeight;

            GlStateManager.color(1.f, 1.f, 1.f, 1.f);
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(item.texture);
            drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, 16, 16, 16, 16);

            int barWidth = (int) (0.2 * item.cooldown);
            int barHeight = 8;
            int barX = alignLeft ? iconX + 20 : iconX - 4 - barWidth;
            int barY = iconY + 4;
            int total = item.cooldown;
            float progress = (float) ticksLeft / total;
            int filled = (int) (barWidth * progress);

            drawRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
            int progressColor = lerpColor(0xFF55FF55, 0xFFFF5555, progress);
            if (alignLeft)  drawRect(barX, barY, barX + filled, barY + barHeight, progressColor);
            else drawRect(barX + barWidth - filled, barY, barX + barWidth, barY + barHeight, progressColor);

            int seconds = ticksLeft / 20;
            String text = seconds + "s";
            int textX = alignLeft ? barX + barWidth + 6 : barX - 6 - mc.fontRendererObj.getStringWidth(text);
            mc.fontRendererObj.drawString(text, textX, barY, 0xFFFFFFFF, mod.getBoolConfig("itemCooldown.textShadow"));

            i++;
        }
    }

    private static int lerpColor(int startColor, int endColor, float t) {
        t = Math.max(0.f, Math.min(1.f, t));

        int a1 = (startColor >> 24) & 0xFF;
        int r1 = (startColor >> 16) & 0xFF;
        int g1 = (startColor >> 8) & 0xFF;
        int b1 = startColor & 0xFF;

        int a2 = (endColor >> 24) & 0xFF;
        int r2 = (endColor >> 16) & 0xFF;
        int g2 = (endColor >> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isMouseOverWidget(int mouseX, int mouseY) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");
        if (alignLeft) {
            return mouseX >= xPosition && mouseX < xPosition + width &&
                    mouseY >= yPosition && mouseY < yPosition + height;
        } else {
            return mouseX >= xPosition - width && mouseX < xPosition &&
                    mouseY >= yPosition && mouseY < yPosition + height;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isMouseOverWidget(mouseX, mouseY)) {
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
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();

            // Clamp to screen and update
            this.xPosition = newX;
            this.yPosition = newY;
            relX = (float) newX / screenWidth;
            relY = (float) newY / screenHeight;
            return true;
        }
        return false;
    }
}