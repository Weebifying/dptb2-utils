package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

public class Notification {
    public static final float TITLE_PHASE_MS = 2500;
    public static final float DESC_PHASE_MS = 4000;
    public static final float FADE_DURATION = 300;
    public static final float END_DURATION = 2000;
    public static final int TOAST_WIDTH = 160;
    public static final int TEXT_WRAP_WIDTH = 125;
    private final long duration;

    private final String title;
    private final String description;
    private final int color;
    private final String soundName;
    private final ResourceLocation iconTexture;
    private final long createdMs;
    private final List<String> titleLines;
    private final List<String> descLines;
    private boolean soundPlayed = false;
    private float pitch;
    private float volume;

    public Notification(String title, String description, int color, ResourceLocation iconTexture, String soundName) {
        this(title, description, color, iconTexture, soundName, 1.0f, 1.0f);
    }

    public Notification(String title, String description, int color, ResourceLocation iconTexture, String soundName, float pitch, float volume) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.iconTexture = iconTexture;
        this.soundName = soundName;
        this.pitch = pitch;
        this.volume = volume;
        this.createdMs = System.currentTimeMillis();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        this.titleLines = fr.listFormattedStringToWidth(this.title, TEXT_WRAP_WIDTH);
        this.descLines = fr.listFormattedStringToWidth(this.description, TEXT_WRAP_WIDTH);

        // same duration logic as your Fabric code:
        if (titleLines.size() + descLines.size() == 2) {
            this.duration = (long) (DESC_PHASE_MS + END_DURATION);
        } else {
            int descPages = (int) Math.ceil(descLines.size() / 2.0);
            this.duration = (long) (TITLE_PHASE_MS + DESC_PHASE_MS * descPages + END_DURATION);
        }
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdMs) >= duration;
    }

    public void tryPlaySound() {
        if (soundPlayed) return;
        if (soundName == null || soundName.isEmpty()) return;
        soundPlayed = true;
        try {
            Minecraft.getMinecraft().getSoundHandler().playSound(
                    PositionedSoundRecord.create(new ResourceLocation(soundName), this.pitch)
            );
        } catch (Throwable t) {
            DPTB2Utils.LOGGER.error("Failed to play sound {}: {}", this.soundName, t);
        }
    }

    public void draw(net.minecraft.client.gui.Gui gui, FontRenderer fr, int x, int y) {
        long elapsed = System.currentTimeMillis() - createdMs;
        if (!soundPlayed && elapsed >= 0) tryPlaySound();

        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, TOAST_WIDTH, getHeight(), 160, 32);

        if (titleLines.size() + descLines.size() == 2) {
            int textColor = color; // ??
            fr.drawString(titleLines.get(0), x + 30, y + 7, textColor);
            fr.drawString(descLines.get(0), x + 30, y + 18, textColor);
        } else {
            if (elapsed < TITLE_PHASE_MS) {
                float t = Math.max(0f, Math.min(1f, (TITLE_PHASE_MS - elapsed) / FADE_DURATION));
                int alpha = (int)(t * 255f) << 24;
                int base = 0x04000000;
                int drawColour = (color & 0x00FFFFFF) | (alpha | base);
                int l = y + (getHeight()/2 - titleLines.size() * 9 / 2);
                for (String line : titleLines) {
                    fr.drawString(line, x + 30, l, drawColour);
                    l += 9;
                }
            } else {
                float t = Math.max(0f, Math.min(1f, (elapsed - TITLE_PHASE_MS) / FADE_DURATION));
                int alpha = (int)(t * 255f) << 24;
                int drawColour = (color & 0x00FFFFFF) | alpha;

                int size = descLines.size();
                int pages = (size + 1) / 2;
                long elapsedDesc = (long)(elapsed - TITLE_PHASE_MS);
                int page = (int)Math.min(elapsedDesc / DESC_PHASE_MS, Math.max(0, pages - 1));

                int firstLineIndex = page * 2;
                int yLine = y + getHeight()/2 - 9;
                for (int i = 0; i < 2; i++) {
                    int idx = firstLineIndex + i;
                    if (0 <= idx && idx < size) {
                        fr.drawString(descLines.get(idx), x + 30, yLine, drawColour);
                        yLine += 9;
                    }
                }
            }
        }

        // draw icon tinted
        if (iconTexture != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(iconTexture);
            // multiply color
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            GL11.glColor4f(r, g, b, 1f);
            Gui.drawModalRectWithCustomSizedTexture(x + 8, y + 8, 0, 0, 16, 16, 16, 16);
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    public int getHeight() {
//        // base toast height, expand if many title lines
//        int base = 32;
//        int extra = Math.max(0, titleLines.size() - 1) * 9;
//        return base + extra;
        return 32;
    }
}
