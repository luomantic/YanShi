package com.luomantic.yanshi.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import java.util.List;

public class WIFIStaReceiver extends BroadcastReceiver {
    private final WIFIStaListener staListener;

    public WIFIStaReceiver(WIFIStaListener staListener) {
        this.staListener = staListener;
    }

    public interface WIFIStaListener {
        void onWifiConnected();

        void onWifiDisconnected();

        void onWifiScanResultBack(String ssId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            handleNetworkStateChanged(intent);
        }
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            handleScanResults(context);
        }
    }

    private void handleScanResults(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (int i = 0; i < scanResults.size(); i++) {
              staListener.onWifiScanResultBack(scanResults.get(i).SSID);
        }
    }

    private void handleNetworkStateChanged(Intent intent) {
        Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (null != parcelableExtra) {
            NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
            NetworkInfo.State state = networkInfo.getState();
            boolean isConnected = (state == NetworkInfo.State.CONNECTED);
            if (isConnected) {
                staListener.onWifiConnected();
            }else {
                staListener.onWifiDisconnected();
            }
        }
    }

}
