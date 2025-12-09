package weebify.dptb2utils.utils;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Pure JNA interface: only method signatures. No INSTANCE field here because interface
 * fields are implicitly final.
 */
@Deprecated
public interface TinyFDJNA extends Library {
    // C signature:
    // const char * tinyfd_openFileDialog(const char * aTitle, const char * aDefaultPathAndFile,
    //                                    const char * aFilterPatterns[], const char * aSingleFilterDescription, int aAllowMultipleSelects);
    String tinyfd_openFileDialog(Pointer aTitle, Pointer aDefaultPathAndFile, Pointer aFilterPatterns, Pointer aSingleFilterDescription, int aAllowMultipleSelects);
}