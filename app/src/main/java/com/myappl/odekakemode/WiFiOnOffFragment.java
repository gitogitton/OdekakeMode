package com.myappl.odekakemode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static android.content.Context.WIFI_SERVICE;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;


public class WiFiOnOffFragment extends Fragment {

    private final String CLASS_NAME= getClass().getSimpleName(); //Class name
    static final int PICK_CONTACT_REQUEST = 1;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private View mView;
    private WifiManager mWifiManager;
    private boolean mWifiStatus;
    private boolean mStatusSetFromActivity = false; //true : activity側からwifi設定する

    public WiFiOnOffFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WiFiOnOffFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WiFiOnOffFragment newInstance(String param1, String param2) {
        WiFiOnOffFragment fragment = new WiFiOnOffFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d( CLASS_NAME, "onCreate() start." );
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d( CLASS_NAME, "onCreateView() start." );
        // Inflate the layout for this fragment
        return mView = inflater.inflate(R.layout.fragment_wi_fi_on_off, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d( CLASS_NAME, "onViewCreated() start." );
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService( WIFI_SERVICE ); //wifi manager 取得
        //固定メッセージ表示
        TextView textViewMesage = view.findViewById( R.id.message_01 );
        //textViewMesage.setText( "※）ご注意！！" );
        //現在のWi-Fiの設定に合わせてトグルボタンを初期セット
        mWifiStatus = false;
        switch( mWifiManager.getWifiState() ) {
            case WIFI_STATE_DISABLED :
            case WIFI_STATE_DISABLING :
                mWifiStatus = false;
                break;
            case WIFI_STATE_ENABLED :
            case WIFI_STATE_ENABLING :
                mWifiStatus = true;
                break;
            default:
                break;
        }
        Log.d( CLASS_NAME, "Wi-Fi Status : "+( mWifiStatus?"wifi ON":"wifi OFF" ) );
        CompoundButton toggleButton = view.findViewById( R.id.toggleWiFiOnOff ); //toggleButtonのviewを取得
        toggleButton.setChecked( mWifiStatus );

        //2018.8.16 追加
        //ボリューム設定表示
        displayVolumeInfo( view );

        // ==================================================================
        // SeekBar 表示
        // ==================================================================
        //seekbar 取得
        SeekBar seekBarAlarm = mView.findViewById( R.id.seekBarAlarm );
        SeekBar seekBarRing = mView.findViewById( R.id.seekBarRing );
        SeekBar seekBarNotification = mView.findViewById( R.id.seekBarNotification );
        AudioManager am = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
        if ( am != null ) {
            //seekbarの最大値設定
            int alarmMax = am.getStreamMaxVolume( AudioManager.STREAM_ALARM );
            int ringMax = am.getStreamMaxVolume( AudioManager.STREAM_RING );
            int notificationMax = am.getStreamMaxVolume( AudioManager.STREAM_NOTIFICATION );
            seekBarAlarm.setMax( alarmMax );
            seekBarRing.setMax( ringMax );
            seekBarNotification.setMax( notificationMax );
            //現在値取得
            int alarmSetting = am.getStreamVolume( AudioManager.STREAM_ALARM );
            int ringSetting = am.getStreamVolume( AudioManager.STREAM_RING );
            int notificationSetting = am.getStreamVolume( AudioManager.STREAM_NOTIFICATION );
            //SeekBar更新
            seekBarAlarm.setProgress( alarmSetting );
            seekBarRing.setProgress( ringSetting );
            seekBarNotification.setProgress( notificationSetting );

            Log.d( CLASS_NAME, "アラーム音："+ alarmSetting + "/" + alarmMax );
            Log.d( CLASS_NAME, "呼び出し音："+ ringSetting + "/" + ringMax );
            Log.d( CLASS_NAME, "通知音：" + notificationSetting + "/" + notificationMax );
        }
        else {
            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
        }

        // ==================================================================
        //リスナー登録
        // ==================================================================
        setListener( view );

        super.onViewCreated(view, savedInstanceState);

    }

    private void displayVolumeInfo( View view ) {
        AudioManager audioManager = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );

        //STREAM_ALARM 	        アラーム音量
        //STREAM_DTMF 	        ダイヤル音量
        //STREAM_MUSIC 	        音楽再生音量
        //STREAM_NOTIFICATION 	通知音量
        //STREAM_RING 	        着信音量
        //STREAM_SYSTEM 	    システムメッセージ音量
        //STREAM_VOICE_CALL 	通話音量

        int alarmVol = 0;
        int ringVol = 0;
        int notificationVol = 0;

        if ( audioManager != null ) {
            //アラーム音量
            alarmVol = audioManager.getStreamVolume( AudioManager.STREAM_ALARM );
            //着信音量
            ringVol = audioManager.getStreamVolume( AudioManager.STREAM_RING );
            //通知音量
            notificationVol = audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION );

            Log.d( CLASS_NAME, "アラーム音＝"+alarmVol+" 呼び出し音＝"+ringVol+" 通知音＝"+notificationVol );
//音量最大値＝７
//            Log.d( CLASS_NAME, "アラーム音max＝"+audioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM )+
//                    " 呼び出し音max＝"+audioManager.getStreamMaxVolume( AudioManager.STREAM_RING )+
//                    " 通知音max＝"+audioManager.getStreamMaxVolume( AudioManager.STREAM_NOTIFICATION ) );
        }
        else {
            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
        }

