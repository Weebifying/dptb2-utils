package weebify.dptb2utils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(RendererLivingEntity.class)
public class RendererLivingEntityMixin<T extends EntityLivingBase> {
    @Inject(
            method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I",
                    shift = At.Shift.AFTER
            )
    )
    private void renderNameInject(T entity, double x, double y, double z, CallbackInfo ci) {
        if (!entity.isSneaking()) return;

        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2) {
            String text = entity.getDisplayName().getFormattedText().replaceAll("§.", "");

            if (mod.websocketClient != null && mod.websocketClient.clientsList.stream().anyMatch(text::contains)) {
                FontRenderer fontrenderer = Minecraft. getMinecraft().fontRendererObj;

                float iconX = fontrenderer.getStringWidth(text) /2.f + 2;
                float iconY = "deadmau5".equals(text) ? -10.f : 0.f;
                ResourceLocation location = new ResourceLocation(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath"));

                Minecraft.getMinecraft().getTextureManager().bindTexture(location);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();

                GlStateManager.enableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);

                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                worldRenderer.pos(iconX, iconY + 9, 0). tex(0, 1).endVertex();
                worldRenderer.pos(iconX + 9, iconY + 9, 0). tex(1, 1).endVertex();
                worldRenderer.pos(iconX + 9, iconY, 0).tex(1, 0).endVertex();
                worldRenderer.pos(iconX, iconY, 0).tex(0, 0). endVertex();
                tessellator.draw();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
