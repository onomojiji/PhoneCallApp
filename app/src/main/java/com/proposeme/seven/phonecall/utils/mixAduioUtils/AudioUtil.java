package com.proposeme.seven.phonecall.utils.mixAduioUtils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Describe: Cette classe d'outils est une classe d'outils de test pour le mixage audio,
 * 1 réalise l'enregistrement et 2 crée des fichiers et des dossiers.
 */
public class AudioUtil {
    private static AudioUtil mInstance;
    private AudioRecord recorder;
    //Source sonore
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    //Fréquence d'échantillonnage de l'enregistrement
    private static int audioRate = 44100;
    //Canal d'enregistrement, mono
    private static int audioChannel = AudioFormat.CHANNEL_IN_STEREO;
    //Précision de quantification
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //Taille du cache
    private static int bufferSize = AudioRecord.getMinBufferSize(audioRate , audioChannel , audioFormat);
    //Enregistrer l'état de lecture
    private boolean isRecording = false;
    //Réseau de signaux numériques
    private byte[] noteArray;
    //Fichier PCM
    private File pcmFile;
    //fichier wav
    private File wavFile;
    //Flux de sortie de fichier
    private OutputStream os;
    //Répertoire racine du fichier
    private String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";

    private AudioUtil()
    {
    }

    //Pour créer un dossier, créez d'abord un répertoire et construisez un nouveau fichier basé sur le nom de fichier transmis.
    public void createFile(String fileName)
    {

        File baseFile = new File(basePath);
        if (!baseFile.exists())
            baseFile.mkdirs(); //Créer un annuaire。

        pcmFile = new File(basePath + fileName);

        if (pcmFile.exists()) //Vérifiez si le fichier existe
            pcmFile.delete();

        try
        {
            pcmFile.createNewFile();  //Appelez cette méthode pour créer réellement le fichier。
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Obtenez une instance。
    public synchronized static AudioUtil getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new AudioUtil();
        }
        return mInstance;
    }

    //Lire le fil d'enregistrement des données numériques
    class WriteThread implements Runnable
    {
        @Override
        public void run()
        {
            writeData();
        }
    }

    //Le corps exécutif du thread d'enregistrement écrit les informations d'enregistrement dans le fichier。
    private void writeData()
    {
        noteArray = new byte[bufferSize];
        //Créer un flux de sortie de fichier
        try
        {
            //Créez d'abord un nouveau fichier de flux d'entrée, puis écrivez les données enregistrées dans le fichier pcm en octets.
            os = new BufferedOutputStream(new FileOutputStream(pcmFile));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        while (isRecording)
        {
            int recordSize = recorder.read(noteArray , 0 , bufferSize);
            if (recordSize > 0)
            {
                try
                {
                    os.write(noteArray);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (os != null)
        {
            try
            {
                os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //Démarrez l'enregistrement, définissez les paramètres d'enregistrement
    public void startRecord()
    {
        recorder = new AudioRecord(audioSource , audioRate ,
                audioChannel , audioFormat , bufferSize);
        isRecording = true;
        recorder.startRecording();
    }

    //Enregistrer les données
    public void recordData()
    {
        new Thread(new WriteThread()).start();
    }

    //Arrête d'enregistrer
    public void stopRecord()
    {
        if (recorder != null)
        {
            isRecording = false;
            recorder.stop(); //Libérer les ressources
            recorder.release();
            recorder = null;

        }
    }
}
