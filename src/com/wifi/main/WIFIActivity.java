package com.wifi.main;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jushi_blub.R;
import com.java.bulb.ConfigWifi;
import com.java.bulb.impl.wifi.WIFIAdmin;
import com.wifi.utils.WifiUtils;


public class WIFIActivity extends Activity{
	public static final String TAG = "WIFIActivity";  
	//展示扫描的wifi信号
    private ListView mlistView;  
    protected WIFIAdmin mWifiAdmin;  
    private List<ScanResult> mWifiList;  
    public int level;  
    protected String ssid; 
    private TextView wifi_info,wifi_SSID,wifi_password;
    private RadioButton radio1,radio2;
    private String SSID,Password;
    private String SERVER_IP;
    private int SERVER_PORT;
    private ConfigWifi configWifi ;
    
    //定时器
    private Timer timer ;
    /**
     * 接收的消息
     */
    private String message;
    
    private Thread thread;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wificonfig);
        
        mWifiAdmin = new WIFIAdmin(WIFIActivity.this);
        initViews();
        getSSID();
        
        SERVER_IP = mWifiAdmin.getWifiIP();
        SERVER_PORT = 9090;
        
        if (SERVER_IP!=""&&SERVER_IP!=null) {
        	 //创建配置类
            configWifi = ConfigWifi.initWifi(SERVER_IP);
		}
        IntentFilter filter = new IntentFilter(
        		WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
        //监听wifi变化
        registerReceiver(mReceiver, filter);
        
        mlistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {

				AlertDialog.Builder alert = new AlertDialog.Builder(WIFIActivity.this);
				ssid=mWifiList.get(position).SSID; 
				//SSID是初始时连接的名称，ssid是即将要连接的名称
				SSID = ssid;
                alert.setTitle(ssid);  
                alert.setMessage("输入密码");
                
                final EditText et_password = new EditText(WIFIActivity.this);
                final SharedPreferences preferences = getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
                et_password.setText(preferences.getString(ssid, ""));  
                alert.setView(et_password);  
                
                alert.setPositiveButton("配置到站点", new DialogInterface.OnClickListener(){  
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
                    	Password = et_password.getText().toString();  
                        if(null == Password  || Password.length() < 8){  
                                    Toast.makeText(WIFIActivity.this, "密码至少8位", Toast.LENGTH_SHORT).show();  
                                return;      
                            }  
                        
                        Editor editor=preferences.edit();  
                        editor.putString(ssid, Password);   //保存密码  
                        editor.commit();          
                        getSSID();
                        
                        if (SERVER_IP==null||SERVER_IP=="") {
                        	Toast.makeText(WIFIActivity.this, "请连上设备！", Toast.LENGTH_SHORT).show();
						}else {
							//创建配置类
							configWifi = ConfigWifi.initWifi(SERVER_IP);
						}
                        
//                        new Thread(){  
//     					   @Override  
//     					   public void run()  
//     					   {  
//     						   configWifi.startListen(controlHandler);
//     					  }  
//     					}.start();
                        
                    	//如果未收到成功回复每隔1秒发送一次SSID和密码
                        timer = new Timer();
                    	timer.schedule(new Mytask(), 0, 100);
                    	
                    }  
                });  
                alert.setNegativeButton("取消", new DialogInterface.OnClickListener(){  
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
                        //  
                        //mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());  
                    }  
                });  
                alert.create();  
                alert.show();  
                
			}
		});
        
    }
    
    /**
     * 获取监听消息的Handler
     */
    Handler controlHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = new Bundle();
			bundle = msg.getData();
			message = bundle.getString("config_receive"); 
			
		}
    };
    
    /**
     * 时钟任务
     * @author 17993
     *
     */
    class Mytask extends TimerTask{

		@Override
		public void run() {
			Message msg = new Message();
            Bundle bundle = new Bundle();
            
            //把数据放到buddle中
            bundle.putString("receive", "test");
            //把buddle传递到message
            msg.setData(bundle);
            myHandler.sendMessage(msg);
          
		}
		
	}
    /**
     * 使用Handler传递数据，避免内部线程不能创建handler
     */
    Handler myHandler = new Handler(){
    	int i = 0;
    	@Override
		public void handleMessage(Message msg)										
		{											
			super.handleMessage(msg);
			Bundle bundle = new Bundle();
			bundle = msg.getData();
			
			
			if (message!=null&&message.equals("success")){
				Toast.makeText(WIFIActivity.this, "配置成功！", Toast.LENGTH_SHORT).show(); 
				
				Toast.makeText(WIFIActivity.this, SERVER_IP, Toast.LENGTH_SHORT).show(); 
				//连接到配置的WIFI上
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssid, Password, 3));
				
				//取消定时
				timer.cancel();
//				Intent intent = new Intent();
//				intent.setClass(WIFIActivity.this, IPActivity.class);
//	        	startActivity(intent);
//	        	finish();
	        	
			}else {
				i++;
				if (i>3) {
					message="success";
					i=0;
				}
				
				//Toast.makeText(WIFIActivity.this, "正在配置，请稍等...", Toast.LENGTH_SHORT).show();
				//给站点配置wifi
				
				new Thread(){  
					   @Override  
					   public void run()  
					   {  
						   configWifi.config(ssid, Password);
					  }  
					}.start();
			}
		}		
	};

	
    /**
     * 获取SSID
     */
    public void getSSID() {
    	SSID =  mWifiAdmin.getSSID();
        if (SSID!=""||SSID!=null) {
			wifi_SSID.setText(SSID.replace("\"", ""));
			wifi_password.setText("********");
		}else {
			wifi_SSID.setText("");
			wifi_password.setText("");
		}
	}
    
    /**
     * 控件初始化
     */
    public void initViews(){
        mlistView=(ListView) findViewById(R.id.wifi_list); 
        wifi_info = (TextView) findViewById(R.id.wifi_info);
        wifi_SSID = (TextView) findViewById(R.id.wifi_SSID);
        wifi_password = (TextView) findViewById(R.id.wifi_password);
        radio1 = (RadioButton) findViewById(R.id.radio1);
        radio2 = (RadioButton) findViewById(R.id.radio2);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	int id = item.getItemId();
    	
    	switch (id) {
	    	case R.id.check_wifi:  
	            mWifiAdmin.checkState(WIFIActivity.this);  
	            break;  
	        case R.id.open_wifi:  
	            mWifiAdmin.openWifi(WIFIActivity.this);  
	            break;  
	        case R.id.close_wifi:  
	            mWifiAdmin.closeWifi(WIFIActivity.this);  
	            break;  
	        case R.id.scan_wifi:  
	            mWifiAdmin.startScan(WIFIActivity.this);  
	            mWifiList=mWifiAdmin.getWifiList();  
	            if(mWifiList!=null){  
	                mlistView.setAdapter(new MyAdapter(this,mWifiList));  
	                new WifiUtils.Utility().setListViewHeightBasedOnChildren(mlistView);  
	            }  
	            break;  
	        default:  
	            break;  
    	}
        return super.onOptionsItemSelected(item);
    }
    
    /**  
     * 监听Back键按下事件,方法1:  
     * 注意:  
     * super.onBackPressed()会自动调用finish()方法,关闭  
     * 当前Activity.  
     */    
    @Override    
    public void onBackPressed() {    
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    } 

	
	public class MyAdapter extends BaseAdapter{
		LayoutInflater inflater;
		List<ScanResult> list;
		
		public MyAdapter(Context context, List<ScanResult> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();  
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);  
		}

		@Override
		public long getItemId(int position) {
			return position;  
		}

		//忽略指定的警告
		@SuppressLint({ "ViewHolder", "InflateParams" })  
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//针对每一个数据（即每一个List ID）创建一个ListView实例，
			View view = null;
			view = inflater.inflate(R.layout.wifi_listitem, null);
			ScanResult result = list.get(position);
			TextView wifi_ssid=(TextView) view.findViewById(R.id.ssid);  
			ImageView wifi_level=(ImageView) view.findViewById(R.id.wifi_level); 
			wifi_ssid.setText(result.SSID);  
			Log.i(TAG, "scanResult.SSID="+result);
			
			level=WifiManager.calculateSignalLevel(result.level,5);  
			
			//两种wifi图像 
			if(result.capabilities.contains("WEP")||result.capabilities.contains("PSK")||  
					result.capabilities.contains("EAP")){  
                wifi_level.setImageResource(R.drawable.wifilock);  
            }else{  
                wifi_level.setImageResource(R.drawable.wifi);  
            }  
            wifi_level.setImageLevel(level);  
            //判断信号强度，显示对应的指示图标    
             return view;  
		}
		
	}
	
	/**
	 * 设置listview的高度
	 * *//*  
    public class Utility {   
        public void setListViewHeightBasedOnChildren(ListView listView) {
        	
            ListAdapter listAdapter = listView.getAdapter();    
            if (listAdapter == null) {   
                return;   
            }   
            int totalHeight = 0;   
            for (int i = 0; i < listAdapter.getCount(); i++) {   
                View listItem = listAdapter.getView(i, null, listView);   
                listItem.measure(0, 0);   
                totalHeight += listItem.getMeasuredHeight();   
            }   
            ViewGroup.LayoutParams params = listView.getLayoutParams();   
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));   
            listView.setLayoutParams(params);   
        }   
    }*/
    
    /**
     * 监听wifi状态  
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver (){    
        @Override    
        public void onReceive(Context context, Intent intent) {     
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
  
            NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
            if(wifiInfo.isConnected()){  
                WifiManager wifiManager = (WifiManager) context  
                        .getSystemService(Context.WIFI_SERVICE);  
                String wifiSSID = wifiManager.getConnectionInfo()  
                        .getSSID();  
                Toast.makeText(context, wifiSSID+"连接成功", 1).show();  
            }                  
        }     
        
    };   
    
    @Override
    protected void onDestroy() {  
    	super.onDestroy();
    	configWifi.stopListen();
    } 

}
