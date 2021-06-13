package com.proposeme.seven.phonecall.audio;

import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;
import com.proposeme.seven.phonecall.provider.ApiProvider;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe: Surveillez le flux audio collecté et encodez-le, puis appelez EncodeProvider pour envoyer les données
 */
public class AudioEncoder implements Runnable{
    String LOG = "AudioEncoder";
    //Objet de construction de modèle singleton
    private static AudioEncoder encoder;
    //Est en train de coder
    private volatile boolean isEncoding = false;

    //Une collection de données audio pour chaque image
    private List<AudioData> dataList = null;

    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    //Stocker les données enregistrées
    public void addData(short[] data, int size) {
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        short[] tempData = new short[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }

    /**
     * Commencer le codage。
     */
    public void startEncoding() {

        Log.e("ccc", "Le sous-thread de codage démarre");
        if (isEncoding) {
            Log.e(LOG, "encoder has been started  !!!");
            return;
        }
        //Ouvrir le fil enfant
        new Thread(this).start();
    }

    /**
     * end encoding	Arrêter le codage
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    @Override
    public void run() {
        int encodeSize = 0;
        byte[] encodedData;
        isEncoding = true;
        while (isEncoding) {
            if (dataList.size() == 0) { //S'il n'y a pas de données encodées, attendez et relâchez le thread
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[Speex.getInstance().getFrameSize()];
                encodeSize = Speex.getInstance().encode(rawData.getRealData(),
                        0, encodedData, rawData.getSize());
                if (encodeSize > 0) {
                    //Réaliser l'envoi de données。
                    if (ApiProvider.getProvider()!=null)
                        ApiProvider.getProvider().sendAudioFrame(encodedData); //Envoyer les données d'enregistrement
                }
            }
        }
    }
}