        CompoundButton buttonVolume = view.findViewById( R.id.toggleVolumeOnOff );
        //１つでも音が出る設定ならONと表現する
        if ( alarmVol > 0 || ringVol > 0 || notificationVol > 0 ) {
            buttonVolume.setChecked( true );
        }
        else {
            buttonVolume.setChecked( false );
        }
    }

    private void setListener( View view ) {
        CompoundButton toggleButton = view.findViewById( R.id.toggleWiFiOnOff ); //wifi toggleButtonのviewを取得
        //set listener to toggleButton
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d( CLASS_NAME, "onClick() start.[view="+view+"]" );
                boolean isChecked = ((CompoundButton)view).isChecked(); //反転して後の状態が来てる！！
                Log.d( CLASS_NAME, "isChecked() = "+ isChecked );
                if ( mWifiManager!=null ) { //WiFiが機能してたら
                    if ( isChecked ) {
                        Log.d( CLASS_NAME, "wifi off -> on" );
                        //wifi切り替え（WiFiがONに出来た時だけEcoアプリを起動する）
                        mWifiStatus=true;
                        setWifi( mWifiStatus );
                        startEcoApplication( 0 ); //ecoアプリ起動（startActivity()で起動）
                        mStatusSetFromActivity = false;
                    }
                    else {
                        Log.d( CLASS_NAME, "wifi on -> off" );
                        startEcoApplication( 1 ); //ecoアプリ起動（startActivityForResult()で起動）
                        mWifiStatus=false;
                        mStatusSetFromActivity = true;
                    }//if (isChecked)
                }//if(mWifiManager)
            }//onClick()
        });

