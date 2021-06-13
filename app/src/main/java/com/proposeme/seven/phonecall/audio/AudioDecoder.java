package com.proposeme.seven.phonecall.audio;

import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.gyz.voipdemo_speex.util.Speex;
/**
 * Describe:  Selon le flux audio reçu, décodez et jouez.
 */
public class AudioDecoder implements Runnable {
    String LOG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private short[] decodedData;
    private volatile boolean isDecoding = false;
    private List<AudioData> dataList = null;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
        startDecoding();
    }


    public void addData(byte[] data, int size) {
        AudioData adata = new AudioData();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        adata.setReceiverdata(tempData);
        dataList.add(adata);
    }

    /**
     * startRecordAndPlay decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "Commencer le décodage");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {

        //Nouveau lecteur audio
        AudioPlayer player = AudioPlayer.getInstance();

        player.startPlaying();
        this.isDecoding = true;
        Log.d(LOG, LOG + "Initialiser le lecteur");
        int decodeSize = 0;
        while (isDecoding) {
            if (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                decodedData = new short[Speex.getInstance().getFrameSize()];
                byte[] data = encodedData.getReceiverdata(); //Obtenez les données reçues
                decodeSize = Speex.getInstance().decode(data, decodedData, data.length);
                if (decodeSize > 0) {
                    // Ajouter des données au lecteur
                    player.addData(decodedData, decodeSize);
                }
            }
        }
        System.out.println(LOG + "停止解码");
        // stopRecordAndPlay playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}
