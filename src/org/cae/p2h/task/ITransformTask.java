package org.cae.p2h.task;

public interface ITransformTask extends Runnable {

	void setDataDir(String dataDir);

	void setDestDir(String destDir);
}
