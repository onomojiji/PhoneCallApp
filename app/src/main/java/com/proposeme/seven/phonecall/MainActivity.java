package com.proposeme.seven.phonecall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gyz.voipdemo_speex.util.Speex;
import com.proposeme.seven.phonecall.net.BaseData;
import com.proposeme.seven.phonecall.service.VoIPService;
import com.proposeme.seven.phonecall.utils.PermissionManager;
import com.yanzhenjie.permission.Permission;

import butterknife.ButterKnife;


import static com.proposeme.seven.phonecall.utils.NetUtils.getIPAddress;

//L'interface principale du téléphone p2p。
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BaseData.LOCALHOST = getIPAddress(this); //Obtenez l'adresse IP locale
        Speex.getInstance().init();


        findViewById(R.id.phoneCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoIpP2PActivity.newInstance(MainActivity.this);
            }
        });

        //Cliquez pour passer à l'appel. Cliquez pour tester la conférence téléphonique à plusieurs.
        /*findViewById(R.id.Multi_phoneCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiVoIpActivity.newInstance(MainActivity.this);
            }
        });*/


        ((TextView)findViewById(R.id.create_ip_addr)).setText(BaseData.LOCALHOST);
        initPermission();

        // Démarrer le service
        Intent intent = new Intent(this,VoIPService.class);
        startService(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Événement d'autorisation initiale
     */
    private void initPermission() {
        //Vérifier les autorisations
        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
            @Override
            public void permissionSuccess() {
                PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                    @Override
                    public void permissionSuccess() {
                        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                            @Override
                            public void permissionSuccess() {


                            }
                            @Override
                            public void permissionFailed() {
                            }
                        }, Permission.Group.STORAGE);
                    }

                    @Override
                    public void permissionFailed() {

                    }
                }, Permission.Group.MICROPHONE);
            }

            @Override
            public void permissionFailed() {

            }
        }, Permission.Group.CAMERA);
    }
}
