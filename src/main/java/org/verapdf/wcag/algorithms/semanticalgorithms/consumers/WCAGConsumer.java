package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

public class WCAGConsumer {

	public Double getPercent() {
		return null;
	}

	public WCAGProgressStatus getWCAGProgressStatus() {
		return null;
	}
	
	public boolean run() {
		return true;
	}

	public boolean startStep() {
		if (StaticContainers.getWCAGValidationInfo().getAbortProcessing()) {
			StaticContainers.getWCAGValidationInfo().setAbortProcessing(false);
			StaticContainers.getWCAGValidationInfo().setCurrentConsumer(null);
			return false;
		}
		StaticContainers.getWCAGValidationInfo().setCurrentConsumer(this);
		return true;
	}
}
