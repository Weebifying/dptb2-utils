package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

public class NotificationToast implements Toast {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, "textures/notif.png");
    public static final float TITLE_PHASE_MS = 2500;
    public static final float DESC_PHASE_MS = 4000;
    public static final float FADE_DURATION = 300;
    public static final float END_DURATION = 2000;
    private float duration;

    private final String title;
    private final String description;
    private final int color;
    private final SoundEvent sfx;
    private boolean soundPlayed = false;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;
    private float pitch;
    private float volume;

    public NotificationToast(String title, String description, int color, @Nullable SoundEvent sfx) {
        this(title, description, color, sfx, 1.0f, 1.0f);
    }

    public NotificationToast(String title, String description, int color, @Nullable SoundEvent sfx, float pitch, float volume) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.sfx = sfx;
        this.pitch = pitch;
        this.volume = volume;

        List<FormattedCharSequence> titleList = Minecraft.getInstance().font.split(FormattedText.of(this.title), 125);
        List<FormattedCharSequence> descList = Minecraft.getInstance().font.split(FormattedText.of(this.description), 125);

        if (titleList.size() + descList.size() == 2) {
            this.duration = DESC_PHASE_MS + END_DURATION;
        } else {
            this.duration = TITLE_PHASE_MS + DESC_PHASE_MS * Mth.ceil(descList.size() / 2.f) + END_DURATION;
        }
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!this.soundPlayed && time > 0) {
            this.soundPlayed = true;
            if (this.sfx != null) {
                manager.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(this.sfx, this.pitch, this.volume));
            }
        }

        this.visibility = time >= duration * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void render(GuiGraphics context, Font textRenderer, long startTime) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());

        List<FormattedCharSequence> titleList = textRenderer.split(FormattedText.of(this.title), 125);
        List<FormattedCharSequence> descList = textRenderer.split(FormattedText.of(this.description), 125);
        if (titleList.size() + descList.size() == 2) {
            context.drawString(textRenderer, this.title, 30, 7, this.color, false);
            context.drawString(textRenderer, descList.get(0), 30, 18, this.color, false);
        } else {
            if (startTime < TITLE_PHASE_MS) {
                int k = Mth.floor(Mth.clamp((TITLE_PHASE_MS - startTime) / FADE_DURATION, 0.f, 1.f) * 255.f) << 24 | 0x04000000;
                int l = this.height() / 2 - titleList.size() * 9 / 2;
                for (FormattedCharSequence orderedText : titleList) {
                    context.drawString(textRenderer, orderedText, 30, l, this.color & 0x00FFFFFF | k, false);
                    l += 9;
                }
            } else {
                int k = Mth.floor(Mth.clamp((startTime - TITLE_PHASE_MS) / FADE_DURATION, 0.f, 1.f) * 255.f) << 24 | 0x04000000;
                int size = descList.size();
                int n = (size + 1) / 2;
                long elaspedDesc = (long) (startTime - TITLE_PHASE_MS);
                int page = (int) Math.min(elaspedDesc / DESC_PHASE_MS, n - 1);

                int firstLineIndex = page * 2;
                int lineHeight = 9;
                int y = this.height() / 2 - lineHeight;

                for (int i = 0; i < 2; i++) {
                    int idx = firstLineIndex + i;
                    if (0 <= idx && idx < size) {
                        context.drawString(textRenderer, descList.get(idx), 30, y, this.color & 0x00FFFFFF | k, false);
                        y += lineHeight;
                    }
                }
            }
        }

        context.blit(RenderPipelines.GUI_TEXTURED, ICON, 8, 8, 0, 0, 16, 16, 16, 16, this.color & CommonColors.WHITE | 0xFF000000);
    }
}
