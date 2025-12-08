package weebify.dptb2utils.utils;

import weebify.dptb2utils.DPTB2Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class TinyFDLoader {
    private static boolean initialized = false;
    private static Path nativeDir;

    public static synchronized void initialize() {
        if (initialized) return;

        try {
            nativeDir = Files.createTempDirectory("dptb2-lwjgl3-natives");

            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();
            if (os.contains("wins")) {
                String suffix = arch.contains("64") ? "" : "32";
                extractAndLoad("lwjgl" + suffix + ".dll");
                extractAndLoad("lwjgl_tinyfd" + suffix + ".dll");
            } else if (os.contains("mac") || os.contains("darwin")) {
                extractAndLoad("liblwjgl.dylib");
                extractAndLoad("liblwjgl_tinyfd.dylib");
            } else {
                extractAndLoad("liblwjgl.so");
                extractAndLoad("liblwjgl_tinyfd.so");
            }

            System.setProperty("org.lwjgl.librarypath", nativeDir.toAbsolutePath().toString());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.walk(nativeDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } catch (IOException ignored) {}
            }));

            initialized = true;
            DPTB2Utils.LOGGER.info("LWJGL3 TinyFD natives loaded successfully!");
        } catch (Exception e) {
            DPTB2Utils.LOGGER.error("Failed to load LWJGL3 natives!");
        }
    }

    private static void extractAndLoad(String libName) throws IOException {
        Path targetPath = nativeDir.resolve(libName);

        InputStream in = TinyFDLoader.class.getResourceAsStream("/" + libName);

        if (in == null) {
            throw new IOException("Could not find native library in jar: /" + libName);
        }

        DPTB2Utils.LOGGER.info("Extracting native: {}", libName);
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }
}
