package org.cae.p2h.task;

import java.io.File;
import java.io.FileOutputStream;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class Rar2Pdf {
	public static void pdfFileUnrar(String rarFilePath){
		if(!rarFilePath.endsWith("rar")){
			System.err.println("文件格式错误，请选择正确的rar文件");
		}
		File rarFile=new File(rarFilePath);
		String nameWithEnds=rarFile.getName();
		String name=nameWithEnds.substring(0,nameWithEnds.lastIndexOf("."));
		Archive archive=null;
		try {
			archive=new Archive(rarFile);
			if(archive!=null){
				FileHeader fileHeader=archive.nextFileHeader();
				while(fileHeader!=null){
					String fileName=fileHeader.getFileNameW().isEmpty()?fileHeader.getFileNameString():
						fileHeader.getFileNameW();
					if(fileHeader.isDirectory()){
						File file=new File(rarFile.getParent()+File.separator
								+name+File.separator+fileName);
						file.mkdirs();
					}else{
						File pdFile=new File(rarFile.getParent()+File.separator
								+name+File.separator+fileName.trim());
						if(!pdFile.exists()){
							if(!pdFile.getParentFile().exists()){
								pdFile.getParentFile().mkdirs();
							}
						pdFile.createNewFile();
						FileOutputStream outputStream=new FileOutputStream(pdFile);
						archive.extractFile(fileHeader, outputStream);
						outputStream.close();
						}
					}
					fileHeader=archive.nextFileHeader();
				}
				
			}
			archive.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
