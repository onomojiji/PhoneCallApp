package com.proposeme.seven.phonecall.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.proposeme.seven.phonecall.VoIpP2PActivity;
import com.proposeme.seven.phonecall.net.NettyReceiverHandler;
import com.proposeme.seven.phonecall.provider.ApiProvider;

import static com.proposeme.seven.phonecall.net.BaseData.IFS;
import static com.proposeme.seven.phonecall.net.BaseData.PHONE_MAKE_CALL;

/**
 * Prise en charge de l'arrière-plan vocal, lorsqu'il y a un appel vocal,
 * il passera directement à l'interface correspondante
 */
public class VoIPService extends Service {

    //Variables pour la lecture audio
    private ApiProvider provider; // Le seul effet est de sauvegarder cette variable ici.

    public VoIPService() {

    }

    // Créez un filet à écouter sur le port.
    @Override
    public void onCreate() {
        super.onCreate();
        provider = ApiProvider.getProvider();
        registerCallBack();
    }

    /**
     *  Cette interface écoute simplement les requêtes PHONE_MAKE_CALL，
     */
    public void registerCallBack(){
        provider.registerFrameResultedCallback(new NettyReceiverHandler.FrameResultedCallback() {

            @Override
            public void onTextMessage(String msg) {
                Log.e("ccc", "Reçu les nouvelles" + msg);
                if (Integer.parseInt(msg) == PHONE_MAKE_CALL){
                    startActivity();
                }
            }

            @Override
            public void onAudioData(byte[] data) {

            }

            @Override
            public void onGetRemoteIP(String ip) {
                if ((!ip.equals(""))){  // Lorsque l'adresse IP n'est pas vide, modifiez l'adresse IP dans le fournisseur.
                    provider.setTargetIP(ip);
                }
            }
        });
    }

    public ApiProvider getProvider() {
       return provider;
    }

    /**
     * Renvoyer un objet Binder
     */
    @Override
    public IBinder onBind(Intent intent) {

        return new MyBinder();
    }
    // De cette manière, l'objet de service peut être obtenu directement de l'extérieur, puis il peut être directement actionné.

    //1. Il existe une classe en service qui hérite de Binder, puis fournit une méthode publique pour renvoyer une instance du service actuel.
    public class  MyBinder extends Binder {
        public VoIPService getService(){
            return VoIPService.this;
        }
    }

    // Démarrez un service à partir du service.
    private void  startActivity(){
        Intent intent = new Intent(this,VoIpP2PActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IFS,true);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fermer tous les composants。
        provider.shutDownSocket();
        provider.stopRecordAndPlay();
    }
}
