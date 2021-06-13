package com.proposeme.seven.phonecall.utils.mixAduioUtils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Describe: Outils de fichiers
 * Réalisez pour obtenir le chemin de base, réalisez que le fichier est lu dans le tableau d'octets sous forme d'octets.
 */
public class FileUtils {

    private static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";

    public static String getFileBasePath(){
        return basePath;
    }

    //Lire le flux de fichiers dans un tableau，
    public static byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            Log.d("ccc","file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        //La boucle while fera en sorte que la lecture continue à lire, fi.read () retournera -1 après la lecture des données
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        //Assurez-vous que toutes les données sont lues
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }

}
