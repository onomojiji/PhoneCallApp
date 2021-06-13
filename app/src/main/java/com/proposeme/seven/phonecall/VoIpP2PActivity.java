package com.proposeme.seven.phonecall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.proposeme.seven.phonecall.audio.AudioDecoder;
import com.proposeme.seven.phonecall.net.BaseData;
import com.proposeme.seven.phonecall.net.IPSave;
import com.proposeme.seven.phonecall.net.NettyReceiverHandler;
import com.proposeme.seven.phonecall.provider.ApiProvider;
import com.proposeme.seven.phonecall.service.VoIPService;
import com.proposeme.seven.phonecall.utils.NetUtils;

import static com.proposeme.seven.phonecall.net.BaseData.IFS;
import static com.proposeme.seven.phonecall.net.BaseData.PHONE_ANSWER_CALL;
import static com.proposeme.seven.phonecall.net.BaseData.PHONE_CALL_END;
import static com.proposeme.seven.phonecall.net.BaseData.PHONE_MAKE_CALL;

// Cette interface est pour appeler, répondre, sonner et basculer normalement
public class VoIpP2PActivity extends AppCompatActivity implements View.OnClickListener{

    private Chronometer timer; // Minuterie d'appel
    private CountDownTimer mCountDownTimer; //Minuterie de délai d'expiration des appels

    // Objet de contrôle API
    private ApiProvider provider;

    private boolean isAnswer = false;  //Répondre au téléphone
    private boolean isBusy = false;  //Si vous êtes en communication. vrai signifie occupé, faux signifie pas occupé.
    private String newEndIp = null; //Vérifiez si la fin de l'appel IP est légale.

    private EditText mEditText; //Enregistrer l'adresse IP d'entrée de l'utilisateur


