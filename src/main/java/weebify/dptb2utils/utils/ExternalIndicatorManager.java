package weebify.dptb2utils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import weebify.dptb2utils.DPTB2Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExternalIndicatorManager {
    private static final Minecraft MC = Minecraft.getMinecraft();
    public static BufferedImage image;
    public static String errorMessage = "";
    private static boolean init = false;

    public static ExternalIndicatorManager instance;

    public static void initialize() {
        DPTB2Utils.LOGGER.info("Initializing ExternalIndicatorManager");
        DPTB2Utils.getInstance().scheduleTask(1, () -> {
            DPTB2Utils.LOGGER.info("running indicator initialization");

            DPTB2Utils mod = DPTB2Utils.getInstance();
            String path = mod.getStringConfig("others.indicatorPath");
            if (path.startsWith("external/")) {
                String fileName = path.replace("external/", "");
                File file = new File(MC.mcDataDir + "/config/dptb2utils", fileName);
                if (registerExternal(file)) {
                    DPTB2Utils.LOGGER.info("Loaded external indicator: {}", fileName);
                } else {
                    mod.setStringConfig("others.indicatorPath", mod.config.getDefaultConfig("others.indicatorPath"));
                    ExternalIndicatorManager.image = null;
                    DPTB2Utils.LOGGER.warn("Failed to load external indicator: {}. Reverted to default.", fileName);
                }
            }
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!init) {


                init = true;
            }
        }
    }

    public static boolean registerExternal(File file) {
        String fileName = file.getName();
        File dest = new File(MC.mcDataDir + "/config/dptb2utils",  fileName);
        DPTB2Utils.LOGGER.info("file: {}", file.getAbsolutePath());
        DPTB2Utils.LOGGER.info("dest: {}", dest.getAbsolutePath());
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            BufferedImage image = ImageIO.read(file);
            ResourceLocation location = new ResourceLocation(DPTB2Utils.MOD_ID, String.format("external/%s", fileName));
            DynamicTexture texture = new DynamicTexture(image);

            registerTexture(location, texture);
            ExternalIndicatorManager.image = image;
            return true;
        } catch (IOException e) {
            DPTB2Utils.LOGGER.error("Failed to load external indicator: {}", fileName);
            DPTB2Utils.LOGGER.error("Error: {}", e.toString());
            errorMessage = e.toString();
            return false;
        }
    }

    public static void registerTexture(ResourceLocation location, DynamicTexture texture) {
        MC.getTextureManager().loadTexture(location, texture);
    }

    public static void unregisterTexture(ResourceLocation location) {
        MC.getTextureManager().deleteTexture(location);
    }
}