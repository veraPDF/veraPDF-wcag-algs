package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.WCAGConsumer;

public class WCAGValidationInfo {

	private volatile Boolean abortProcessing = false;
	private WCAGConsumer currentConsumer = null;

	public String getWCAGProcessStatusWithPercent() {
		if (currentConsumer == null) {
			return null;
		}
		WCAGProgressStatus wcagProgressStatus = currentConsumer.getWCAGProgressStatus();
		if (wcagProgressStatus == null) {
			return null;
		}
		Double percent = currentConsumer.getPercent();
		if (percent == null) {
			return wcagProgressStatus.getValue();
		}
		return wcagProgressStatus.getValue() + " " + percent.intValue() + "%";
	}

	public Boolean getAbortProcessing() {
		return abortProcessing;
	}

	public void setAbortProcessing(Boolean abortProcessing) {
		this.abortProcessing = abortProcessing;
	}

	public WCAGConsumer getCurrentConsumer() {
		return currentConsumer;
	}

	public void setCurrentConsumer(WCAGConsumer currentConsumer) {
		this.currentConsumer = currentConsumer;
	}
}
