package org.cae.p2h.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cae.p2h.TransformHandler;

public class TransformProxy {

	private Log logger = LogFactory.getLog(this.getClass());

	private TransformHandler handler = new TransformHandler();

	public TransformResult transform() {
		logger.info("pdf转换即将开始");
		return handler.handle();
	}
}
