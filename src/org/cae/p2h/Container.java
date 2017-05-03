package org.cae.p2h;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Container {

	private boolean isStarted = false;
	
	private final static Container container = new Container();
	
	private Context context;
	
	private final static String CONFIGURATION = "/p2h.properties";
	
	private final static String DEFAULT_CONFIGURATION = "/default_p2h.properties";
	
	private final static String FILE_DATADIR = "file.datadir";
	
	private final static String FILE_DESTDIR = "file.destdir";
	
	private final static String THREADPOOL_TYPE = "threadpool.type";
	
	private final static String THREADPOOL_THREADNUM = "threadpool.threadnum";
	
	public static Container getCurrent(){
		return container;
	}
	
	private Container(){
		if(!isStarted)
			init();
	}
	
	private void init(){
		if(context==null){
			context=new Context();
		}
		Properties properties = null;
		try {
			InputStream in = this.getClass().getResourceAsStream(CONFIGURATION);
			if(in==null)
				in=this.getClass().getResourceAsStream(DEFAULT_CONFIGURATION);
			properties = new Properties();
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if(properties.size()==0)
			return;
		
		initFileLocation(properties);
		initThreadPool(properties);
		context.set(Context.FINISH_NUM, new AtomicInteger());
		context.set(Context.FAIL_NUM, new AtomicInteger());
		context.set(Context.OPERATION_SYSTEM, System.getProperty("os.name"));
	}
	
	private void initFileLocation(Properties properties){
		context.set(Context.DATADIR, properties.get(FILE_DATADIR));
		context.set(Context.DESTDIR, properties.get(FILE_DESTDIR));
	}
	
	private void initThreadPool(Properties properties){
		String threadPoolType=properties.getProperty(THREADPOOL_TYPE);
		if(threadPoolType.equals("fix")){
			String threadNum=properties.getProperty(THREADPOOL_THREADNUM);
			if(threadNum==null){
				context.set(Context.THREADPOOL, Executors.newFixedThreadPool(getThreadCount()));
			}
			else{
				Integer num;
				try{
					num=Integer.parseInt(threadNum);
				} catch(NumberFormatException ex){
					System.err.println(THREADPOOL_THREADNUM+"的值不合法,不是一个整形数字");
					return;
				}
				context.set(Context.THREADPOOL, Executors.newFixedThreadPool(num));
			}
		}
		else if(threadPoolType.equals("single")){
			context.set(Context.THREADPOOL, Executors.newSingleThreadExecutor());
		}
		else if(threadPoolType.equals("cache")){
			context.set(Context.THREADPOOL, Executors.newCachedThreadPool());
		}
	}

	private int getThreadCount() {
		int nCpu=Runtime.getRuntime().availableProcessors();
		return nCpu;
	}
	
	public ExecutorService getThreadPool(){
		return (ExecutorService) context.get(Context.THREADPOOL);
	}
	
	public Integer getFinishNum(){
		return ((AtomicInteger)context.get(Context.FINISH_NUM)).intValue();
	}
	
	public void addFinishNum(){
		AtomicInteger finishNum=(AtomicInteger) context.get(Context.FINISH_NUM);
		finishNum.incrementAndGet();
		context.set(Context.FINISH_NUM, finishNum);
	}
	
	public Integer getTotalNum(){
		return (Integer) context.get(Context.TOTAL_NUM);
	}
	
	public void setTotalNum(Integer totalNum){
		context.set(Context.TOTAL_NUM, totalNum);
	}
	
	public String getDestDir(){
		return (String) context.get(Context.DESTDIR);
	}
	
	public String getDataDir(){
		return (String) context.get(Context.DATADIR);
	}
	
	public String getOperationSystem(){
		return (String) context.get(Context.OPERATION_SYSTEM);
	}
	
	public void addFailNum(){
		AtomicInteger failNum=(AtomicInteger) context.get(Context.FAIL_NUM);
		failNum.incrementAndGet();
		context.set(Context.FAIL_NUM, failNum);
	}
}
