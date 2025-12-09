package weebify.dptb2utils.utils;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import weebify.dptb2utils.DPTB2Utils;

/**
 * Helper class that lazily loads the JNA binding and exposes a safe openFileDialog wrapper.
 * Holds the mutable instance in a class (not an interface) so it can be volatile/reassigned.
 */
@Deprecated
public final class TinyFD {
    private static volatile TinyFDJNA INSTANCE;

    private TinyFD() {}

    private static TinyFDJNA getInstance() {
        TinyFDJNA inst = INSTANCE;
        if (inst != null) return inst;

        synchronized (TinyFD.class) {
            if (INSTANCE != null) return INSTANCE;

            try {
                // Ensure native binary is extracted and System.load()'d
                TinyFDJNALoader.initialize();

                // Load the native library via JNA now that System.load() has been done
                INSTANCE = (TinyFDJNA) Native.loadLibrary("tinyfd", TinyFDJNA.class);
                DPTB2Utils.LOGGER.info("TinyFD: loaded native library via JNA");
                return INSTANCE;
            } catch (Throwable t) {
                DPTB2Utils.LOGGER.error("TinyFD: failed to load native library via JNA", t);
                throw new RuntimeException("Failed to load tinyfd native library", t);
            }
        }
    }

    public static String openFileDialog(String title, String startDir, String[] filters, String description, boolean allowMultiple) {
        TinyFDJNA api;
        try {
            api = getInstance();
        } catch (Throwable t) {
            DPTB2Utils.LOGGER.error("TinyFD: getInstance failed", t);
            return null;
        }

        Pointer cTitle = Pointer.NULL;
        if (title != null) {
            byte[] tb = title.getBytes();
            Memory m = new Memory(tb.length + 1);
            m.setString(0, title);
            cTitle = m;
        }

        Pointer cStart = Pointer.NULL;
        if (startDir != null) {
            byte[] sb = startDir.getBytes();
            Memory m = new Memory(sb.length + 1);
            m.setString(0, startDir);
            cStart = m;
        }

        Pointer cDesc = Pointer.NULL;
        if (description != null) {
            byte[] db = description.getBytes();
            Memory m = new Memory(db.length + 1);
            m.setString(0, description);
            cDesc = m;
        }

        Pointer pointerArray = Pointer.NULL;
        if (filters != null && filters.length > 0) {
            int ptrSize = Native.POINTER_SIZE;
            Memory arr = new Memory((long) ptrSize * (filters.length + 1)); // null terminated
            for (int i = 0; i < filters.length; i++) {
                byte[] fs = filters[i].getBytes();
                Memory fm = new Memory(fs.length + 1);
                fm.setString(0, filters[i]);
                arr.setPointer((long) i * ptrSize, fm);
            }
            arr.setPointer((long) filters.length * ptrSize, Pointer.NULL);
            pointerArray = arr;
        }

        try {
            String res = api.tinyfd_openFileDialog(cTitle, cStart, pointerArray, cDesc, allowMultiple ? 1 : 0);
            if (res == null || res.isEmpty()) return null;
            return res;
        } catch (Throwable t) {
            DPTB2Utils.LOGGER.error("TinyFD: tinyfd_openFileDialog failed", t);
            return null;
        }
    }
}