package org.cae.p2h;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutorService;

import org.cae.p2h.task.ITransformTask;
import org.cae.p2h.task.TransformTaskFactory;

public class TransformHandler {

	public void handle(){
		Container container=Container.getCurrent();
		File[] files=getPdfFile(container.getDataDir());
		container.setTotalNum(files.length);
		ExecutorService threadPool=container.getThreadPool();
		for(File file:files){
			ITransformTask task=TransformTaskFactory.getTransformTask(file.getName(), this);
			if(task!=null){
				threadPool.execute(task);
			}
		}
	}
	
	/**
	 * 
	 * @param dataDir 输入PDF文件所在文件夹的相对路径
	 * @return
	 */
	private File[] getPdfFile(String dataDir){
		File file=new File(dataDir);
		if(!file.exists()){
			file.mkdir();
		}
		File[] pdfFiles=new File(dataDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String filename = pathname.getName().toLowerCase();
				if(filename.endsWith(".pdf")){
					return true;
				}else{
					return false;
				}
			}
		});
		return pdfFiles;
	}
	
	public void onFinish(String fileName,boolean isSuccessed){
		Container container=Container.getCurrent();
		container.addFinishNum();
		int nowNum=container.getFinishNum();
		if(!isSuccessed){
			container.addFailNum();
		}
		int totalNum=container.getTotalNum();
		System.out.println("文件"+fileName+"转化成功,目前进度为"+nowNum+"/"+totalNum);
		if(nowNum==totalNum){
			System.out.println("文件转换全部完成");
			container.getThreadPool().shutdown();
		}
	}
}
