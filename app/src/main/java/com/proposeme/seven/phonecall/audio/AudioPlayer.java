package com.proposeme.seven.phonecall.audio;


import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describe:  Acceptez le flux audio décodé pour la lecture
 */
public class AudioPlayer implements  Runnable{
    String LOG = "AudioPlayer ";
    private static AudioPlayer player;

    private List<AudioData> dataList;
    private AudioData playData;
    private  volatile  boolean isPlaying = false;

    private AudioTrack audioTrack;

    //Enregistrer et lire des fichiers multimédias。
    private File file;
    private FileOutputStream fos;

    private AudioPlayer() {
        //Créer une liste doublement liée
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());

        //Test de lecture de fichier。
        File mFile = new File(Environment.getExternalStorageDirectory(),"/audio");
        if (!mFile.exists()){
            if (mFile.mkdirs()){
                Log.d("ggh","创建成功");
            }else {
                Log.d("ggh","文件已存在！");
            }
        }

        file = new File(Environment.getExternalStorageDirectory(),"/audio/decode.amr");
        try {
            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Obtenez un exemple de lecture audio
    public static AudioPlayer getInstance() {
        if (player == null) {
            player = new AudioPlayer();
        }
        return player;
    }

    //Ajouter des données au flux de lecture
    public void addData(short[] rawData, int size) {
        AudioData decodedData = new AudioData();
        decodedData.setSize(size);
        short[] tempData = new short[size];
        System.arraycopy(rawData, 0, tempData, 0, size);
        decodedData.setRealData(tempData);
        dataList.add(decodedData);
    }

    /*
     * Initialiser les paramètres du lecteur
     */
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.PLAYER_CHANNEL_CONFIG2,
                AudioConfig.AUDIO_FORMAT);
        if (bufferSize < 0) {
            Log.e(LOG, LOG + "initialize error!");
            return false;
        }
        Log.i(LOG, "Player初始化的 buffersize大小" + bufferSize);

        //Définir les paramètres du lecteur
        //MODE_STREAM lire et jouer
        // STREAM_VOICE_CALL signifie jouer avec l'écouteur. STREAM_MUSIC indique le locuteur
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                AudioConfig.SAMPLERATE, AudioConfig.PLAYER_CHANNEL_CONFIG2,
                AudioConfig.AUDIO_FORMAT, bufferSize, AudioTrack.MODE_STREAM);

        // set volume:Régler le volume de lecture
        audioTrack.setStereoVolume(1.0f, 1.0f);
        audioTrack.play();
        return true;
    }

    //Ajout de données de lecture
    private void playFromList() throws IOException {
        while (isPlaying) {
            while (dataList.size() > 0) {
                playData = dataList.remove(0);
                audioTrack.write(playData.getRealData(), 0, playData.getSize());
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void startPlaying() {
        if (isPlaying) {
            Log.e(LOG, "Vérifiez que le lecteur est ouvert" + isPlaying);
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        this.isPlaying = true;
        //Initialiser le lecteur
        if (!initAudioTrack()) {
            Log.i(LOG, "L'initialisation du lecteur a échoué");
            return;
        }
        Log.e(LOG, "Commencer à jouer");
        try {
            playFromList(); //Commencer à jouer
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
        Log.d(LOG, LOG + "end playing");
    }

    public void stopPlaying() {
        this.isPlaying = false;
    }

    public boolean isPlaying(){
        return this.isPlaying;
    }
}