//トグルボタンが変更された時のリスナー（OnClick()がないと押下した時にもここにくる。）
        toggleButton.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                                                     @Override
                                                     public void onCheckedChanged( CompoundButton toggleButtonView, boolean isChecked ) {
                                                         Log.d( CLASS_NAME, "onCheckedChanged() start. [button is "+ isChecked +" ]" );
                                                         if ( toggleButtonView.getId()==R.id.toggleWiFiOnOff ) {
                                                             Log.d( CLASS_NAME, "---" );
                                                         }
                                                     }//public void onCheckedChanged()
                                                 }//OnCheckedChangeListener()
        );//setOnCheckedChangeListener


        CompoundButton volumeButton = view.findViewById( R.id.toggleVolumeOnOff ); //volume togglebuttonのView取得
        //set listener to toggleButton
        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d( CLASS_NAME, "Click volume button [view="+view+"]" );
                boolean isChecked = ((CompoundButton)view).isChecked(); //反転して後の状態が来てる！！
                Log.d( CLASS_NAME, "isChecked() = "+ isChecked );
                AudioManager audioManager = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                if ( isChecked ) {
                    Log.d( CLASS_NAME, "volume off -> on" );
                    //音　復活させる！！
//                    AudioManager audioManager = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                    if ( audioManager != null ) {
                        //着信のマナーモード解除（これは本当に着信だけ）
                        //setStreamVolume(STREAM_RING)では常に最大ボリュームになってしまう・・・ので。
                        //→しかし、マナーモードを解除したときにっ戻るボリューム値が不安定・・・なんじゃこれ？
                        audioManager.setRingerMode( AudioManager.RINGER_MODE_NORMAL );

                        int alarmMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM );
                        int ringMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
                        int notificationMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_NOTIFICATION );

                        int settingVal = 0;
                        //着信音
                        settingVal = (int)( (float)ringMax * 8/10 );
                        Log.d( CLASS_NAME, "ring = " + settingVal );
                        audioManager.setStreamVolume( AudioManager.STREAM_RING, settingVal, 0 );
                        //アラーム音（第3引数はフラグ。FLAG_xxxで指定してるみたい。UIをともなったりする。）
                        settingVal = alarmMax;
                        audioManager.setStreamVolume( AudioManager.STREAM_ALARM, settingVal, 0 );
                        //通知音（第3引数はフラグ。FLAG_xxxで指定してるみたい。UIをともなったりする。）
                        settingVal = notificationMax / 2;
                        audioManager.setStreamVolume( AudioManager.STREAM_NOTIFICATION, settingVal, 0 );

                        Log.d( CLASS_NAME, "アラーム音＝"+audioManager.getStreamVolume( AudioManager.STREAM_ALARM )+
                                "　着信音＝" +audioManager.getStreamVolume( AudioManager.STREAM_RING )+
                                "　通知音＝"+audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION ) );
                    }
                    else {
                        Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
                    }
                }
                else {
                    Log.d( CLASS_NAME, "volume on -> off" );
                    //音　消す！！
//                    AudioManager audioManager = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                    if ( audioManager != null ) {
                        //着信をバイブレーションモードにする。
                        //setStreamVolume( STREAM_RING )で音量を設定すると常に最大になってしまうので音量を消す場合もこちらに合わせる。
                        audioManager.setRingerMode( AudioManager.RINGER_MODE_VIBRATE );
                        //アラーム音（第3引数はフラグ。FLAG_xxxで指定してるみたい。UIをともなったりする。）
                        audioManager.setStreamVolume( AudioManager.STREAM_ALARM, 0, 0 );
                        //通知音（第3引数はフラグ。FLAG_xxxで指定してるみたい。UIをともなったりする。）
                        audioManager.setStreamVolume( AudioManager.STREAM_NOTIFICATION, 0, 0 );

                        Log.d( CLASS_NAME, "アラーム音＝"+audioManager.getStreamVolume( AudioManager.STREAM_ALARM )+
                                                "　着信音＝" +audioManager.getStreamVolume( AudioManager.STREAM_RING )+
                                                "　通知音＝"+audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION ) );
                    }
                    else {
                        Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
                    }
                }//if (isChecked)
                //volume seekbar を表示
                displayVolumeVal( audioManager );
            }//onClick()
        });

