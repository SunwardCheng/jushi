package com.wifi.mdns;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;




public class DiscoverServices {

	public static class UDPListener implements ServiceListener ,ServiceTypeListener{

		public UDPListener() { 

		}
		/**
		 * 搜索局域网设备的type，该方法和ServiceAdded很像，但是这个方法不到serviceInfo，event的serviceInfo是为null
		 */
		@Override
		public void serviceTypeAdded(ServiceEvent event) {
			System.out.println(" serviceTypeAdded: " + event.getInfo());  
		}

		public void subTypeForServiceTypeAdded(ServiceEvent event) {
			System.out.println(" subTypeForServiceTypeAdded: " + event.getInfo()); 
		}

		/**
		 *  serviceAdded 方法会自动返回搜索到服务的相关信息，
		 *  比如一般都会带有ip地址、端口、serviceInfo信息，这些
		 *  信息一般都是搜索到的注册的设备的信息，用这个方法就可
		 *  以拿到设备的所有信息  
		 *  有的时候这个信息ServiceInfo的值是null，这个几率不是
		 *  很大，但是和手机的硬件设备有点关系
		 */
		@Override
		public void serviceAdded(ServiceEvent event) {
			System.out.println("Service added   : " + event.getName() + "." + event.getType());			
		}

		/**
		 * System.out.println(" serviceTypeAdded: " + event.getInfo());  
		 */
		@Override
		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "." + event.getType());  
		}

		/**
		 * 什么时候监听接口回调该方法  
		 */
		@Override
		public void serviceResolved(ServiceEvent event) {
			 System.out.println("Service resolved: " + event.getInfo());  			
		}  
		
	}
	
	/*new ServiceListener(){

		@Override
		public void serviceAdded(ServiceEvent event) {
			jmDNS.requestServiceInfo(event.getType(), event.getName(), 1);
			
		}

		@Override
		public void serviceRemoved(ServiceEvent ev) {
			System.out.println("Service removed: " + ev.getName());
			
		}

		@Override
		public void serviceResolved(ServiceEvent ev) {
			String addr = "";
	        if (ev.getInfo().getInetAddresses() != null && ev.getInfo().getInetAddresses().length > 0) {
	            addr = ev.getInfo().getInetAddresses()[0].getHostAddress();
	        }
	        System.out.println("Service resolved: " + ev.getInfo().getName() +" ==> "+ addr + ":" + ev.getInfo().getPort() );
			
		}
    	
    }*/
}