package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.ItemCooldownConfigScreen;

import java.util.HashMap;
import java.util.Map;

public class ItemCooldownManager {
    public enum Items {
        BEAR_TRAP("Bear Trap", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/trap.png")),
        LANDMINE("Landmine", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/landmine.png")),
        BIRD("Bird", 400, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/bird.png")),
        GROUND_POUND("Ground Pound", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/pound.png")),
        EXPLOSIVE_CAKE("Explosive Cake", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/cake.png")),
        REMOTE_ACTIVATION("Remote Activation", 400, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/remote.png")),
        SMOKE_BOMB("Smoke Bomb", 400, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/smoke.png")),
        FREEZE_RAY("Freeze Ray", 400, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/freeze.png")),
        SWAP_CRYSTAL("Swap Crystal", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/swap.png")),
        IMMUNE_APPLE("Immune Apple", 600, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/immune.png")),
        LASSO("Lasso", 400, Identifier.of(DPTB2Utils.MOD_ID, "textures/items/lasso.png"));

        public final String name;
        public final int cooldown;
        public final Identifier texture;
        public static final Map<String, Items> NAME_MAP = new HashMap<>();

        static {
            for (Items item : values()) {
                NAME_MAP.put(item.name, item);
            }
        }

        Items(String name, int cooldown, Identifier texture) {
            this.name = name;
            this.cooldown = cooldown;
            this.texture = texture;
        }
    }

    public static Map<String, Integer> currentCooldowns = new HashMap<>();
    public static String lastAdded = "";

    public static boolean isInMap(double x, double y, double z) {
        // city: 124 7 -113 -> -1 72 140
        if (x >= -1 && x <= 124 && y >= 7 && y <= 72 && z >= -113 && z <= 140) {
            return true;
        }
        // wild west: -18 120 -108 -> -105 195 138
        if (x >= -105 && x <= -18 && y >= 120 && y <= 195 && z >= -108 && z <= 138) {
            return true;
        }

        return false;
    }
    public static boolean isInSpawn(double x, double y, double z) {
        // city: -1 13 -115 -> 123 72 -85
        // near spawn: 56 17 -85 -> 66 31 -66
        if (x >= -1 && x <= 123 && y >= 13 && y <= 72 && z >= -115 && z <= -85) {
            return true;
        }
        if (x >= 56 && x <= 66 && y >= 17 && y <= 31 && z >= -85 && z <= -66) {
            return true;
        }
        // wild west: -105 144 -115 -> -19 194 -80
        // near spawn: -67 144 -80 -> -57 158 -60
        if (x >= -105 && x <= -19 && y >= 144 && y <= 194 && z >= -115 && z <= -80) {
            return true;
        }
        if (x >= -67 && x <= -57 && y >= 144 && y <= 158 && z >= -80 && z <= -60) {
            return true;
        }

        return false;
    }
    public static boolean isInPkCiv(double x, double y, double z) {
        // city: -1 19 -85 -> 55 72 -67
        if (x >= -1 && x <= 55 && y >= 19 && y <= 72 && z >= -85 && z <= -67) {
            return true;
        }
        // wild west: -68 194 -75 -> -105 144 -62 (GUESSWORK)
        if (x >= -105 && x <= -68 && y >= 144 && y <= 194 && z >= -75 && z <= -62) {
            return true;
        }

        return false;
    }

    public static void addCooldown(String itemName) {
        if (Items.NAME_MAP.containsKey(itemName) && !currentCooldowns.containsKey(itemName)) {
            currentCooldowns.put(itemName, Items.NAME_MAP.get(itemName).cooldown);
            lastAdded = itemName;
        }
    }

    public static Map<String, Integer> generateRandomCooldowns() {
        Map<String, Integer> randomCooldowns = new HashMap<>();
        for (String itemName : Items.NAME_MAP.keySet()) {
            if (Math.random() > 0.6) {
                randomCooldowns.put(itemName, (int) (Items.NAME_MAP.get(itemName).cooldown*Math.random()));
            }
        }
        if (randomCooldowns.isEmpty()) {
            String itemName = Items.NAME_MAP.get(Items.NAME_MAP.keySet().stream().toList().get((int)(Math.random()*Items.NAME_MAP.size()))).name;
            int randomTicks = (int)(Math.random() * 400);
            randomCooldowns.put(itemName, randomTicks);
        }
        return randomCooldowns;
    }

    public static void initialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!player.getStackInHand(hand).isEmpty() && DPTB2Utils.getInstance().isInDPTB2) {
                ItemStack stack = player.getStackInHand(hand);
                String itemName = stack.getName().getString();
                if (Items.NAME_MAP.containsKey(itemName)) {
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    if (!isInPkCiv(x, y, z)) {
                        if ((itemName.equals("Immune Apple") || !isInSpawn(x, y, z)) && isInMap(x, y, z)) {
                            addCooldown(itemName);
                        }
                    }
                }
            }

            return ActionResult.PASS;
        });
        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            for (String itemName : currentCooldowns.keySet().toArray(new String[0])) {
                int timeLeft = currentCooldowns.get(itemName);
                if (timeLeft > 0) {
                    currentCooldowns.put(itemName, timeLeft - 1);
                } else {
                    currentCooldowns.remove(itemName);
                }
            }
        });
//        HudLayerRegistrationCallback.EVENT.register((drawer) -> {
//            drawer.attachLayerAfter(
//                    IdentifiedLayer.HOTBAR_AND_BARS,
//                    Identifier.of(DPTB2Utils.MOD_ID, "item_cooldowns"),
//                    ItemCooldownManager::renderItemCooldowns
//            );
//        });
        HudRenderCallback.EVENT.register(ItemCooldownManager::renderItemCooldowns);
    }

    private static void renderItemCooldowns(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (mod.isInDPTB2 && mod.getBoolConfig("itemCooldown.enabled") && !currentCooldowns.isEmpty() && !(mc.currentScreen instanceof ItemCooldownConfigScreen)) {
            int width = mc.getWindow().getScaledWidth();
            int height = mc.getWindow().getScaledHeight();
            int posX = (int)(mod.getFloatConfig("itemCooldown.posX")*width);
            int posY = (int)(mod.getFloatConfig("itemCooldown.posY")*height);
            int padding = 5;
            int lineHeight = 20;
            int maxWidth = 0;
            int totalHeight = currentCooldowns.size() * lineHeight + padding;

            boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");

            for (Map.Entry<String, Integer> entry : currentCooldowns.entrySet()) {
                String itemName = entry.getKey();
                int ticksLeft = entry.getValue();
                Items item = Items.NAME_MAP.get(itemName);
                int barWidth = (int) (0.2 * item.cooldown);
                int textWidth = mc.textRenderer.getWidth((ticksLeft / 20) + "s");
                maxWidth = Math.max(maxWidth, padding + 20 + barWidth + 6 + textWidth + padding);
            }

            if (mod.getBoolConfig("itemCooldown.renderBackground")) {
                drawContext.fill(
                        alignLeft ? posX : posX - maxWidth,
                        posY,
                        alignLeft ? posX + maxWidth : posX,
                        posY + totalHeight,
                        0x63000000
                );
            }

            int i = 0;
            for (Map.Entry<String, Integer> entry : currentCooldowns.entrySet()) {
                String itemName = entry.getKey();
                int ticksLeft = entry.getValue();
                Items item = Items.NAME_MAP.get(itemName);

                int x = alignLeft ? posX + padding : posX - padding - 16;
                int y = posY + padding + i * lineHeight;
                drawContext.drawTexture(RenderLayer::getGuiTextured, item.texture, x, y, 0, 0, 16, 16, 16, 16);

                int barWidth = (int) (0.2 * Items.NAME_MAP.get(itemName).cooldown);
                int barHeight = 8;
                int barX = alignLeft ? x + 20 : x - 4 - barWidth;
                int barY = y + 4;
                int total = item.cooldown;
                float progress = (float)ticksLeft / total;
                int filled = (int)(barWidth * progress);
                drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
                if (alignLeft) drawContext.fill(barX, barY, barX + filled, barY + barHeight, lerpColor(0xFF55FF55, 0xFFFF5555, progress));
                else drawContext.fill(barX + barWidth - filled, barY, barX + barWidth, barY + barHeight, lerpColor(0xFF55FF55, 0xFFFF5555, progress));

                int seconds = ticksLeft / 20;
                String text = seconds + "s";
                int textX = alignLeft ? barX + barWidth + 6 : barX - 6 - mc.textRenderer.getWidth(text);
                drawContext.drawText(mc.textRenderer, text, textX, barY, 0xFFFFFFFF, mod.getBoolConfig("itemCooldown.textShadow"));

                i++;
            }
        }
    }

    // thanks gpt
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
}
