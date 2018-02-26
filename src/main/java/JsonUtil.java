package com.yscredit.pgp.gateway.util;

import net.sf.json.JSONObject;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class JsonUtil {

	private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	private static final ObjectMapper om = new ObjectMapper();
	static {
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);//解决json属性比对象多报错请情况
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String args) {
		Map<String, Object> argsMap = null;
		try {
	    	argsMap = om.readValue(args, Map.class);
		} catch(Exception ex) {
			argsMap = new HashMap<String, Object>();
			ex.printStackTrace();
		}
		
		return argsMap;
	}


	public static Map<String, String> toStringMap(String args) {
		Map<String,String> argsMap = new HashMap<String,String>();
		JSONObject jsonObject=JSONObject.fromObject(args.substring(1,args.length()-1));
		Iterator it=jsonObject.keys();
		while(it.hasNext()){
			String key=String.valueOf(it.next());
			String value=(String)jsonObject.get(key);
			argsMap.put(key,value);
		}
		return argsMap;
	}
	
	public static String toJsonString(Object obj) {
		if(obj == null) {
			return "";
		}
		try {
			return om.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("转Json出错。。。。。。");
			logger.error("error msg:", e);
			return null;
		}
	}
	
	
	public static <T> T fromString(String jsonString, Class<T> c) {
		try {
			return om.readValue(jsonString, c);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T> List<T> toList(String jsonString, Class<T> c) {
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		try {
			return om.readValue(jsonString, typeFactory.constructCollectionType(List.class, c));
		} catch (Exception e) {
			logger.error("Json转对象出错，jsonString=" + jsonString);
			logger.error("error msg:", e);
			e.printStackTrace();
			return null;
		}


		}


		public static void  getJsonUtil(HttpServletResponse response,Object object){
			response.setContentType("text/html;charset=UTF-8");
			try {
				if(null != object) {
					String jsonString=JsonUtil.toJsonString(object);
						if(null != jsonString) {
							response.getWriter().write(jsonString);
						}
				}
			} catch (IOException e) {
				logger.error(e.getMessage()+"jsonUtil方法中gerJsonUtil()方法出错",e);
				e.printStackTrace();
			}
		}

	public static void  getJsonUtilFormat(HttpServletResponse response,Object object){
		response.setContentType("application/json; charset=utf-8");
		try {
			response.getWriter().write(JsonUtil.formatJson(JsonUtil.toJsonString(object)));
		} catch (IOException e) {
			logger.error(e.getMessage()+"jsonUtil方法中gerJsonUtil()方法出错",e);
			e.printStackTrace();
		}
	}

	/**
	 * 将json转化为实体POJO
	 * @param jsonStr jsonStr
	 * @param obj obj
	 * @return
	 */
	public static<T> Object JSONToObj(String jsonStr,Class<T> obj) {
		T t = null;
		try {
			t = om.readValue(jsonStr,obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	public  static void writerJson(HttpServletResponse response,Object obj){
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(JsonUtil.toJsonString(obj));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static String formatJson(String jsonStr) {
		if (null == jsonStr || "".equals(jsonStr)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		char current = '\0';
		int indent = 0;
		boolean isInQuotationMarks = false;
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
			switch (current) {
				case '"':
					if (last != '\\'){
						isInQuotationMarks = !isInQuotationMarks;
					}
					sb.append(current);
					break;
				case '{':
				case '[':
					sb.append(current);
					if (!isInQuotationMarks) {
						sb.append('\n');
						indent++;
						addIndentBlank(sb, indent);
					}
					break;
				case '}':
				case ']':
					if (!isInQuotationMarks) {
						sb.append('\n');
						indent--;
						addIndentBlank(sb, indent);
					}
					sb.append(current);
					break;
				case ',':
					sb.append(current);
					if (last != '\\' && !isInQuotationMarks) {
						sb.append('\n');
						addIndentBlank(sb, indent);
					}
					break;
				default:
					sb.append(current);
			}
		}

		return sb.toString();
//        return "this is a format";
	}

	/**
	 * 添加space
	 *
	 * @param sb 字符串
	 * @param indent indent
	 * @author YS
	 *
	 */
	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append('\t');
		}
	}
}
