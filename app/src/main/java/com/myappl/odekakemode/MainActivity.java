package com.myappl.odekakemode;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String CLASS_NAME=getClass().getSimpleName();
    private FragmentManager mFragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( CLASS_NAME, "onCreate() start." );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //fragmentで！！
//api19        mFragmentManager = getSupportFragmentManager();
        mFragmentManager = getSupportFragmentManager();
        WiFiOnOffFragment wiFiOnOffFragment = new WiFiOnOffFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add( R.id.topViewGroup, wiFiOnOffFragment );
//        fragmentTransaction.addToBackStack( null );
        fragmentTransaction.commit();
    }

    @Override
    protected void onRestart() {
        Log.d( CLASS_NAME, "onRestart() start." );
        List<Fragment> fragmentList = mFragmentManager.getFragments();
        if ( fragmentList.size()<=0 ) { return; }
        for ( int i=0; i<fragmentList.size(); i++ ) {
            Fragment fragment = fragmentList.get( i );
            if ( fragment instanceof WiFiOnOffFragment ) {
                //activity から wifi 設定する指定がある場合（OFFにする時呼び出してます。かっこ悪い・・・）
                if ( ((WiFiOnOffFragment) fragment).getStatusSetFromActivity() ) {
                    boolean state = ((WiFiOnOffFragment) fragment).getWiFiStatus();
                    ((WiFiOnOffFragment) fragment).setWifi( state );
                }
            }
        }
        super.onRestart();
    }
}
