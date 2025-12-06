package weebify.dptb2utils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.LabelCommandRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(LabelCommandRenderer.Commands.class)
public class LabelCommandRendererCommandsMixin {
    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void addInject(MatrixStack matrices, @Nullable Vec3d pos, int y, Text text, boolean notSneaking, int light, double squaredDistanceToCamera, CameraRenderState cameraState, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        VertexConsumerProvider vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        if (mod.isInDPTB2) {
            if (mod.websocketClient != null && mod.websocketClient.clientsList.stream().anyMatch(name -> text.getString().contains(name))) {
                float x = MinecraftClient.getInstance().textRenderer.getWidth(text.getString()) / 2.f + 2;
                float yOffset = "deadmau5".equals(text.getString()) ? -10.f : 0.f;
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                Identifier id = Identifier.of(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath"));

                RenderLayer rl = notSneaking ? RenderLayer.getTextSeeThrough(id) : RenderLayer.getText(id);
                VertexConsumer vc = vertexConsumers.getBuffer(rl);

                // tl, bl, br, tr
                vc.vertex(matrix4f, x, yOffset, 0).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x, yOffset + 9, 0).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x + 9, yOffset + 9, 0).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);
                vc.vertex(matrix4f, x + 9, yOffset, 0).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0.f, 1.f, 0.f).color(0x80FFFFFF);

                if (notSneaking) {
                    int brightLight = LightmapTextureManager.applyEmission(light, 2);
                    RenderLayer rl2 = RenderLayer.getText(id);
                    VertexConsumer vc2 = vertexConsumers.getBuffer(rl2);

                    vc2.vertex(matrix4f, x, yOffset, 0).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x, yOffset + 9, 0).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x + 9, yOffset + 9, 0).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                    vc2.vertex(matrix4f, x + 9, yOffset, 0).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(brightLight).normal(0.f, 1.f, 0.f).color(Colors.WHITE);
                }
            }
        }
    }
}
