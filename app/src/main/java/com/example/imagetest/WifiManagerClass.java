package com.example.imagetest;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiManagerClass {
    private WifiManager wifiManager;

    private WifiInfo wifiInfo;

    private List<ScanResult> scanResultList; 

    private List<WifiConfiguration> wifiConfigList;
    private WifiLock wifiLock;

    public WifiManagerClass(Context context) {
        this.wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        this.wifiInfo = wifiManager.getConnectionInfo();

    }
    
    public boolean getWifiStatus()
    {
         return wifiManager.isWifiEnabled();

    }

    public boolean openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            return wifiManager.setWifiEnabled(true);
        } else {
            return false;
        }

    }

    public boolean closeWifi() {
        if (!wifiManager.isWifiEnabled()) {
            return true;
        } else {
            return wifiManager.setWifiEnabled(false);
        }
    }


    public void lockWifi() {

        wifiLock.acquire();

    }


    public void unLockWifi() {
        if (!wifiLock.isHeld()) {
            wifiLock.release(); 
        }
    }

    public void createWifiLock() {
        wifiLock = wifiManager.createWifiLock("flyfly");
    }

    public List<ScanResult> getWifiList() {
        return scanResultList;
    }

    public List<WifiConfiguration> getWifiConfigList() {
        return wifiConfigList;
    }

    public StringBuilder lookUpscan() {
        StringBuilder scanBuilder = new StringBuilder();

        for (int i = 0; i < scanResultList.size(); i++) {
            scanBuilder.append("��ţ�" + (i + 1));
            scanBuilder.append(scanResultList.get(i).toString()); 
            scanBuilder.append("\n");
        }

        return scanBuilder;
    }

    public int getLevel(int NetId)
    {
        return scanResultList.get(NetId).level;
    }

    public String getMac() {
        return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getBSSID();
    }

    public String getSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getSSID();
    }


    public int getCurrentNetId() {
        return (wifiInfo == null) ? null : wifiInfo.getNetworkId();
    }


    public String getwifiInfo() {
        return (wifiInfo == null) ? null : wifiInfo.toString();
    }

    public int getIP() {
        return (wifiInfo == null) ? null : wifiInfo.getIpAddress();
    }


    public boolean addNetWordLink(WifiConfiguration config) {
        int NetId = wifiManager.addNetwork(config);
        return wifiManager.enableNetwork(NetId, true);
    }


    public boolean disableNetWordLick(int NetId) {
        wifiManager.disableNetwork(NetId);
        return wifiManager.disconnect();
    }

    public boolean removeNetworkLink(int NetId) {
        return wifiManager.removeNetwork(NetId);
    }
   
    public void hiddenSSID(int NetId)
    {
        wifiConfigList.get(NetId).hiddenSSID=true;
    }
 
    public void displaySSID(int NetId)
    {
        wifiConfigList.get(NetId).hiddenSSID=false;
    }
    
}