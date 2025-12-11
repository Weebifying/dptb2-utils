package weebify.dptb2utils.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

public class NotificationToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final Identifier ICON = Identifier.of(DPTB2Utils.MOD_ID, "textures/notif.png");
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

        List<OrderedText> titleList = MinecraftClient.getInstance().textRenderer.wrapLines(StringVisitable.plain(this.title), 125);
        List<OrderedText> descList = MinecraftClient.getInstance().textRenderer.wrapLines(StringVisitable.plain(this.description), 125);

        if (titleList.size() + descList.size() == 2) {
            this.duration = DESC_PHASE_MS + END_DURATION;
        } else {
            this.duration = TITLE_PHASE_MS + DESC_PHASE_MS * MathHelper.ceil(descList.size() / 2.f) + END_DURATION;
        }
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!this.soundPlayed && time > 0) {
            this.soundPlayed = true;
            if (this.sfx != null) {
                manager.getClient().getSoundManager().play(PositionedSoundInstance.master(this.sfx, this.pitch, this.volume));
            }
        }

        this.visibility = time >= duration * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        List<OrderedText> titleList = textRenderer.wrapLines(StringVisitable.plain(this.title), 125);
        List<OrderedText> descList = textRenderer.wrapLines(StringVisitable.plain(this.description), 125);
        if (titleList.size() + descList.size() == 2) {
            context.drawText(textRenderer, this.title, 30, 7, this.color, false);
            context.drawText(textRenderer, descList.get(0), 30, 18, this.color, false);
        } else {
            if (startTime < TITLE_PHASE_MS) {
                int k = MathHelper.floor(MathHelper.clamp((TITLE_PHASE_MS - startTime) / FADE_DURATION, 0.f, 1.f) * 255.f) << 24 | 0x04000000;
                int l = this.getHeight() / 2 - titleList.size() * 9 / 2;
                for (OrderedText orderedText : titleList) {
                    context.drawText(textRenderer, orderedText, 30, l, this.color & 0x00FFFFFF | k, false);
                    l += 9;
                }
            } else {
                int k = MathHelper.floor(MathHelper.clamp((startTime - TITLE_PHASE_MS) / FADE_DURATION, 0.f, 1.f) * 255.f) << 24 | 0x04000000;
                int size = descList.size();
                int n = (size + 1) / 2;
                long elaspedDesc = (long) (startTime - TITLE_PHASE_MS);
                int page = (int) Math.min(elaspedDesc / DESC_PHASE_MS, n - 1);

                int firstLineIndex = page * 2;
                int lineHeight = 9;
                int y = this.getHeight() / 2 - lineHeight;

                for (int i = 0; i < 2; i++) {
                    int idx = firstLineIndex + i;
                    if (0 <= idx && idx < size) {
                        context.drawText(textRenderer, descList.get(idx), 30, y, this.color & 0x00FFFFFF | k, false);
                        y += lineHeight;
                    }
                }
            }
        }

        context.drawTexture(RenderLayer::getGuiTextured, ICON, 8, 8, 0, 0, 16, 16, 16, 16, this.color & Colors.WHITE | 0xFF000000);
    }
}
