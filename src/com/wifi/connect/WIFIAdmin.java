package com.wifi.connect;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.text.format.Formatter;
import android.widget.Toast;

public class WIFIAdmin {
	
	// 定义WifiManager对象     
    private WifiManager mWifiManager;    
    // 定义WifiInfo对象     
    private WifiInfo mWifiInfo;    
    // 扫描出的网络连接列表     
    private List<ScanResult> mWifiList;    
    // 网络连接列表     
    private List<WifiConfiguration> mWifiConfiguration;  
    
    
    // 定义一个WifiLock,（默认时手机锁屏时2分钟后wifi会关闭）  
    WifiLock mWifiLock;  
    
    //定义一个组播锁
    private MulticastLock wifiLock;
    
    /**
     * 构造器，获取wifi信息和管理对象
     * @param context
     */
    public WIFIAdmin(Context context){
    	//获取WifiManager对象
    	mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	//获取WifiInfo对象
    	mWifiInfo = mWifiManager.getConnectionInfo();
    }
    
    /**
     * 打开Wifi
     * @param context
     */
    public void openWifi(Context context) {
    	//wifi是否打开
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
			/*
			WIFI_STATE_DISABLING:Wi-Fi网卡正在关闭，用整型常量0表示。
            WIFI_STATE_DISABLED：Wi-Fi网卡不可用，用整型常量1表示。
			WIFI_STATE_ENABLING:Wi-Fi网卡正在打开，启动需要一段时间，用整型常量2表示。
			WIFI_STATE_ENABLED：Wi-Fi网卡可用，用整型常量3表示。
			WIFI_STATE_UNKNOWN:未知网卡状态，用整型常量4表示。
			 */
		}else if (mWifiManager.getWifiState() == 2) {
			   Toast.makeText(context,"亲，Wifi正在打开，请稍等...", Toast.LENGTH_SHORT).show();  
        }else if (mWifiManager.getWifiState() == 0) {  
            	Toast.makeText(context,"亲，Wifi已经打开，请稍等...", Toast.LENGTH_SHORT).show(); 
        }
	}
    
    /**
     * 关闭WIFI     
     * @param context
     */
    public void closeWifi(Context context) {    
        if (mWifiManager.isWifiEnabled()) {    
            mWifiManager.setWifiEnabled(false);    
        }else if(mWifiManager.getWifiState() == 1){  
            Toast.makeText(context,"亲，Wifi已经关闭，不用再关了", Toast.LENGTH_SHORT).show();  
        }else if (mWifiManager.getWifiState() == 0) {  
            Toast.makeText(context,"亲，Wifi正在关闭，不用再关了", Toast.LENGTH_SHORT).show();  
        }else{  
            Toast.makeText(context,"请重新关闭", Toast.LENGTH_SHORT).show();  
        }  
    }   
    
    public void checkState(Context context) {
    	 if (mWifiManager.getWifiState() == 0) {  
             Toast.makeText(context,"Wifi正在关闭", Toast.LENGTH_SHORT).show();  
         } else if (mWifiManager.getWifiState() == 1) {  
             Toast.makeText(context,"Wifi已经关闭", Toast.LENGTH_SHORT).show();  
         } else if (mWifiManager.getWifiState() == 2) {  
             Toast.makeText(context,"Wifi正在开启", Toast.LENGTH_SHORT).show();  
         } else if (mWifiManager.getWifiState() == 3) {  
             Toast.makeText(context,"Wifi已经开启", Toast.LENGTH_SHORT).show();  
         } else {  
             Toast.makeText(context,"没有获取到WiFi状态", Toast.LENGTH_SHORT).show();  
         }    
	}
    
    /**
     * 锁定WifiLock  
     */
    public void acquireWifiLock() {    
        mWifiLock.acquire();    
    }    
    
    /** 
     * 解锁WifiLock     
     * 
     */
    public void releaseWifiLock() {    
        // 判断时候锁定     
        if (mWifiLock.isHeld()) {    
            mWifiLock.release();    
        }    
    }    
	 /**
	  *  创建一个WifiLock     
	  */
    public void createWifiLock() {    
        mWifiLock = mWifiManager.createWifiLock("MyLock");    
    }    
    
    /**
     * 打开组播
     */
    public void acquireMulticastLock() {
		wifiLock.acquire();
	}
    /** 
     * 解锁MulticastLock    
     * 
     */
    public void releaseMulticastLock() {
		if (wifiLock.isHeld()) {
			wifiLock.release();
		}
	}
    /**
     * 创建一个组播MuticastLock
     */
    public void createMuticastLock(){
    	wifiLock = mWifiManager.createMulticastLock(getClass().getSimpleName());
    	wifiLock.setReferenceCounted(true);
    }
    /**
     * 得到配置好的网络
     * @return
     */
    public List<WifiConfiguration> getConfiguration(){
    	return mWifiConfiguration;
    }
    /**
     * 指定配置好的网络进行连接
     * @param index
     */
    public void connectConfiguration(int index){
    	// 索引大于配置好的网络索引返回     
        if (index > mWifiConfiguration.size()) {    
            return;    
        }    
        // 连接配置好的指定ID的网络     
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,    
                true);    
    }
    
    /**
     * 扫描Wifi
     * 1.有的ssid为""，也就是ssid !＝ null,获取不到ssid
     * 		设置中并没有多余wifi，但这个热点点其它信息可以获取到，说明这个热点是存在的，
     *      是该热点隐藏了，所以获取不到。这也就是手机设置中为什么会有添加网路的按钮了
     * 2  wifi列表中有许多同名的wifi热点，也就是扫描的结果中有重合部分，并不是有多个同名的wifi热点
     *  	当附近wifi热点比较少时不会出现此问题，当附近wifi网络比较多时会出现此问题。这就需要将同
     *  	名的热点进行删除，但是如果真有两个ssid名相同的wifi，那就可以通过capabilities去区分吧，
     *  	如果capabilities也相同就没办法了，系统设置里面也不显示同名的
     * @param context
     */
    public void startScan(Context context){
    	//开始扫描
    	mWifiManager.startScan();
    	//得到扫描结果
    	List<ScanResult> results = mWifiManager.getScanResults();
    	//得到配置好的网络连接
    	mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    	
    	if (results==null) {
			switch (mWifiManager.getWifiState()) {
			case 2:
				Toast.makeText(context,"WiFi正在开启，请稍后重新点击扫描", Toast.LENGTH_SHORT).show();  
				break;
			case 3:
				Toast.makeText(context,"当前区域没有无线网络", Toast.LENGTH_SHORT).show();  
				break;
			default:
				Toast.makeText(context,"WiFi没有开启，无法扫描", Toast.LENGTH_SHORT).show();
				break;
			}
		}else {
			mWifiList = new ArrayList<ScanResult>();
			for(ScanResult result:results){
				if(result.SSID==null||result.SSID.length()==0||result.capabilities.contains("[IBSS]")){
					continue;
				}
				boolean found = false;  
			    for(ScanResult item:mWifiList){   
				    if(item.SSID.equals(result.SSID)&&item.capabilities.equals(result.capabilities)){  
					    found = true;
					    break;   
			       }  
			    }   
			    if(!found){  
				    mWifiList.add(result);  
			    }   
			}
		}
    }
    
	/**
	 * 得到网络列表     
	 * @return
	 */
    public List<ScanResult> getWifiList() {    
        return mWifiList;    
    }    
    
    /**
     * 查看扫描结果
     * @return
     */
    public StringBuilder lookupScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder.append("Index_" + new Integer(i + 1).toString()+":");
			 // 将ScanResult信息转换成一个字符串包     
            // 其中把包括：BSSID、SSID、capabilities、frequency、level     
            stringBuilder.append((mWifiList.get(i)).toString());    
            stringBuilder.append("/n");    
		}
		return stringBuilder;
	}

    /**
     * 得到Mac地址
     * @return
     */
    public String getMacAddress() {
		return (mWifiInfo==null)?"NULL":mWifiInfo.getMacAddress();
	}
    
    /**
     *  得到接入点的BSSID     
     * @return
     */
    public String getBSSID() {    
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();    
    }    
    
    /**
     * 得到IP地址     
     * @return
     */
    public int getIPAddress() {    
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();    
    }    
    
    /**
     * 得到连接的ID     
     * @return
     */
    public int getNetworkId() {    
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();    
    }    
    
    /**
     * 得到WifiInfo的所有信息包     
     * @return
     */
    public String getWifiInfo() {    
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();    
    }  
    
    /**
     * 添加一个网络并连接
     * @param wfc
     */
    public void  addNetwork(WifiConfiguration wfc) {
    	int wfcId = mWifiManager.addNetwork(wfc);
    	Boolean b = mWifiManager.enableNetwork(wfcId, true);
    	System.out.println("a--" + wfcId);   
        System.out.println("b--" + b);  
	}
    
    /**
     * 断开指定ID网络
     * @param netId
     */
    public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
    
    /**
     * 获取网络SSID
     * @return
     */
    public String getSSID() {
		return mWifiInfo.getSSID();
	}
    
    /**
     * 判断是否存在SSID
     * @param SSID
     * @return
     */
    private WifiConfiguration IsExsits(String SSID)     
    {     
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();     
           for (WifiConfiguration existingConfig : existingConfigs)      
           {     
             if (existingConfig.SSID.equals("\""+SSID+"\""))     
             {     
                 return existingConfig;     
             }     
           }     
        return null;      
    }   
    
    /**
     * 连接新wifi
     * @param SSID
     * @param passward
     * @param type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID,String password,int type){
    	
    	WifiConfiguration config = new WifiConfiguration();
    	//清除之前的内容
        config.allowedAuthAlgorithms.clear();    
        config.allowedGroupCiphers.clear();    
        config.allowedKeyManagement.clear();    
        config.allowedPairwiseCiphers.clear();    
        config.allowedProtocols.clear();    
        config.SSID = "\"" + SSID + "\"";   
        
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        
        if (tempConfig!=null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}
        
        if(type == 1) //WIFICIPHER_NOPASS   
        {    
             config.wepKeys[0] = "";    
             config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);    
             config.wepTxKeyIndex = 0;    
        }    
        if(type == 2) //WIFICIPHER_WEP   
        {    
            config.hiddenSSID = true;   
            config.wepKeys[0]= "\""+password+"\"";    
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);    
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);    
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);    
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);    
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);    
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);    
            config.wepTxKeyIndex = 0;    
        }    
        if(type == 3) //WIFICIPHER_WPA   
        {    
	        config.preSharedKey = "\""+password+"\"";    
	        config.hiddenSSID = true;      
	        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);      
	        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                            
	        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                            
	        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                       
	        //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);     
	        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);   
	        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);   
	        config.status = WifiConfiguration.Status.ENABLED;      
        }   
         return config; 
    }
    
    /**
     * 获取wifi的IP地址
     * @return
     */
    public String getWifiIP(){
    	DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
    	String routeIP = Formatter.formatIpAddress(dhcpInfo.gateway);
    	return routeIP;
    	
    }
}
