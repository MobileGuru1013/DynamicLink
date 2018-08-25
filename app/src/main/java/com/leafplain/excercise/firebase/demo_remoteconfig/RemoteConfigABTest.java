package com.leafplain.excercise.firebase.demo_remoteconfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.leafplain.excercise.firebase.BuildConfig;
import com.leafplain.excercise.firebase.R;
import com.leafplain.excercise.firebase.util.DebugLog;

/**
 * Created by kennethyeh on 2017/1/18.
 */

public class RemoteConfigABTest extends AppCompatActivity {
    private static final String TAG = "RemoteConfigABTest";
    private Context mContext;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    private  long cacheExpiration = 3; // 1 hour in seconds.
    private static final String UI_VARIANT_KEY = "ui_variant";
    private static final String UI_VARIANT_VALUE_A          = "variant_a";
    private static final String UI_VARIANT_VALUE_B          = "variant_b";
    private static final String UI_VARIANT_VALUE_DEFAULT    = "variant_default";

    private static final String User_Property_UIExperiment = "UIExperiment";

    private TextView mConfingTV;
    private RelativeLayout mUiRL;
    private TextView mUiTV;
    private String uiExperiment="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_config_ab_test);
        mContext    = this;
        mConfingTV  = (TextView) findViewById(R.id.confingTV);
        mUiRL       = (RelativeLayout) findViewById(R.id.uiRL);
        mUiTV       = (TextView) findViewById(R.id.uiTV);


        mFirebaseAnalytics      = FirebaseAnalytics.getInstance(this);
        mFirebaseRemoteConfig   = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config);

        fetchConfigUI();
    }

    private void fetchConfigUI(){
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DebugLog.d(TAG, "Fetch Succeeded");


                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            DebugLog.d(TAG, "Fetch Failed");
                        }
                        displayConfigUI();
                    }
                });
        // [END fetch_config_with_callback]
    }

    private void displayConfigUI(){
        uiExperiment = mFirebaseRemoteConfig.getString(UI_VARIANT_KEY);

        mFirebaseAnalytics.setUserProperty(User_Property_UIExperiment, uiExperiment);
        String display = "App Ver. "+BuildConfig.VERSION_NAME+"\n"+" UI Config : "+uiExperiment;
        mConfingTV.setText(display);
        mUiTV.setText(uiExperiment);
        DebugLog.d(TAG, "displayConfigUI:"+uiExperiment);
        if (uiExperiment.equals(UI_VARIANT_VALUE_A)) {
            mUiRL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (uiExperiment.equals(UI_VARIANT_VALUE_B)) {
            mUiRL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        } else if (uiExperiment.equals(UI_VARIANT_VALUE_DEFAULT)) {
            mUiRL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.cover_bg_70));
        }
        mUiRL.setOnClickListener(uiClick);
    }

    private View.OnClickListener uiClick = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            DebugLog.d(TAG, "uiClick:"+uiExperiment);
            try {
                    Snackbar.make(mUiRL, "click:"+uiExperiment, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }catch (Exception e){}

            if (uiExperiment.equals(UI_VARIANT_VALUE_A)) {
                mFirebaseAnalytics.logEvent(UI_VARIANT_VALUE_A, new Bundle());

            } else if (uiExperiment.equals(UI_VARIANT_VALUE_B)) {
                mFirebaseAnalytics.logEvent(UI_VARIANT_VALUE_B, new Bundle());

            } else if (uiExperiment.equals(UI_VARIANT_VALUE_DEFAULT)) {
                mFirebaseAnalytics.logEvent(UI_VARIANT_VALUE_DEFAULT, new Bundle());

            }
        }
    };
}
