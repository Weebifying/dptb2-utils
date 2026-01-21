package weebify.dptb2utils.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import weebify.dptb2utils.DPTB2Utils;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class WaypointManager {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    public static Map<String, Waypoint> waypoints = new HashMap<>();
    public static boolean renderThroughWalls = true;

    public record Waypoint(double x, double y, double z, String name, int color) {}

    public static void addWaypoint(String id, double x, double y, double z, String name, int color) {
        waypoints.put(id, new Waypoint(x, y, z, name, color));
    }

    public static void removeWaypoint(String id) {
        waypoints.remove(id);
    }

    public static void initializeWaypoints() {
        // #FFAA00
        addWaypoint("shifty", -78.5, 150, -38.5, "Shifty", 0xFFAA00);
        addWaypoint("banker", -88.5, 150, -1.5, "Banker", 0xFFAA00);
        addWaypoint("train_conductor", -70.5, 150, 36.5, "Train Conductor", 0xFFAA00);
        addWaypoint("inconspicuous_guard", -46.5, 150, 74.5, "Inconspicuous Guard", 0xFFAA00);
        addWaypoint("julia", -31.5, 150, 77.5, "Julia", 0xFFAA00);
        addWaypoint("duelmaster", -81.5, 149, 70.5, "Duelmaster", 0xFFAA00);
        addWaypoint("unsuspicious_merchant", -47.5, 151, 121.5, "Unsuspicious Merchant", 0xFFAA00);
        addWaypoint("venecio", -74.5, 167, -51.5, "Venecio", 0xFFAA00);
        addWaypoint("unknown", -57.5, 143, -92.5, "???", 0xFFAA00);

        // #55FFFF
        addWaypoint("gardener", 46.5, 21, -56.5, "Gardener", 0x55FFFF);
        addWaypoint("wandering_believer", 86.5, 21, -34.5, "Wandering Believer", 0x55FFFF);
        addWaypoint("bernard", 75.5, 21, -18.5, "Bernard", 0x55FFFF);
        addWaypoint("carpenter", 49.5, 22, -32.5, "Carpenter", 0x55FFFF);
        addWaypoint("chef_gordon", 49.5, 21, -20.5, "Chef Gordon", 0x55FFFF);
        addWaypoint("librarian", 44.5, 21, -3.5, "Librarian", 0x55FFFF);
        addWaypoint("librarian_assistant", 49.5, 21, 9.5, "Librarian Assistant", 0x55FFFF);
        addWaypoint("delivery_dave", 77.5, 21, 3.5, "Delivery Dave", 0x55FFFF);
        addWaypoint("linda", 44.5, 21, 34.5, "Linda", 0x55FFFF);
        addWaypoint("minimum_wage_worker", 79.5, 21, 35.5, "Minimum Wage Worker", 0x55FFFF);
        addWaypoint("maximum_wage_worker", 43.5, 21, 100.5, "Maximum Wage Worker", 0x55FFFF);
        addWaypoint("alberto", 83.5, 21, 96.5, "Alberto", 0x55FFFF);

        // #D2FFC8
        addWaypoint("samita2", -78.5, 50, 26.5, "Samita2", 0xD2FFC8);
        addWaypoint("samita3", -85.5, 50, 22.5, "Samita3", 0xD2FFC8);
        addWaypoint("lucien", -65.5, 21, -59.5, "Lucien", 0xD2FFC8);
        addWaypoint("tony", -80.5, 150, 23.5, "Tony", 0xD2FFC8);

    }

    public static void initializeEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            if (!DPTB2Utils.getInstance().isInDPTB2) return;

            MatrixStack matrices = context.matrixStack();
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            TextRenderer tr = MC.textRenderer;
            BufferBuilder buffer;

            if (DPTB2Utils.getInstance().getBoolConfig("waypoints.enabled")) {
                for (Waypoint wp : waypoints.values()) {
                    double cx = wp.x() - cameraPos.x;
                    double cy = wp.y() - cameraPos.y;
                    double cz = wp.z() - cameraPos.z;

                    double distance = Math.sqrt(cx * cx + cy * cy + cz * cz);
                    if (distance > 12 * 16) continue;
                    // distance = 0 -> scale = 0.02f
                    // distance >= 32 -> scale = 0.04f
                    float scale = (float) (0.02f + Math.min(distance, 32) * (0.04f - 0.02f) / 32.f);
                    float alpha = (float) Math.max(0.5f, 0.8f - distance / (8 * 16));

                    matrices.push();
                    // Rotate the waypoint to face the camera
                    matrices.translate(cx, cy + 3.5 - Math.max(0.04f - scale, 0) / 0.02f, cz);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch() * 0.4f));
                    matrices.scale(-scale, -scale, scale);


                    //                String label = String.format("%s (%sm)", wp.name, (int) distance);
                    int color = (wp.color() & 0xFFFFFF) | ((int) (alpha * 255) << 24);

                    if (renderThroughWalls) RenderSystem.disableDepthTest();
                    Matrix4f mat = matrices.peek().getPositionMatrix();
                    VertexConsumerProvider.Immediate vcp = MC.getBufferBuilders().getEntityVertexConsumers();

                    int width = Math.max(tr.getWidth(wp.name()), tr.getWidth(String.format("%dm", (int) distance)));
                    float padding = 2.f;

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                    RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

                    // rectangle behind text
                    buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    float bgLeft = -width / 2.f - padding;
                    float bgRight = width / 2.f + padding;
                    float bgTop = -2.f;
                    float bgBottom = tr.fontHeight * 2 + 2.f;

                    buffer.vertex(mat, bgLeft, bgTop, 0).color(color);
                    buffer.vertex(mat, bgLeft, bgBottom, 0).color(color);
                    buffer.vertex(mat, bgRight, bgBottom, 0).color(color);
                    buffer.vertex(mat, bgRight, bgTop, 0).color(color);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());

                    // triangle below text
                    buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                    float triSize = width + padding * 2;
                    //                float triHeight = (float) (Math.sqrt(3) * triSize / 2);
                    float triHeight = 20.f;
                    float triYTop = bgBottom + padding;
                    float triYBottom = triYTop + triHeight;

                    buffer.vertex(mat, 0, triYBottom, 0).color(color);
                    buffer.vertex(mat, triSize / 2, triYTop, 0).color(color);
                    buffer.vertex(mat, -triSize / 2, triYTop, 0).color(color);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());

                    RenderSystem.disableBlend();

                    // text
                    tr.draw(wp.name(), -tr.getWidth(wp.name()) / 2.f, 0, Colors.WHITE, false, mat, vcp, renderThroughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                    tr.draw(String.format("%dm", (int) distance), -tr.getWidth(String.format("%dm", (int) distance)) / 2.f, tr.fontHeight + 2, Colors.WHITE, false, mat, vcp, renderThroughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                    vcp.draw(); // actually draws the text!!

                    if (renderThroughWalls) RenderSystem.enableDepthTest();
                    matrices.pop();
                }
            }
        });
    }
}
