package org.cae.p2h.task;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cae.p2h.TransformHandler;

public class WindowsTransformTask implements ITransformTask {

	private Log logger = LogFactory.getLog(this.getClass());

	private String fileName;
	private String destDir;// 输出路径
	private String dataDir;// 输入路径
	private TransformHandler handler;

	public WindowsTransformTask(String fileName, String destDir,
			String dataDir, TransformHandler handler) {
		this.fileName = fileName;
		this.destDir = destDir;
		this.dataDir = dataDir;
		this.handler = handler;
	}

	@Override
	public void run() {
		String prefix = fileName.substring(fileName.lastIndexOf("."));
		int num = prefix.length();
		String fileOtherName = fileName.substring(0, fileName.length() - num);
		String proPath = "pdf2htmlEX\\pdf2htmlEX.exe";
		String command = proPath + " --dest-dir " + destDir + " " + dataDir
				+ "\\\\" + "\"" + fileOtherName + ".pdf\" " + "\""
				+ fileOtherName + ".html\"";
		Runtime rt = Runtime.getRuntime();
		try {
			Process p = rt.exec(command);
			logger.info("文件" + fileName + "开始转化");
			p.waitFor();
			if (p.exitValue() == 0) {
				handler.onFinish(fileName, true);
			} else {
				handler.onFinish(fileName, false);
			}
		} catch (IOException e) {
			try {
				throw new Exception("没有找到pdf2htmlEX软件,请正确放置pdf2htmlEX于根目录位置");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	@Override
	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}

}
