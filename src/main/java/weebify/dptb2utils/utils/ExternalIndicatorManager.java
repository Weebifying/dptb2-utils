package weebify.dptb2utils.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ExternalIndicatorManager {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    public static NativeImage image;
    public static String errorMessage = "";
    private static boolean init = false;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!init) {
                DPTB2Utils mod = DPTB2Utils.getInstance();
                String path = mod.getStringConfig("others.indicatorPath");
                if (path.startsWith("external/")) {
                    String fileName = path.replace("external/", "");
                    File file = new File(MC.runDirectory + "/config/dptb2utils", fileName);
                    if (registerExternal(file)) {
                        DPTB2Utils.LOGGER.info("Loaded external indicator: {}", fileName);
                    } else {
                        mod.setStringConfig("others.indicatorPath", mod.config.getDefaultConfig("others.indicatorPath"));
                        ExternalIndicatorManager.image = null;
                        DPTB2Utils.LOGGER.warn("Failed to load external indicator: {}. Reverted to default.", fileName);
                    }
                }

                init = true;
            }
        });
    }

    public static boolean registerExternal(File file) {
        String fileName = file.getName();
        File dest = new File(MC.runDirectory + "/config/dptb2utils",  fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            NativeImage image = NativeImage.read((new FileInputStream(dest)));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier id = Identifier.of(DPTB2Utils.MOD_ID, String.format("external/%s", fileName));

            registerTexture(id, texture);
            ExternalIndicatorManager.image = image;
            return true;
        } catch (IOException e) {
            DPTB2Utils.LOGGER.error("Failed to load external indicator: {}", fileName);
            DPTB2Utils.LOGGER.error("Error: {}", e.toString());
            errorMessage = e.toString();

            return false;
        }
    }

    public static void registerTexture(Identifier id, AbstractTexture texture) {
        MC.execute(() -> {
            MC.getTextureManager().registerTexture(id, texture);
        });
    }

    public static void unregisterTexture(Identifier id) {
        MC.execute(() -> {
            MC.getTextureManager().destroyTexture(id);
        });
    }
}
