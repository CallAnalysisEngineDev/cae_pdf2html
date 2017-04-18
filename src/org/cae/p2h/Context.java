package org.cae.p2h;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private Map<String,Object> context =new HashMap<String,Object>();
	
	public final static String THREADPOOL = "threadPool";
	
	public final static String DESTDIR = "destDir";
	
	public final static String DATADIR = "dataDir";
	
	public final static String FINISH_NUM = "finishNum";
	
	public final static String TOTAL_NUM = "totalNum";
	
	public final static String FAIL_NUM = "failNum";
	
	public final static String OPERATION_SYSTEM = "operationSystem";
	
	public Object get(String key){
		return context.get(key);
	}
	
	public void set(String key,Object value){
		context.put(key, value);
	}
}
