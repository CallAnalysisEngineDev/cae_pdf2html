package org.cae.p2h;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cae.p2h.client.TransformResult;
import org.cae.p2h.task.ITransformTask;
import org.cae.p2h.task.Rar2Pdf;
import org.cae.p2h.task.TransformTaskFactory;

public class TransformHandler {

	private Log logger = LogFactory.getLog(this.getClass());

	// 倒数栅栏,用于检测是否全部转换任务都完成了
	private CountDownLatch countdown;

	public TransformResult handle() {
		Container container = Container.getCurrent();
		File[] files = getPdfFile(container.getDataDir());
		ExecutorService threadPool = container.getThreadPool();
		// task的列表,先对文件进行分析,得到需要转换的pdf文件的总数量
		List<ITransformTask> taskList = new ArrayList<ITransformTask>();
		// 所有解压后的文件夹名,先存放在这个list中,方便之后删除所有的解压文件夹
		List<String> rarFileNames = new ArrayList<String>();
		for (File file : files) {
			ITransformTask task = null;
			// 如果这是一个rar压缩文件
			if (file.getName().toLowerCase().endsWith(".rar")) {
				// 解压
				Rar2Pdf.pdfFileUnrar(file.getPath());
				String rarFileName = file.getName().substring(0,
						file.getName().indexOf("."));
				rarFileNames.add(rarFileName);
				File[] rarFiles = getPdfFile(container.getDataDir() + "/"
						+ rarFileName);
				// 将解压出来的文件都设置好输入输出路径,然后加入taskList
				for (File rarFile : rarFiles) {
					task = TransformTaskFactory.getTransformTask(
							rarFile.getName(), this);
					task.setDataDir(container.getDataDir() + "/" + rarFileName);
					task.setDestDir(container.getDestDir() + "/" + rarFileName);
					taskList.add(task);
				}
			} else {
				// 普通的pdf
				task = TransformTaskFactory.getTransformTask(file.getName(),
						this);
				taskList.add(task);
			}
		}
		logger.info("pdf文件分析完毕,共有" + taskList.size() + "个pdf文件,其中rar文件"
				+ rarFileNames.size() + "个");
		container.setTotalNum(taskList.size());
		countdown = new CountDownLatch(taskList.size());
		for (ITransformTask task : taskList) {
			threadPool.execute(task);
		}
		try {
			countdown.await();
			// 最后要删除所有解压出来的文件夹
			for (String rarFileName : rarFileNames) {
				deleteAllFilesOfDir(new File(container.getDataDir() + "/"
						+ rarFileName));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		TransformResult result;
		int failNum = container.getFailNum();
		if (failNum == 0) {
			result = new TransformResult();
		} else {
			List<String> failList = container.getFailList();
			result = new TransformResult(failList);
		}
		return result;
	}

	private static void deleteAllFilesOfDir(File path) {
		if (!path.exists())
			return;
		if (path.isFile()) {
			path.delete();
			return;
		}
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			deleteAllFilesOfDir(files[i]);
		}
		path.delete();
	}

	/**
	 * 
	 * @param dataDir
	 *            输入PDF文件所在文件夹的相对路径
	 * @return
	 */
	private File[] getPdfFile(String dataDir) {
		File file = new File(dataDir);
		if (!file.exists()) {
			file.mkdir();
		}
		File[] pdfFiles = new File(dataDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String filename = pathname.getName().toLowerCase();
				if (filename.endsWith(".pdf") || filename.endsWith(".rar")) {
					return true;
				} else {
					return false;
				}
			}
		});
		return pdfFiles;
	}

	public void onFinish(String fileName, boolean isSuccessed) {
		Container container = Container.getCurrent();
		container.addFinishNum();
		int nowNum = container.getFinishNum();
		int totalNum = container.getTotalNum();
		if (!isSuccessed) {
			container.addFailNum(fileName);
			logger.info("文件" + fileName + "转化失败,目前进度为" + nowNum + "/"
					+ totalNum + ",目前已失败" + container.getFailNum() + "个文件");
		} else {
			logger.info("文件" + fileName + "转化成功,目前进度为" + nowNum + "/"
					+ totalNum);
		}
		countdown.countDown();
		if (nowNum == totalNum) {
			logger.info("文件转换全部完成");
			logger.info("即将关闭线程池...");
			container.getThreadPool().shutdown();
			logger.info("线程池关闭成功,cae_pdf2html关闭");
		}
	}
}
