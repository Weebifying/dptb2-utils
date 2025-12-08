package weebify.dptb2utils.utils;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import weebify.dptb2utils.DPTB2Utils;

public interface TinyFDJNA extends Library {
    // Load the native "tinyfd" library we've System.load()'ed above
    TinyFDJNA INSTANCE = (TinyFDJNA) Native.loadLibrary("tinyfd", TinyFDJNA.class);

    // C signature:
    // const char * tinyfd_openFileDialog(const char * aTitle, const char * aDefaultPathAndFile,
    //                                    const char * aFilterPatterns[], const char * aSingleFilterDescription, int aAllowMultipleSelects);
    String tinyfd_openFileDialog(Pointer aTitle, Pointer aDefaultPathAndFile, Pointer aFilterPatterns, Pointer aSingleFilterDescription, int aAllowMultipleSelects);

    /**
     * Convenience wrapper that initializes the native, builds the C-style filter pointer array,
     * and calls tinyfd_openFileDialog. Keeps JNA usage minimal for compatibility with older JNA.
     */
    static String openFileDialog(String title, String startDir, String[] filters, String description, boolean allowMultiple) {
        try {
            TinyFDJNALoader.initialize();
        } catch (Throwable t) {
            DPTB2Utils.LOGGER.error("TinyFDJNA: failed to initialize native libs", t);
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
        Memory[] mems = null;
        if (filters != null && filters.length > 0) {
            int ptrSize = Native.POINTER_SIZE;
            Memory arr = new Memory((long) ptrSize * (filters.length + 1)); // null terminated
            mems = new Memory[filters.length];
            for (int i = 0; i < filters.length; i++) {
                byte[] fs = filters[i].getBytes();
                mems[i] = new Memory(fs.length + 1);
                mems[i].setString(0, filters[i]);
                arr.setPointer((long) i * ptrSize, mems[i]);
            }
            arr.setPointer((long) filters.length * ptrSize, Pointer.NULL);
            pointerArray = arr;
        }

        try {
            String res = INSTANCE.tinyfd_openFileDialog(cTitle, cStart, pointerArray, cDesc, allowMultiple ? 1 : 0);
            if (res == null || res.isEmpty()) return null;
            return res;
        } catch (Throwable t) {
            DPTB2Utils.LOGGER.error("TinyFDJNA: tinyfd_openFileDialog failed", t);
            return null;
        }
    }
}