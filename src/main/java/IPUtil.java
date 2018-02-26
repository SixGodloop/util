package com.yscredit.common.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class IPUtil {
	
	/**
	 * 客户端真实IP地址的方法一：
	 */
	public static String getRemortIP(HttpServletRequest request) {  
	    if (request.getHeader("x-forwarded-for") == null) {  
	        return request.getRemoteAddr();  
	    }  
	    return request.getHeader("x-forwarded-for");  
	}  
	/**
	 * 客户端真实IP地址的方法二：
	 */
	public static String getIpAddr(HttpServletRequest request) {  
		String ip="";
		if(request!=null){
			 ip = request.getHeader("x-forwarded-for");  
			    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			        ip = request.getHeader("Proxy-Client-IP");  
			    }  
			    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			        ip = request.getHeader("WL-Proxy-Client-IP");  
			    }  
			    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
			        ip = request.getRemoteAddr();  
			    }  
		}
	    return ip;  
	}

	/**
	 * 获取服务器ip地址，由于linux和window以及macos等系统获取ip地址的方式不同，
	 * 需要做特殊的处理。
	 * @return
	 */
	public static String getServerIpAddress() throws UnknownHostException, SocketException {
		String result = "";
		String osName = System.getProperty("os.name");
		InetAddress ip = null;
		if(osName.toLowerCase().indexOf("linux") > -1){
			boolean bFindIp = false;
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while(netInterfaces.hasMoreElements()){
				if(bFindIp){
					break;
				}
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while(ips.hasMoreElements()){
					ip = ips.nextElement();
					if(ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1){
						bFindIp = true;
						break;
					}
				}
			}
		}else {
			ip = InetAddress.getLocalHost();
		}
		if(null != ip){
			result = ip.getHostAddress();
		}
		return result;
	}
}
