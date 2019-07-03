package com.luomantic.yanshi.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class WIFIConManager {

        private static WIFIConManager sInstance = null;
        private WifiManager mWifiManager;
        private int networkId;

        private WIFIConManager(Context context) {
            mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        public static WIFIConManager getInstance(Context context) {
            if (sInstance == null) {
                synchronized (WIFIConManager.class) {
                    if (sInstance == null) {
                        sInstance = new WIFIConManager(context);
                    }
                }
            }
            return sInstance;
        }

        /**
         * 尝试连接指定wifi
         *
         * @param ssId     wifi名
         * @param password 密码
         * @return 是否连接成功
         */
        public boolean connect(@NonNull String ssId, @NonNull String password) {
            boolean isConnected = isConnected(ssId);//当前已连接至指定wifi
            if (isConnected) {
                return true;
            }
            networkId = mWifiManager.addNetwork(newWifiConfig(ssId, password, true));
            return mWifiManager.enableNetwork(networkId, true);
        }

        /**
         * 根据wifi名与密码配置 WiFiConfiguration, 每次尝试都会先断开已有连接
         *
         * @param isClient 当前设备是作为客户端,还是作为服务端, 影响SSID和PWD
         */
        @NonNull
        private WifiConfiguration newWifiConfig(String ssId, String password, boolean isClient) {
            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            if (isClient) {//作为客户端, 连接服务端wifi热点时要加双引号
                config.SSID = "\"" + ssId + "\"";
                config.preSharedKey = "\"" + password + "\"";
            } else {//作为服务端, 开放wifi热点时不需要加双引号
                config.SSID = ssId;
                config.preSharedKey = password;
            }
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
            return config;
        }

        /**
         * @return 热点是否已开启
         */
        public boolean isWifiEnabled() {
            try {
                @SuppressLint("PrivateApi")
                Method methodIsWifiApEnabled = WifiManager.class.getDeclaredMethod("isWifiApEnabled");
                return (boolean) methodIsWifiApEnabled.invoke(mWifiManager);
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 是否已连接指定wifi [测试无效]
         */
        private boolean isConnected(String ssId) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                return false;
            }
            switch (wifiInfo.getSupplicantState()) {
                case AUTHENTICATING:
                case ASSOCIATING:
                case ASSOCIATED:
                case FOUR_WAY_HANDSHAKE:
                case GROUP_HANDSHAKE:
                case COMPLETED:
                    return wifiInfo.getSSID().replace("\"", "").equals(ssId);
                default:
                    return false;
            }
        }

        /**
         * 打开WiFi
         * @return 操作成功返回true, 否则返回false.
         */
        public boolean openWifi() {
            boolean opened = true;
            if (!mWifiManager.isWifiEnabled()) {
                opened = mWifiManager.setWifiEnabled(true);
            }
            return opened;
        }

        /**
         * 关闭wifi
         * @return 操作成功返回true, 否则返回false.
         */
        public boolean closeWifi() {
            boolean closed = true;
            if (mWifiManager.isWifiEnabled()) {
                closed = mWifiManager.setWifiEnabled(false);
            }
            return closed;
        }

        /**
         * 断开连接
         * @return 操作成功返回true, 否则返回false.
         */
        public WIFIConManager disconnect() {
            if (networkId != 0) {
                mWifiManager.disableNetwork(networkId);
            }
            mWifiManager.disconnect();
            return this;
        }

        /**
         * 是否连接过指定Wifi
         */
        @Nullable
        public WifiConfiguration everConnected(String ssId) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            if (existingConfigs == null || existingConfigs.isEmpty()) {
                return null;
            }
            ssId = "\"" + ssId + "\"";
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssId)) {
                    return existingConfig;
                }
            }
            return null;
        }

        /**
         * 获取本机的ip地址
         */
        @Nullable
        public String getLocalIp() {
            return convertIp(mWifiManager.getConnectionInfo().getIpAddress());
        }

        private String convertIp(int ipAddress) {
            if (ipAddress == 0) return null;
            return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                    + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
        }

        public WifiManager getWifiManager() {
            return mWifiManager;
        }

        public void removeWifi(String wifiName) {
            List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration config :
                    networks) {
                String ssId = config.SSID;
                if (ssId.equals(wifiName)) {
                    mWifiManager.removeNetwork(config.networkId);
                    mWifiManager.saveConfiguration();
                }
            }
        }
}
