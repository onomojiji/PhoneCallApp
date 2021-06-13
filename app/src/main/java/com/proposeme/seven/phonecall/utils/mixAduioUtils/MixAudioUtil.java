package com.proposeme.seven.phonecall.utils.mixAduioUtils;

import android.util.Log;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Describe: Travail de mélange
 */

public class MixAudioUtil {
    /**
     * Utilisation d'un algorithme moyen simple algorithme de mixage audio moyen
     * Le test a révélé que cet algorithme réduira le volume d'enregistrement
     * Mélangez l'algorithme pcm et enregistrez-le sous forme de fichier
     * Principe: La superposition de signaux vocaux quantifiés équivaut à la superposition d'ondes sonores dans l'air,
     * qui se reflète sur les données audio, c'est-à-dire que les valeurs du même canal sont simplement ajoutées
     */

    public static byte[] averageMix(String file1,String file2) throws IOException {

        byte[][] bMulRoadAudioes =  new byte[][]{
                FileUtils.getContent(file1),    //Premier fichier
                FileUtils.getContent(file2)     //Deuxième fichier
        };


        byte[] realMixAudio = bMulRoadAudioes[0]; //Sauvegardez les données après le mélange。
        Log.e("ccc", " bMulRoadAudioes length " + bMulRoadAudioes.length); //2
        //Déterminez si la taille des deux fichiers est la même, et si elles sont différentes, effectuez l'opération de remplissage
        for (int rw = 0; rw < bMulRoadAudioes.length; ++rw) { //La longueur est toujours égale à 2. La longueur du fichier et la longueur du fichier2 sont détectées tour à tour
            if (bMulRoadAudioes[rw].length != realMixAudio.length) {
                Log.e("ccc", "colonne du chemin de l'audio + " + rw + " est différente.");
                if (bMulRoadAudioes[rw].length<realMixAudio.length){
                    realMixAudio = subBytes(realMixAudio,0,bMulRoadAudioes[rw].length); //进行数组的扩展
                }
                else if (bMulRoadAudioes[rw].length>realMixAudio.length){
                    bMulRoadAudioes[rw] = subBytes(bMulRoadAudioes[rw],0,realMixAudio.length);
                }
            }
        }

        int row = bMulRoadAudioes.length;       //Ligne
        int column = realMixAudio.length / 2;   //Colonne
        short[][] sMulRoadAudioes = new short[row][column];
        for (int r = 0; r < row; ++r) {         //première moitié
            for (int c = 0; c < column; ++c) {
                sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
            }
        }
        short[] sMixAudio = new short[column];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < column; ++sc) {
            mixVal = 0;
            sr = 0;
            for (; sr < row; ++sr) {
                mixVal += sMulRoadAudioes[sr][sc];
            }
            sMixAudio[sc] = (short) (mixVal / row);
        }

        //Le mix synthétisé est enregistré dans realMixAudio
        for (sr = 0; sr < column; ++sr) { //Deuxième partie
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }

        //Enregistrez le PCM après le mélange
        FileOutputStream fos = null;
        //Enregistrez le fichier après la synthèse。
        File saveFile = new File(FileUtils.getFileBasePath()+ "averageMix.pcm" );
        if (saveFile.exists()) {
            saveFile.delete();
        }
        fos = new FileOutputStream(saveFile);// Créer un fichier avec des octets accessibles
        fos.write(realMixAudio);
        fos.close();// Fermer le flux d'écriture
        return realMixAudio; //Revenir au mix synthétisé。
    }

    //Combinez deux pistes audio。
    private static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }
}

