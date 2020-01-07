package example.pnc.msshin.wifisample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private WifiManager mWifiManager;
    private ArrayList<WifiAPItem> mWifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerWifi();
        if (mWifiManager != null && mWifiManager.isWifiEnabled()){
            mWifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterWifi();
    }

    private void registerWifi() {
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter1.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(wifiReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiConnectionStateReceiver, filter2);
    }

    private void unregisterWifi() {
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(wifiConnectionStateReceiver);
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null){
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                    Log.d(TAG, "wifiReceiver onReceive - SCAN_RESULTS_AVAILABLE_ACTION");
                }else if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
                    Log.d(TAG, "wifiReceiver onReceive - RSSI_CHANGED_ACTION");
                }

                mWifiList = getMakeWifiList();
                if (mWifiList != null && !mWifiList.isEmpty()){
                     Log.d(TAG, "Detected wifi list length: "+mWifiList.size());
                }
            }
        }
    };

    private BroadcastReceiver wifiConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    switch (state) {
                        case ASSOCIATED: Log.i(TAG, "[WiFiState] ASSOCIATED"); break;
                        case ASSOCIATING: Log.i(TAG, "[WiFiState] ASSOCIATING"); break;
                        case AUTHENTICATING: Log.i(TAG, "[WiFiState] Authenticating..."); break;
                        case COMPLETED: Log.i(TAG, "[WiFiState] Connected"); break;
                        case DISCONNECTED: Log.i(TAG, "[WiFiState] Disconnected"); break;
                        case DORMANT: Log.i(TAG, "[WiFiState] DORMANT"); break;
                        case FOUR_WAY_HANDSHAKE: Log.i(TAG, "[WiFiState] FOUR_WAY_HANDSHAKE"); break;
                        case GROUP_HANDSHAKE: Log.i(TAG, "[WiFiState] GROUP_HANDSHAKE"); break;
                        case INACTIVE: Log.i(TAG, "[WiFiState] INACTIVE"); break;
                        case INTERFACE_DISABLED: Log.i(TAG, "[WiFiState] INTERFACE_DISABLED"); break;
                        case INVALID: Log.i(TAG, "[WiFiState] INVALID"); break;
                        case SCANNING: Log.i(TAG, "[WiFiState] SCANNING"); break;
                        case UNINITIALIZED: Log.i(TAG, "[WiFiState] UNINITIALIZED"); break;
                        default: Log.i(TAG, "[WiFiState] Unknown"); break;
                    }
                } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if (netInfo != null) Log.d(TAG, "[NetState] netInfo : "+netInfo.toString());
                    if (wifiInfo != null) Log.d(TAG, "[NetState] wifiInfo : "+wifiInfo.toString());
                }
            }
        }
    };

    public static String convertToQuotedString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        final int lastPos = string.length() - 1;
        if(lastPos > 0 && (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    private WifiConfiguration getWifiConfiguration(String bssid, String ssid) {
        if (mWifiManager != null) {
            List<WifiConfiguration> wcList = mWifiManager.getConfiguredNetworks();
            if (wcList != null) {
                for (WifiConfiguration wc : wcList){
                    if (wc == null) return null;
                    if (wc.BSSID == null && wc.SSID.equals(convertToQuotedString(ssid))) {
                        Log.d(TAG, "getWifiConfiguration() Find!! : SSID=" + wc.SSID);
                        return wc;
                    } else if (wc.BSSID != null && wc.BSSID.equals(bssid) && wc.SSID.equals(convertToQuotedString(ssid))) {
                        Log.d(TAG, "getWifiConfiguration() Find!! : BSSID=" + wc.BSSID + ", SSID=" + wc.SSID);
                        return wc;
                    } else if (wc.BSSID != null && wc.BSSID.equals("any") && wc.SSID.equals(convertToQuotedString(ssid))) {
                        Log.d(TAG, "getWifiConfiguration() Find!! : BSSID=" + wc.BSSID + ", SSID=" + wc.SSID);
                        return wc;
                    }
                }
            }
        }

        return null;
    }

    private ArrayList<WifiAPItem> getMakeWifiList() {
        ArrayList<WifiAPItem> wifiArr = new ArrayList<>();
        boolean isEnabled = false;
        if (mWifiManager != null){
            isEnabled = mWifiManager.isWifiEnabled();
        }
        Log.d(TAG, "makeWifiList() - enabled : "+isEnabled);
        if (isEnabled) {
            String[] connectedInfo = getConnectedInfo();

            for (ScanResult result : mWifiManager.getScanResults()) {
                WifiAPItem adapterItem = new WifiAPItem(
                        result.BSSID,
                        result.SSID,
                        result.capabilities,
                        result.frequency,
                        result.level,
                        getWifiConfiguration(result.BSSID, result.SSID),
                        result,
                        WifiAPItem.STATE_NONE
                );

                if (connectedInfo != null && connectedInfo.length > 1 && connectedInfo[1] != null && connectedInfo[1].equals(convertToQuotedString(adapterItem.getSsid()))) {
                    Log.d(TAG, "Find connected AP ! : insert first item");
                    adapterItem.setState(WifiAPItem.STATE_CONNECTED);
                } else if (adapterItem.getWc() != null) {
                    adapterItem.setState(WifiAPItem.STATE_SAVED);
                } else {
                    adapterItem.setState(WifiAPItem.STATE_NONE);
                }
                Log.d(TAG, "AP - " + adapterItem.toString());
                wifiArr.add(adapterItem);
            }
        }
        return wifiArr;
    }

    private String[] getConnectedInfo() {
        if( mWifiManager == null ) return null;

        String[] info = new String[2];
        WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        if(connectionInfo != null) {
            info[0] = connectionInfo.getBSSID();
            info[1] = connectionInfo.getSSID();
            Log.d(TAG, "getConnectedInfo() : BSSID=" + connectionInfo.getBSSID() + ", SSID=" + connectionInfo.getSSID());
        }

        return info;
    }
}
