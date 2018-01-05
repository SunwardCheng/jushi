package com.wifi.main;

import java.lang.Thread.State;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bulb.wifi.BulbControl;
import com.example.jushi_blub.R;
import com.wifi.udp.UDPUtils;
import com.wifi.utils.MyApplication;


public class MainActivity extends Activity implements OnClickListener{
	public static final String TAG = "MainActivity";  
    private Button send_udp,receive_udp,coapServer,coapClient;
    private long exitTime = 0;
    
    //发送或者接收的文本
    public static EditText send_msg,receive_msg;
    //目的主机IP
    private String SERVER_IP;
    private int SERVER_PORT;
    //本机监听端口
    private int LOCAL_PORT;
    
    private TextView infomation;
    
    private UDPUtils udpUtils;
    private String message;
    
    private Thread thread;
    
    private Map<String, Object> map;
    //用户输入的灯泡ID
    private int  bulbID;
    
    //用来存储全局变量，用于Activity之间的传递
    private MyApplication myApplication;
    //定时器,用来检测是否接收到成功消息
    private Timer timer;
    
    //灯泡的状态
    private String status = "off";
    
    private BulbControl bulbControl;
    
    /**
     * 初始化
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        myApplication = (MyApplication)this.getApplicationContext();
        map = myApplication.getMap();
        
        if (!map.isEmpty()) {
        	SERVER_IP = map.get("IP").toString();
        	SERVER_PORT = Integer.parseInt(map.get("Port").toString().trim());
        	LOCAL_PORT = Integer.parseInt(map.get("LOCAL_PORT").toString().trim());
		}else {
			
			SERVER_IP = "192.168.0.107";
	        SERVER_PORT = 9090;
	        LOCAL_PORT = 8929;
		}
        //udpUtils = new UDPUtils(SERVER_IP,SERVER_PORT);
      //创建配置类
		bulbControl = bulbControl.init(SERVER_IP,SERVER_PORT);
		
		
        infomation.append("目的IP： "+SERVER_IP+"\n"+"目的端口： "+SERVER_PORT+"\n");
        infomation.append("本地端口： " +LOCAL_PORT);
        
    }

    
    
    
    /**
     * 控件初始化
     */
    public void initViews(){
        send_udp = (Button) findViewById(R.id.send_udp);
//        receive_udp = (Button) findViewById(R.id.receive_udp); 
        send_msg = (EditText) findViewById(R.id.message);
//        receive_msg = (EditText) findViewById(R.id.receive);
        infomation =(TextView) findViewById(R.id.information);
//        coapServer = (Button) findViewById(R.id.coapServer);
//        coapClient = (Button) findViewById(R.id.coapClient);
        
        
//        receive_udp.setOnClickListener(MainActivity.this);
        send_udp.setOnClickListener(MainActivity.this);
//        coapClient.setOnClickListener(MainActivity.this);
//        coapServer.setOnClickListener(MainActivity.this);
        
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**  
     * 监听Back键按下事件,方法1:  
     * 注意:  
     * super.onBackPressed()会自动调用finish()方法,关闭  
     * 当前Activity.  
     */    
   /* @Override    
    public void onBackPressed() {    
        super.onBackPressed();    
    }    */
    
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO 按两次返回键退出应用程序
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// 判断间隔时间 大于2秒就退出应用
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				// 应用名
				String applicationName = getResources().getString(
						R.string.app_name);
				String msg = "再按一次返回键退出";
				//String msg1 = "再按一次返回键回到桌面";
				Toast.makeText(MainActivity.this, msg, 0).show();
				// 计算两次返回键按下的时间差
				exitTime = System.currentTimeMillis();
			} else {
				// 关闭应用程序
				finish();
				// 返回桌面操作
				// Intent home = new Intent(Intent.ACTION_MAIN);
				// home.addCategory(Intent.CATEGORY_HOME);
				// startActivity(home);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    /**
     * 菜单单击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.wifi_config) {
        	Intent intent = new Intent();
        	intent.setClass(MainActivity.this, WIFIActivity.class);
        	this.startActivity(intent);
        	myHandler.removeCallbacks(thread);
        	//关闭线程
            return true;
        }
        if (id == R.id.ip_config) {
        	Intent intent = new Intent();
        	intent.setClass(MainActivity.this, IPActivity.class);
        	this.startActivity(intent);
        	myHandler.removeCallbacks(thread);
        	//关闭线程
            //udpUtils.setKeepRunning(false); 
            return true;
		}
        return super.onOptionsItemSelected(item);
    }



    @Override
	public void onClick(View view) {

		switch (view.getId()) {  
	        case R.id.send_udp:
	        	
                String ID = send_msg.getText().toString().trim();
                if (!ID.equals("")&&ID!=null&&isInteger(ID)){
                	bulbID = Integer.parseInt(ID);
                	if(bulbID>=0&&bulbID<=32767) {
                		if (send_udp.getText().equals("打开")) {
                			for (int i = 0; i < 3; i++) {
                				new Thread(){
                					@Override
									public void run(){
                						bulbControl.openBulb(bulbID);
                					}
                				}.start();
							}
                			send_udp.setText("关闭");
						}else {
							for (int i = 0; i < 3; i++) {
								new Thread(){
                					@Override
									public void run(){
                						bulbControl.closeBulb(bulbID);
                					}
                				}.start();
							}
							send_udp.setText("打开");
						}
                		//启动监听线程
        	           // thread = new Thread(udpUtils);
                		//timer = new Timer();
                		//每隔三秒执行一次
                		//timer.schedule(new Mytask(), 0, 3000);
                	}else {
                		Toast.makeText(this, "请输入合法数字！", Toast.LENGTH_SHORT).show(); 
                	}
                	
                }else {
                	Toast.makeText(this, "请输入合法数字！", Toast.LENGTH_SHORT).show(); 
				}
                
	        	break;
	       /* case R.id.receive_udp:
	        	thread = new Thread(udpUtils);
	        	thread.start();
	        	udpUtils.setMessage(udpUtils.getMessage());
	        	break;*/
	        	
	       /* case R.id.coapServer:
	        	Intent intent = new Intent();
	        	intent.setClass(MainActivity.this, CoAPServerActivity.class);
	        	this.startActivity(intent);
	        	break;
	        case R.id.coapClient:
	        	intent = new Intent();
	        	intent.setClass(MainActivity.this, CoAPClientActivity.class);
	        	this.startActivity(intent);
	        	break;*/
	        default:  
	            break;  
        }  
	}
    
    /**
     * 判断输入的是否为数字
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {  
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        return pattern.matcher(str).matches();  
  }
	
    /**
     * 定时执行的类
     * @author 17993
     *
     */
    class Mytask extends TimerTask{

		@Override
		public void run() {
			
			//监听到的返回信息
			Message msg = new Message();
            Bundle bundle = new Bundle();
            
            //String receiveMessage = udpUtils.getMessage();
            
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
    	@Override
		public void handleMessage(Message msg)										
		{				
            
			super.handleMessage(msg);
			Bundle bundle=new Bundle();
			//从传过来的message数据中取出传过来的绑定数据的bundle对象
			bundle = msg.getData();
			
			String receive = udpUtils.getMessage();
			Toast.makeText(MainActivity.this,receive, Toast.LENGTH_SHORT).show();  
			//如果没有收到对灯泡操作的确认消息
			if (bulbResponse(receive)) {
				//如果反馈回来的灯泡打开成功
				if (status=="on"&&send_udp.getText().equals("打开")) {
					send_udp.setText("关闭");
				}else if (status=="off"&&send_udp.getText().equals("关闭")){
					send_udp.setText("打开");
				}
				udpUtils.setMessage("test");
				//udpUtils.setKeepRunning(false);
				//线程终止
				timer.cancel();
				
			}else {
				if (send_udp.getText().equals("打开")) {
					//在子线程内进行网络操作，否则Android5.1不能发数据
					new Thread(){  
						   @Override  
						   public void run()  
						   {  
							   for(int i=0;i<3;i++){
								   udpUtils.sendControInfo(bulbID,"on");
							   }
							   //udpUtils.StartListen();
						  }  
						}.start(); 
	        		Toast.makeText(MainActivity.this, "正在打开，请等一哈...", Toast.LENGTH_SHORT).show();  
				}else {
					new Thread(){  
						   @Override  
						   public void run()  
						   {  
							   for (int i = 0; i < 3; i++) {
								   udpUtils.sendControInfo(bulbID,"off");
							   }
							  //udpUtils.StartListen();
						  }  
						}.start(); 
					Toast.makeText(MainActivity.this, "正在关闭，请等一哈...", Toast.LENGTH_SHORT).show();  
				}
				State state = thread.getState();
				if (state==State.NEW) {
					thread.start();
				}
	            
			}
		}		
	};
	
	/**
	 * 判断开灯返回的数据
	 * @param receive
	 * @return
	 */
	public boolean bulbResponse(String receive){
		byte [] receivebyte = receive.getBytes();
		byte ID1 = receivebyte[1];
		byte ID2 = receivebyte[2];
		byte state = receivebyte[3];
		if (receivebyte.length>=4) {
			if (state==0x51) {
				status = "on";
			}else if (state==0x52) {
				status = "off";
			}
			Toast.makeText(MainActivity.this, state+" " + ID2 +" " +udpUtils.byteArrayToInt(ID1,ID2) 
					, Toast.LENGTH_SHORT).show();
			if (receivebyte[0]==0x5a&&bulbID==udpUtils.byteArrayToInt(ID1,ID2)){
				return true;
			}
		}
		return false;
	}
	
	@Override
    protected void onDestroy() {  
    	super.onDestroy();
    	//关闭线程
       // udpUtils.setKeepRunning(false); 
    }  
	
	/*public void start() {
		udpUtils.setKeepRunning(true);
		thread = new Thread(udpUtils);
		thread.start();
	}*/
}