    private VoIPService IPService;
    // Obtenez la référence de l'objet de service, obtenez le fournisseur
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IPService = ((VoIPService.MyBinder) service).getService();
            provider = IPService.getProvider();
            // Le réseau ne peut être initialisé qu'après obtention du fournisseur
            netInit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    // Logique de commutation d'état
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //Selon le fonctionnement personnalisé de la mémoire de marques, cette opération peut faire fonctionner le thread principal.
            if (msg.what == PHONE_MAKE_CALL) { //  Le destinataire reçoit
                if (!isBusy){ //S'il n'est pas occupé, passez à l'interface d'appel.
                    showRingView(); //Accédez à l'interface de la cloche.
                    isBusy = true;
                }
            }else if (msg.what == PHONE_ANSWER_CALL){ // L'expéditeur reçoit
                showTalkingView();
                provider.startRecordAndPlay();
                isAnswer = true; //L'appel connecté est vrai
                mCountDownTimer.cancel(); // Désactivez le compte à rebours.
            }else if (msg.what == PHONE_CALL_END){ //Le destinataire et l'expéditeur peuvent recevoir le message de fin d'appel.
                if (newEndIp.equals(provider.getTargetIP())){
                    showBeginView();
                    isAnswer = false;
                    isBusy = false;
                    provider.stopRecordAndPlay();
                    timer.stop();
                }
            }
        }
    };

    //Activité de saut
    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, VoIpP2PActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        //Obtenez l'IP d'appel, puis mettez à jour l'interface.
        setContentView(R.layout.activity_voip_p2p);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        findViewById(R.id.begin_view).setVisibility(View.GONE);
        findViewById(R.id.user_input_ip_view).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.create_ip_addr)).setText(BaseData.LOCALHOST);
        timer = findViewById(R.id.timer);

        //Définir le bouton de raccrochage
        findViewById(R.id.calling_hangup).setOnClickListener(this);
        findViewById(R.id.talking_hangup).setOnClickListener(this);
        findViewById(R.id.ring_pickup).setOnClickListener(this);
        findViewById(R.id.ring_hang_off).setOnClickListener(this);
        //Définir l'ip d'entrée manuelle
        findViewById(R.id.Create_button).setOnClickListener(this);
        findViewById(R.id.user_input_phoneCall).setOnClickListener(this);

        mEditText = findViewById(R.id.user_input_TargetIp);
        showBeginView();//Afficher l'interface initiale

        //Compte à rebours pour les appels. Compte à rebours 10s
        mCountDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                if (!isAnswer){ //Si personne ne répond, raccroche
                    hangupOperation();
                    Toast.makeText(VoIpP2PActivity.this,"L'appel a expiré, veuillez réessayer plus tard!",Toast.LENGTH_SHORT).show();
                }
            }
        };
        //Démarrez le service.
        Intent intent = new Intent(this,VoIPService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);

        // Vérifiez s'il s'agit d'une activité démarrée par un service.
        boolean isFromService = getIntent().getBooleanExtra(IFS,false);
        if (isFromService){
            showRingView(); // Affichez l'interface de l'appel.
            isBusy = true;
        }
    }

    /**
     *  Désactiver automatiquement la méthode de saisie
     * @param act Activité actuel
     * @param v Contrôle lié。
     */
    public void hideOneInputMethod(Activity act, View v) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //Opération d'initialisation du réseau
    private void netInit(){
        // Enregistrer le rappel d'interface。
        provider.registerFrameResultedCallback(new NettyReceiverHandler.FrameResultedCallback() {
            //Recevoir la signalisation d'appel
            @Override
            public void onTextMessage(String msg) {

                // Il existe trois types d'informations envoyées: 100 200 300
                mHandler.sendEmptyMessage(Integer.parseInt(msg));
                /*
                 PHONE_MAKE_CALL = 100; //composer le numéro
                 PHONE_ANSWER_CALL = 200; //Répondre à l'appel // Vous devez changer d'interface à ce moment. Coupez l'appel dans une interface d'appel.
                 PHONE_CALL_END = 300; //appel terminé
                */
            }

            // Pour l'enregistrement, vous devez connaître l'adresse IP de l'autre partie pour envoyer le flux audio, et pour la lecture, il vous suffit de démarrer le fil pour la lecture.
            @Override
            public void onAudioData(byte[] data) {
                if (isAnswer){
                    AudioDecoder.getInstance().addData(data, data.length);
                }
            }

            //Obtenez l'adresse IP à laquelle l'autre partie est retournée
            @Override
            public void onGetRemoteIP(String ip) {
                newEndIp = ip; //每次都会记录新的ip。
                Log.e("ccc", "Recevoir l'adresse IP de l'autre partie" + ip);
                if ((!ip.equals("")) && (!isBusy)){  //Si vous êtes occupé, vous ne pouvez pas changer votre adresse IP. Ne peut faire aucune opération de réponse.
                    provider.setTargetIP(ip);
                }
            }
        });
    }

    //Comment déclencher en cliquant sur le bouton retour
    @Override
    public void onBackPressed(){

        // À ce stade, vous devez vous inscrire et réécouter。
        hangupOperation();// Raccrochez aussi à ce moment。
        IPService.registerCallBack(); //Réenregistrer l'auditeur pour écouter la demande d'appel。
        timer.stop();
        finish(); //Assurez-vous d'appeler la méthode de sortie plus tard
    }

    // Afficher l'interface initiale
    private void showBeginView(){
        findViewById(R.id.begin_view).setVisibility(View.VISIBLE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.user_input_ip_view).setVisibility(View.GONE);
    }

    // Afficher l'interface IP d'entrée utilisateur
    private void showUserInputIpView(){
        findViewById(R.id.user_input_ip_view).setVisibility(View.VISIBLE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.begin_view).setVisibility(View.GONE);
        // Trouvez l'adresse IP dans le cache。
        mEditText.setText(IPSave.getIP(this));
    }
    // Afficher la vue lors de l'appel
    private void showCallingView(){
        findViewById(R.id.calling_view).setVisibility(View.VISIBLE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        findViewById(R.id.begin_view).setVisibility(View.GONE);
        findViewById(R.id.user_input_ip_view).setVisibility(View.GONE);

        //Démarrer la minuterie。
        mCountDownTimer.start();
    }
    //Afficher la vue lorsque vous parlez
    private void showTalkingView(){

        findViewById(R.id.talking_view).setVisibility(View.VISIBLE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.ring_view).setVisibility(View.GONE);
        findViewById(R.id.begin_view).setVisibility(View.GONE);
        findViewById(R.id.user_input_ip_view).setVisibility(View.GONE);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    //Afficher l'interface de la cloche
    private void showRingView(){
        findViewById(R.id.ring_view).setVisibility(View.VISIBLE);
        findViewById(R.id.calling_view).setVisibility(View.GONE);
        findViewById(R.id.talking_view).setVisibility(View.GONE);
        findViewById(R.id.begin_view).setVisibility(View.GONE);
        findViewById(R.id.user_input_ip_view).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.disConnect();
    }

    //Définir l'événement de clic
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ring_pickup: //Répondez à l'appel sur l'interface de sonnerie
                showTalkingView();
                provider.sentTextData(PHONE_ANSWER_CALL.toString());
                // Commencer à envoyer un message vocal
                provider.startRecordAndPlay();
                isAnswer = true; //L'appel connecté est vrai
                break;
            case R.id.calling_hangup: //Raccrocher pendant la numérotation
                hangupOperation();
                break;
            case R.id.talking_hangup: //Raccrocher pendant un appel
                hangupOperation();
                break;
            case R.id.ring_hang_off: //Raccrocher pendant la sonnerie
                hangupOperation();
                break;
            case R.id.Create_button: //Saisissez manuellement l'adresse IP
                showUserInputIpView();
                break;
            case R.id.user_input_phoneCall: // Entrée pour passer un appel
                //Obtenir l'adresse IP
                String ip = mEditText.getText().toString();
                // Vérifiez s'il s'agit d'une adresse IP légitime
                if (NetUtils.ipCheck(ip)){
                    provider.setTargetIP(ip);
                    //1 Afficher l'interface de numérotation
                    showCallingView();
                    isBusy = true;
                    //2 Envoyer un message pour passer un appel。
                    provider.sentTextData(PHONE_MAKE_CALL.toString());
                    IPSave.saveIP(this, ip); //Enregistrer l'IP
                    hideOneInputMethod(this,mEditText); // Masquer la méthode de saisie
                }else {
                    Toast.makeText(this,"Le format IP est incorrect, veuillez saisir à nouveau ~",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //Logique lors du raccrochage du téléphone
    private void hangupOperation(){
        provider.sentTextData(PHONE_CALL_END.toString());
        isBusy = false;
        showBeginView();
        isAnswer = false;
        provider.stopRecordAndPlay();
        timer.stop();
        mCountDownTimer.cancel();
    }

}
