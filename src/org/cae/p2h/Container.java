package org.cae.p2h;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cae.p2h.client.TransformResult;

public class Container {

	private Log logger=LogFactory.getLog(this.getClass());
	
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
		if(!isStarted){
			logger.info("cae_pdf2html即将启动");
			init();
		}
			
	}
	
	private void init(){
		if(context==null){
			context=new Context();
		}
		Properties properties = null;
		try {
			logger.info("正在读取配置文件p2h.properties");
			InputStream in = this.getClass().getResourceAsStream(CONFIGURATION);
			if(in==null){
				logger.info("没有找到配置文件p2h.properties,加载默认配置文件default_p2h.properties");
				in=this.getClass().getResourceAsStream(DEFAULT_CONFIGURATION);
			}
			properties = new Properties();
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if(properties.size()==0){
			logger.error("当前没有配置项!请正确填写配置文件!cae_pdf2html即将关闭...");
			return;
		}
		
		initFileLocation(properties);
		initThreadPool(properties);
		context.set(Context.FINISH_NUM, new AtomicInteger());
		context.set(Context.FAIL_NUM, new AtomicInteger());
		context.set(Context.FAIL_LIST, new ArrayList<String>());
		logger.info("当前的操作系统为:"+System.getProperty("os.name"));
		context.set(Context.OPERATION_SYSTEM, System.getProperty("os.name"));
	}
	
	private void initFileLocation(Properties properties){
		logger.info("当前的pdf输入文件夹名为:"+properties.get(FILE_DATADIR));
		context.set(Context.DATADIR, properties.get(FILE_DATADIR));
		logger.info("当前的html输出文件夹名为:"+properties.get(FILE_DESTDIR));
		context.set(Context.DESTDIR, properties.get(FILE_DESTDIR));
	}
	
	private void initThreadPool(Properties properties){
		String threadPoolType=properties.getProperty(THREADPOOL_TYPE);
		if(threadPoolType.equals("single")){
			logger.info("正在创建single类型的线程池...");
			context.set(Context.THREADPOOL, Executors.newSingleThreadExecutor());
		} else if(threadPoolType.equals("cache")){
			logger.info("正在创建cached类型的线程池...");
			context.set(Context.THREADPOOL, Executors.newCachedThreadPool());
		} else {
			if(threadPoolType==null||"".equals(threadPoolType)){
				logger.info("当前没有配置线程池,默认使用fix类型的线程池");
			}
			logger.info("正在创建fix类型的线程池...");
			String threadNum=properties.getProperty(THREADPOOL_THREADNUM);
			if(threadNum==null){
				logger.info("没有配置fix线程池的线程数量,将根据CPU核数来计算线程数量");
				context.set(Context.THREADPOOL, Executors.newFixedThreadPool(getThreadCount()));
			}
			else{
				Integer num;
				try{
					num=Integer.parseInt(threadNum);
				} catch(NumberFormatException ex){
					logger.error(THREADPOOL_THREADNUM+"的值不合法,不是一个整形数字");
					return;
				}
				logger.info("读取到线程数量,当前线程数为:"+num);
				context.set(Context.THREADPOOL, Executors.newFixedThreadPool(num));
			}
		}
	}

	private int getThreadCount() {
		int nCpu=Runtime.getRuntime().availableProcessors();
		logger.info("当前线程数为:"+nCpu);
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
	
	public void addFailNum(String fileName){
		AtomicInteger failNum=(AtomicInteger) context.get(Context.FAIL_NUM);
		failNum.incrementAndGet();
		context.set(Context.FAIL_NUM, failNum);
		List<String> failList=(List<String>) context.get(Context.FAIL_LIST);
		failList.add(fileName);
		context.set(Context.FAIL_LIST, failList);
	}
	
	public int getFailNum(){
		AtomicInteger failNum=(AtomicInteger) context.get(Context.FAIL_NUM);
		return failNum.intValue();
	}
	
	public List<String> getFailList(){
		return (List<String>) context.get(Context.FAIL_LIST);
	}
}
