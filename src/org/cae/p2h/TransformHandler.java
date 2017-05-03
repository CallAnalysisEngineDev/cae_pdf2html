package org.cae.p2h;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.cae.p2h.task.ITransformTask;
import org.cae.p2h.task.Rar2Pdf;
import org.cae.p2h.task.TransformTaskFactory;

public class TransformHandler {

	private CountDownLatch countdown;
	
	public void handle(){
		Container container=Container.getCurrent();
		File[] files=getPdfFile(container.getDataDir());
		ExecutorService threadPool=container.getThreadPool();
		List<ITransformTask> taskList=new ArrayList<ITransformTask>();
		for(File file:files){
			ITransformTask task = null;
			if(file.getName().toLowerCase().endsWith(".rar")){
				Rar2Pdf.pdfFileUnrar(file.getPath());
				String rarFileName=file.getName().substring(0, file.getName().indexOf("."));
				File[] rarFiles=getPdfFile(container.getDataDir()+"/"+rarFileName);
				for(File rarFile:rarFiles){
					task=TransformTaskFactory.getTransformTask(rarFile.getName(), this);
					task.setDataDir(container.getDataDir()+"/"+rarFileName);
					task.setDestDir(container.getDestDir()+"/"+rarFileName);
					taskList.add(task);
				}
				new File(container.getDataDir()+"/"+rarFileName).delete();
			}
			else{
				task=TransformTaskFactory.getTransformTask(file.getName(), this);
				taskList.add(task);
			}
		}
		container.setTotalNum(taskList.size());
		countdown=new CountDownLatch(taskList.size());
		for(ITransformTask task:taskList){
			threadPool.execute(task);
		}
		try {
			countdown.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
				if(filename.endsWith(".pdf")||filename.endsWith(".rar")){
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
		int totalNum=container.getTotalNum();
		if(!isSuccessed){
			container.addFailNum();
			System.out.println("文件"+fileName+"转化失败,目前进度为"+nowNum+"/"+totalNum);
		}
		else{
			System.out.println("文件"+fileName+"转化成功,目前进度为"+nowNum+"/"+totalNum);
		}
		countdown.countDown();
		if(nowNum==totalNum){
			System.out.println("文件转换全部完成");
			container.getThreadPool().shutdown();
		}
	}
}