//トグルボタンが変更された時のリスナー（OnClick()がないと押下した時にもここにくる。）
        volumeButton.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                                                     @Override
                                                     public void onCheckedChanged( CompoundButton toggleButtonView, boolean isChecked ) {
                                                         Log.d( CLASS_NAME, "volume onCheckedChanged() start. [button is "+ isChecked +" ]" );
                                                         if ( toggleButtonView.getId()==R.id.toggleWiFiOnOff ) {
                                                             Log.d( CLASS_NAME, "---" );
                                                         }
                                                     }//public void onCheckedChanged()
                                                 }//OnCheckedChangeListener()
        );//setOnCheckedChangeListener

        //アラーム音量
        SeekBar seekBarAlarm = mView.findViewById( R.id.seekBarAlarm );
        seekBarAlarm.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d( CLASS_NAME, "onProgressChanged() : progress = " + progress );
                        AudioManager am = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                        if ( am != null ) {
                            am.setStreamVolume( AudioManager.STREAM_ALARM, progress, AudioManager.FLAG_SHOW_UI );
                        }
                        else {
                            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStartTrackingTouch() : " );
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStopTrackingTouch() : " );
                    }
                }
        );
        //着信音量
        SeekBar seekBarRing = mView.findViewById( R.id.seekBarRing );
        seekBarRing.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d( CLASS_NAME, "onProgressChanged() : progress = " + progress );
                        AudioManager am = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                        if ( am != null ) {
                            am.setStreamVolume( AudioManager.STREAM_RING, progress, AudioManager.FLAG_SHOW_UI );
                        }
                        else {
                            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStartTrackingTouch() : " );
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStopTrackingTouch() : " );
                    }
                }
        );
        //通知音量
        SeekBar seekBarNotification = mView.findViewById( R.id.seekBarNotification );
        seekBarNotification.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d( CLASS_NAME, "onProgressChanged() : progress = " + progress );
                        AudioManager am = (AudioManager)getActivity().getApplicationContext().getSystemService( Context.AUDIO_SERVICE );
                        if ( am != null ) {
                            am.setStreamVolume( AudioManager.STREAM_NOTIFICATION, progress, AudioManager.FLAG_SHOW_UI );
                        }
                        else {
                            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStartTrackingTouch() : " );
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d( CLASS_NAME, "onStopTrackingTouch() : " );
                    }
                }
        );

    }

    private void displayVolumeVal( AudioManager am ) {
        if ( am != null ) {
//            //音量の設定値を取得
//            int ringVol = am.getStreamVolume( AudioManager.STREAM_RING );
//            int alarmVol = am.getStreamVolume( AudioManager.STREAM_ALARM );
//            int notificationVol = am.getStreamVolume( AudioManager.STREAM_NOTIFICATION );
//            Log.d( CLASS_NAME, "アラーム音＝"+ alarmVol + " / 着信音＝" + ringVol + " / 通知音＝" + notificationVol );

            //seekbar 取得
            SeekBar seekBarAlarm = mView.findViewById( R.id.seekBarAlarm );
            SeekBar seekBarRing = mView.findViewById( R.id.seekBarRing );
            SeekBar seekBarNotification = mView.findViewById( R.id.seekBarNotification );

            //各最大値を取得
            int ringMax = am.getStreamMaxVolume( AudioManager.STREAM_RING );
            int alarmMax = am.getStreamMaxVolume( AudioManager.STREAM_ALARM );
            int notificationMax = am.getStreamMaxVolume( AudioManager.STREAM_NOTIFICATION );
            Log.d( CLASS_NAME, "アラーム最大音＝"+ alarmMax + " / 着信最大音＝" + ringMax + " / 通知最大音＝" + notificationMax );

            //seekbarの最大値設定
            seekBarAlarm.setMax( alarmMax );
            seekBarRing.setMax( ringMax );
            seekBarNotification.setMax( notificationMax );

            //各音量設定値設定
            int alarmSet = am.getStreamVolume( AudioManager.STREAM_ALARM );   //alarm
            int ringSet = am.getStreamVolume( AudioManager.STREAM_RING );     //着信音
            int notificationSet = am.getStreamVolume( AudioManager.STREAM_NOTIFICATION );   //通知音;
            Log.d( CLASS_NAME, "アラーム設定音量＝"+ alarmSet + " / 着信設定音量＝" + ringSet + " / 通知設定音量＝" + notificationSet );
            //seekbarの現在値表示
            seekBarAlarm.setProgress( alarmSet );
            seekBarNotification.setProgress( notificationSet );
            seekBarRing.setProgress( ringSet );

        }
        else {
            Toast.makeText( getActivity().getApplicationContext(), "AudioManagerの取得に失敗！", Toast.LENGTH_LONG ).show();
        }
    }

    private boolean startEcoApplication( int mode ) {
        //
        //暗黙的なアプリケーション起動
        // -----> actionを指定して該当する定義のあるものを(android側で)一覧してユーザーが選択する（？）
        //
//                        Intent intent = new Intent();
//                        intent.setAction( ACTION_OTHERAPP );
//                        startActivity( intent );
////この2行でも同じ ↓---------
////                        Intent intent = new Intent( ACTION_OTHERAPP );
////                        startActivity( intent );

        //
        //明示的な外部アプリケーション起動
        // -----> 該当のアプリケーションのパッケージ、クラスを指定して起動する
        //
////異常終了する・・・ notFoundException 発生。manifests.xml に指定してますか？みたいに言われる・・・。
//        Intent intent = new Intent();
//        intent.setAction( ACTION_OTHERAPP );
//        intent.setClassName( "jp.mineo.app.eco",
//                "jp.mineo.app.eco.EcoApplication" );
//        startActivity( intent );
//動いた　↓
        PackageManager packageManager = getContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage( "jp.mineo.app.eco" );
        if ( null==intent.resolveActivity( packageManager ) ) {
            Log.d( CLASS_NAME, "intent.resolveActivity() is null." );
            return false;
        }
        if ( mode == 1 ) {
            startActivityForResult( intent, PICK_CONTACT_REQUEST );
        }
        else {
            startActivity( intent );
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d( CLASS_NAME, "onActivityResult() start.[requestCode/resultCode]="+"["+requestCode+"/"+resultCode+"]" );
        if ( resultCode== Activity.RESULT_OK ) {
            Log.d( CLASS_NAME, "resultCode is RESULT_OK." );
            if ( mWifiManager.isWifiEnabled() ) { //WiFi がONの時だけecoアプリ起動
                Log.d( CLASS_NAME, "Wi-Fi is Enable." );
                startEcoApplication( 0 ); //startActivity()で起動（結果はいらない）
            }
            else {
                Log.d( CLASS_NAME, "Wi-Fi is Disable." );
                //wifi切り替え
                boolean result = mWifiManager.setWifiEnabled( false );
                if ( result ) { //when setWifiEnabled() is success.
                    CompoundButton toggleButton = mView.findViewById( R.id.toggleWiFiOnOff ); //toggleButtonのviewを取得
                    toggleButton.setChecked( false );
                }
            }
        }
        else {
            Log.d( CLASS_NAME, "result is NOT ok : "+resultCode );
        }
    }

    public void setWifi( boolean state ) {
        Log.d( CLASS_NAME, "setWifi() run. [state="+state+"]" );
        boolean result = mWifiManager.setWifiEnabled( state );
        if ( result ) { //when setWifiEnabled() is success.
            ToggleButton toggleButton = getActivity().findViewById( R.id.toggleWiFiOnOff );
            toggleButton.setChecked( state ); //change button condition
        }
    }

    public void setWiFiStatus( boolean b ) {
        mWifiStatus = b;
    }
    public boolean getWiFiStatus() {
        return mWifiStatus;
    }

    public void setStatusSetFromActivity( boolean b ) {
        mStatusSetFromActivity = b;
    }
    public boolean getStatusSetFromActivity() {
        return mStatusSetFromActivity;
    }
}
