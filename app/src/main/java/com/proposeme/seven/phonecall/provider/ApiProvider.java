package com.proposeme.seven.phonecall.provider;

import android.util.Log;

import com.proposeme.seven.phonecall.audio.AudioPlayer;
import com.proposeme.seven.phonecall.audio.AudioRecorder;
import com.proposeme.seven.phonecall.net.Message;
import com.proposeme.seven.phonecall.net.NettyClient;
import com.proposeme.seven.phonecall.net.NettyReceiverHandler;

/**
 * Describe: Fournissez une API de connexion réseau et une API de logique de contrôle, qui sont toutes prises en charge dans le service.
 * Lors de l'enregistrement, vous devez définir la valeur de TargetIP.
 */

public class ApiProvider {

    private NettyClient nettyClient; //Initialiser l'agent d'envoi réseau
    private static ApiProvider provider;
    private String targetIP = null; // adresse cible。

    // Mode singleton。
    public static ApiProvider getProvider() {
        if (provider == null) {
            provider = new ApiProvider();
        }
        return provider;
    }

    private AudioRecorder mAudioRecorder;   //enregistreur
    private AudioPlayer mAudioPlayer;       // joueur。

    //Méthode de construction
    private ApiProvider() {
        // 1 Configurez les informations client, l'adresse IP cible et le port.
        nettyClient = NettyClient.getClient();
        mAudioRecorder = AudioRecorder.getAudioRecorder();
        mAudioPlayer = AudioPlayer.getInstance();
        provider = this;
    }

    /**
     *  Enregistrer le rappel
     * @param callback Variable de rappel。
     */
    public void registerFrameResultedCallback(NettyReceiverHandler.FrameResultedCallback callback){
        nettyClient.setFrameResultedCallback(callback);
    }

    /**
     * 发送音频数据
     * @param data 音频流
     */
    public void sendAudioFrame(byte[] data) {
        if (targetIP!= null)
            nettyClient.UserIPSendData(targetIP, data, Message.MES_TYPE_AUDIO);
        //需要处理为空的异常。
    }

    /**
     *  Envoyer des données en définissant l'adresse IP par défaut。
     * @param msg message
     */
    public void sentTextData(String msg) {
        if (targetIP != null)
            nettyClient.UserIPSendData(targetIP, msg, Message.MES_TYPE_NORMAL);
        Log.e("ccc","Envoyer un message" + msg );
    }

    /**
     * Envoyer des messages texte via l'adresse IP spécifiée
     * @param targetIp IP cible
     * @param msg Message texte。
     */
    public void UserIPSentTextData(String targetIp, String msg) {
        if (targetIp != null)
            nettyClient.UserIPSendData(targetIp, msg, Message.MES_TYPE_NORMAL);
        Log.e("ccc","Envoyer un message" + msg );
    }


    /**
     * Envoyer des informations audio via une adresse IP spécifiée
     * @param targetIp IP cible
     * @param data données
     */
    public void UserIpSendAudioFrame(String targetIp ,byte[] data) {
        if (targetIp != null)
            nettyClient.UserIPSendData(targetIp ,data, Message.MES_TYPE_AUDIO);
    }

    /**
     * Fermez le client Netty，
     */
    public void shutDownSocket(){
        nettyClient.shutDownBootstrap();
    }

    /**
     *  Fermez la connexion et mettez fin à l'appel
     * @return true or false
     */
    public boolean disConnect(){
        return  nettyClient.DisConnect();
    }

    /**
     *  Obtenez l'adresse cible
     * @return Adresse cible。
     */
    public String getTargetIP() {
        return targetIP;
    }

    /**
     *  Définir l'adresse cible
     * @param targetIP Définissez l'adresse de destination。
     */
    public void setTargetIP(String targetIP) {
        this.targetIP = targetIP;
    }

    /**
     * Démarrer l'enregistrement Avant de commencer les opérations suivantes, l'adresse IP cible doit être définie correctement, sinon des problèmes se produiront.
     */
    public void startRecord(){
        mAudioRecorder.startRecording();
    }

    /**
     * Arrête d'enregistrer
     */
    public void  stopRecord(){
        mAudioRecorder.stopRecording();
    }

    /**
     *  Si le fil d'enregistrement est en train d'enregistrer
     * @return true enregistrement or false Pas d'enregistrement
     */
    public boolean isRecording(){
        return mAudioRecorder.isRecording();
    }

    /**
     * Commencer la lecture audio
     */
    public void startPlay(){
        mAudioPlayer.startPlaying();
    }

    /**
     * Arrêter la lecture audio
     */
    public void stopPlay(){
        mAudioPlayer.stopPlaying();
    }

    /**
     *  Est-ce que ça joue
     * @return true Lecture en cours;  false Arrêtez de jouer
     */
    public boolean isPlaying(){
        return mAudioPlayer.isPlaying();
    }


    /**
     *  Activer l'enregistrement et la lecture
     */
    public void startRecordAndPlay(){
        startPlay();
        startRecord();
    }

    /**
     * Désactiver l'enregistrement et la lecture
     */
    public void stopRecordAndPlay(){
        stopRecord();
        stopPlay();
    }
}
