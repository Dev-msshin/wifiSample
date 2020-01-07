package example.pnc.msshin.wifisample;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;


public class WifiAPItem {
    private static String TAG = "WiFiAPItem";
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;
    public static final int STATE_NONE = 0;
    public static final int STATE_SAVED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_AUTHENTICATING = 3;
    public static final int STATE_CONNECTED = 4;
    private enum PskType {UNKNOWN, WPA, WPA2, WPA_WPA2}
    private String bssid;
    private String ssid;
    private String capabilities;
    private int frequency;
    private int level;
    private int security;
    private WifiConfiguration wc;
    private ScanResult scanResult;
    private int state;
    private PskType pskType;

    public WifiAPItem(){
        state = STATE_NONE;
        pskType = PskType.UNKNOWN;
    }

    public WifiAPItem(String bssid, String ssid, String capabilities, int frequency, int level, WifiConfiguration wc, ScanResult scanResult, int state){
        this.state = STATE_NONE;
        this.pskType = PskType.UNKNOWN;

        setBssid(bssid);
        setSsid(ssid);
        setCapabilities(capabilities);
        setFrequency(frequency);
        setLevel(level, true, 5);
        setSecurity(scanResult);
        setWc(wc);
        setScanResult(scanResult);
        setState(state);
        setPskType(scanResult);
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level, boolean isConvert, int convertMaxLevel) {
        if (isConvert){
            WifiManager.calculateSignalLevel(level, convertMaxLevel);
        }else {
            this.level = level;
        }
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(ScanResult scanResult) {
        if (scanResult != null) {
            security = getSecurity(scanResult);
        }
    }

    public WifiConfiguration getWc() {
        return wc;
    }

    public void setWc(WifiConfiguration wc) {
        this.wc = wc;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    private int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public PskType getPskType() {
        return pskType;
    }

    public void setPskType(ScanResult scanResult) {
        if (security == SECURITY_PSK) {
            this.pskType = getPskType(scanResult);
        }
    }

    private PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w(TAG, "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bssid: ").append(bssid);
        sb.append(", ssid: ").append(ssid);
        sb.append(", capabilities: ").append(capabilities);
        sb.append(", frequency: ").append(frequency);
        sb.append(", level: ").append(level);
        sb.append(", security: ").append(security);
        sb.append(", state: ").append(state);
        sb.append(", pskType: ").append(pskType);
        return sb.toString();
    }
}