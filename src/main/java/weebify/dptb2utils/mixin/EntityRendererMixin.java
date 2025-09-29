package weebify.dptb2utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderLabelInject(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        if (mod.isInDPTB2) {
            if (mod.websocketClient != null && mod.websocketClient.clientsList.stream().anyMatch(name -> text.getString().contains(name))) {
                TextRenderer textRenderer = ((EntityRenderer<?, ?>)(Object)this).getTextRenderer();
                boolean bl = !state.sneaking;
                float x = textRenderer.getWidth(text.getString()) / 2.f + 2;
                float y = "deadmau5".equals(text.getString()) ? -10.f : 0.f;
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                Identifier id = Identifier.of(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath"));


                RenderLayer rl = bl ? RenderLayer.getTextSeeThrough(id) : RenderLayer.getText(id);
                VertexConsumer vc = vertexConsumers.getBuffer(rl);

                // tl, bl, br, tr
                vc.vertex(matrix4f, x, y, 0).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x, y + 9, 0).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x + 9, y + 9, 0).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x + 9, y, 0).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);

                if (bl) {
                    int brightLight = LightmapTextureManager.applyEmission(light, 2);
                    RenderLayer rl2 = RenderLayer.getText(id);
                    VertexConsumer vc2 = vertexConsumers.getBuffer(rl2);

                    vc2.vertex(matrix4f, x, y, 0).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x, y + 9, 0).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x + 9, y + 9, 0).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x + 9, y, 0).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                }
            }
        }
    }
}
