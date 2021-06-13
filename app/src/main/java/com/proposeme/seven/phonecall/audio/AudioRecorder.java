package com.proposeme.seven.phonecall.audio;


import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;

/**
 * Describe: Effectuer un enregistrement audio Ceci est le fichier d'entrée pour l'enregistrement audio
 */
public class AudioRecorder  implements Runnable {
    String LOG = "Recorder";
    //Objet d'enregistrement audio
    private AudioRecord audioRecord;

    // Objet Singleton。
    private static AudioRecorder mAudioRecorder;
    //Annulation d'écho
    private AcousticEchoCanceler canceler;

    private AudioRecorder(){
    }

    //Enregistre
    private volatile boolean isRecording = false;

    // Obtenir une instance du modèle singleton。
    public static AudioRecorder getAudioRecorder(){
        if (mAudioRecorder == null){
            mAudioRecorder = new AudioRecorder();
        }
        return mAudioRecorder;
    }

    //Logique pour démarrer l'enregistrement
    public void startRecording() {
        Log.e("ccc", "Commencer l'enregistrement");
        //Calculer la taille du cache
        int audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE, AudioConfig.PLAYER_CHANNEL_CONFIG2, AudioConfig.AUDIO_FORMAT);
        //Instancier l'objet d'enregistrement
        if (null == audioRecord && audioBufSize != AudioRecord.ERROR_BAD_VALUE) {
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.PLAYER_CHANNEL_CONFIG2,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);
        }
        //Traitement d'écho
        assert audioRecord != null;
        initAEC(audioRecord.getAudioSessionId());
        new Thread(this).start();
    }

    // Désactiver l'enregistrement
    public void stopRecording() {
        this.isRecording = false;
    }

    // Enregistre、
    public boolean isRecording() {
        return this.isRecording;
    }

    //Élimine l'écho
    public boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        if (!AcousticEchoCanceler.isAvailable()){
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }

    @Override
    public void run() {

        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            return;
        }
        //Avant l'enregistrement, instanciez une classe d'encodage et réalisez l'envoi de données dans la classe d'encodage.
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding();
        audioRecord.startRecording();

        this.isRecording = true;
        Log.e("ccc", "Commencer le codage");
        int size = Speex.getInstance().getFrameSize();

        short[] samples = new short[size];

        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            if (bufferRead > 0) {
                encoder.addData(samples,bufferRead);
            }
        }
        encoder.stopEncoding();
    }
}
