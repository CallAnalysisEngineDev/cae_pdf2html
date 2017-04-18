package org.cae.p2h.task;

import org.cae.p2h.TransformHandler;

public class WindowsTransformTask implements ITransformTask {

	private String fileName;
	private String destDir;
	private String dataDir;
	private TransformHandler handler;
	
	public WindowsTransformTask(String fileName,String destDir,String dataDir,TransformHandler handler){
		this.fileName=fileName;
		this.destDir=destDir;
		this.dataDir=dataDir;
		this.handler=handler;
	}
	
	@Override
	public void run() {
		String prefix=fileName.substring(fileName.lastIndexOf("."));
		int num=prefix.length();  
	     String fileOtherName=fileName.substring(0, fileName.length()-num); 
		String proPath="pdf2htmlEX\\pdf2htmlEX.exe";
		String command =proPath+" --dest-dir "+destDir+" "+dataDir+"\\\\"+"\""+fileOtherName+".pdf\" "+"\""+fileOtherName+".html\"";
		 Runtime rt = Runtime.getRuntime();  
		 try {
			Process p = rt.exec(command);
			System.out.println("文件"+fileName+"开始转化");
			p.waitFor();
			if(p.exitValue()==0){
				handler.onFinish(fileName, true);
			}else{
				handler.onFinish(fileName, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}

}
