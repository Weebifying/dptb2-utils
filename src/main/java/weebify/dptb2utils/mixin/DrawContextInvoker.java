package weebify.dptb2utils.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(DrawContext.class)
public interface DrawContextInvoker {
    @Invoker("drawTexturedQuad")
    public void invokeDrawTexturedQuad(Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2, int color);
}
