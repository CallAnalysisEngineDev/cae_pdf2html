package org.cae.p2h.task;

import org.cae.p2h.Container;
import org.cae.p2h.TransformHandler;

public class TransformTaskFactory {

	public static ITransformTask getTransformTask(String fileName,
			TransformHandler handler) {
		Container container = Container.getCurrent();
		String operationSystem = container.getOperationSystem();
		if (operationSystem.startsWith("Windows")) {
			return new WindowsTransformTask(fileName, container.getDestDir(),
					container.getDataDir(), handler);
		}
		return null;
	}
}
