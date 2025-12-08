package weebify.dptb2utils.utils;

import weebify.dptb2utils.DPTB2Utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class TinyFDJNALoader {
    private static boolean initialized = false;
    private static Path nativeDir;

    public static synchronized void initialize() {
        if (initialized) return;

        try {

            nativeDir = Files.createTempDirectory("tinyfd-natives");
            nativeDir.toFile().deleteOnExit();

            String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            DPTB2Utils.LOGGER.info("TinyFDJNALoader: os = {}", os);

            String resource;
            if (os.contains("win")) {
                resource = "/tinyfd/natives-windows/tinyfd.dll";
            } else if (os.contains("mac") || os.contains("darwin")) {
                resource = "/tinyfd/natives-macos/libtinyfd.dylib";
            } else {
                resource = "/tinyfd/natives-linux/libtinyfd.so";
            }

            InputStream in = TinyFDJNALoader.class.getResourceAsStream(resource);
            if (in == null) {
                throw new IllegalStateException("TinyFDJNALoader: missing resource: " + resource);
            }

            String fileName = Paths.get(resource).getFileName().toString();
            Path target = nativeDir.resolve(fileName);
            try (InputStream is = in; FileOutputStream out = new FileOutputStream(target.toFile())) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) out.write(buf, 0, r);
            }

            // Make sure the extracted file is readable/executable on Unix
            try {
                target.toFile().setReadable(true, false);
                target.toFile().setExecutable(true, false);
            } catch (Exception ignored) {}

            DPTB2Utils.LOGGER.info("TinyFDJNALoader: extracted native to {}", target.toAbsolutePath());

            // Load it explicitly so JNA will find the symbol table we expect.
            try {
                System.load(target.toAbsolutePath().toString());
                DPTB2Utils.LOGGER.info("TinyFDJNALoader: System.load() succeeded for {}", target.toAbsolutePath());
            } catch (UnsatisfiedLinkError ule) {
                DPTB2Utils.LOGGER.error("TinyFDJNALoader: System.load() failed for {}", target.toAbsolutePath(), ule);
                throw ule;
            }

            initialized = true;
        } catch (Exception e) {
            DPTB2Utils.LOGGER.error("TinyFDJNALoader: Unhandled exception caught!");
            e.printStackTrace();
        }
    }
}