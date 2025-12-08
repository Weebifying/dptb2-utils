package weebify.dptb2utils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.screen.GuiItemCooldownConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCooldownManager {
    public enum Items {
        BEAR_TRAP("Bear Trap", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/trap.png")),
        LANDMINE("Landmine", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/landmine.png")),
        BIRD("Bird", 400, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/bird.png")),
        GROUND_POUND("Ground Pound", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/pound.png")),
        EXPLOSIVE_CAKE("Explosive Cake", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/cake.png")),
        REMOTE_ACTIVATION("Remote Activation", 400, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/remote.png")),
        SMOKE_BOMB("Smoke Bomb", 400, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/smoke.png")),
        FREEZE_RAY("Freeze Ray", 400, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/freeze.png")),
        SWAP_CRYSTAL("Swap Crystal", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/swap.png")),
        IMMUNE_APPLE("Immune Apple", 600, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/immune.png")),
        LASSO("Lasso", 400, new ResourceLocation(DPTB2Utils.MOD_ID, "textures/items/lasso.png"));

        public final String name;
        public final int cooldown;
        public final ResourceLocation texture;
        public static final Map<String, Items> NAME_MAP = new HashMap<>();

        static {
            for (Items item : values()) {
                NAME_MAP.put(item.name, item);
            }
        }

        Items(String name, int cooldown, ResourceLocation texture) {
            this.name = name;
            this.cooldown = cooldown;
            this.texture = texture;
        }
    }

    public static Map<String, Integer> currentCooldowns = new HashMap<>();
    public static String lastAdded = "";

    public static ItemCooldownManager instance;

    public static void initialize() {
        DPTB2Utils.LOGGER.info("Initializing ItemCooldownManager");
        instance = new ItemCooldownManager();
        MinecraftForge.EVENT_BUS.register(instance);
    }

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
        List<String> keys = new ArrayList<>(Items.NAME_MAP.keySet());
        for (String itemName : keys) {
            if (Math.random() > 0.6) {
                randomCooldowns.put(itemName, (int) (Items.NAME_MAP.get(itemName).cooldown * Math.random()));
            }
        }
        if (randomCooldowns.isEmpty()) {
            String itemName = keys.get((int) (Math.random() * keys.size()));
            int randomTicks = (int) (Math.random() * 400);
            randomCooldowns.put(itemName, randomTicks);
        }
        return randomCooldowns;
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.entityPlayer == null || event.entityPlayer.getCurrentEquippedItem() == null) {
            return;
        }

        if (!DPTB2Utils.getInstance().isInDPTB2) {
            return;
        }

        ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
        String itemName = stack.getDisplayName().replaceAll("§[0-9a-fk-or]", "");

        if (Items.NAME_MAP.containsKey(itemName)) {
            double x = event.entityPlayer.posX;
            double y = event.entityPlayer.posY;
            double z = event.entityPlayer.posZ;

            if (! isInPkCiv(x, y, z)) {
                if ((itemName.equals("Immune Apple") || !isInSpawn(x, y, z)) && isInMap(x, y, z)) {
                    DPTB2Utils.LOGGER.info("added cooldown {}", itemName);
                    addCooldown(itemName);
                    DPTB2Utils.LOGGER.info("added cooldown {}", itemName);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        for (String itemName : new ArrayList<>(currentCooldowns.keySet())) {
            int timeLeft = currentCooldowns.get(itemName);
            if (timeLeft > 0) {
                currentCooldowns.put(itemName, timeLeft - 1);
            } else {
                currentCooldowns.remove(itemName);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (!mod.isInDPTB2 || !mod.getBoolConfig("itemCooldown.enabled") || currentCooldowns.isEmpty()) {
            return;
        }

        if (mc.currentScreen instanceof GuiItemCooldownConfig) {
            return;
        }

        ScaledResolution scaledRes = new ScaledResolution(mc);
        int width = scaledRes.getScaledWidth();
        int height = scaledRes.getScaledHeight();
        int posX = (int) (mod.getFloatConfig("itemCooldown.posX") * width);
        int posY = (int) (mod.getFloatConfig("itemCooldown.posY") * height);
        int padding = 5;
        int lineHeight = 20;
        int maxWidth = 0;
        int totalHeight = currentCooldowns.size() * lineHeight + padding;

        boolean alignLeft = mod.getStringConfig("itemCooldown.textAlign").equals("left");

        // Calculate max width
        for (Map.Entry<String, Integer> entry : currentCooldowns.entrySet()) {
            String itemName = entry.getKey();
            int ticksLeft = entry.getValue();
            Items item = Items.NAME_MAP.get(itemName);
            int barWidth = (int) (0.2 * item.cooldown);
            int textWidth = mc.fontRendererObj.getStringWidth((ticksLeft / 20) + "s");
            maxWidth = Math.max(maxWidth, padding + 20 + barWidth + 6 + textWidth + padding);
        }

        // Render background
        if (mod.getBoolConfig("itemCooldown.renderBackground")) {
            Gui.drawRect(
                    alignLeft ? posX : posX - maxWidth,
                    posY,
                    alignLeft ?  posX + maxWidth : posX,
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

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(item.texture);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            int barWidth = (int) (0.2 * item.cooldown);
            int barHeight = 8;
            int barX = alignLeft ? x + 20 : x - 4 - barWidth;
            int barY = y + 4;
            int total = item.cooldown;
            float progress = (float) ticksLeft / total;
            int filled = (int) (barWidth * progress);

            Gui.drawRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);

            int barColor = lerpColor(0xFF55FF55, 0xFFFF5555, progress);
            if (alignLeft) {
                Gui.drawRect(barX, barY, barX + filled, barY + barHeight, barColor);
            } else {
                Gui.drawRect(barX + barWidth - filled, barY, barX + barWidth, barY + barHeight, barColor);
            }

            int seconds = ticksLeft / 20;
            String text = seconds + "s";
            int textX = alignLeft ? barX + barWidth + 6 : barX - 6 - mc.fontRendererObj.getStringWidth(text);
            mc.fontRendererObj.drawString(
                    text,
                    textX,
                    barY,
                    0xFFFFFFFF,
                    mod.getBoolConfig("itemCooldown.textShadow")
            );

            i++;
        }
    }

    private static int lerpColor(int startColor, int endColor, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));

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
}