package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

public class WCAGValidationInfo {

	private volatile WCAGProgressStatus wcagProgressStatus = null;
	private volatile Boolean abortProcessing = false;

	public WCAGProgressStatus getWCAGProgressStatus() {
		return wcagProgressStatus;
	}

	public void setWCAGProgressStatus(WCAGProgressStatus wcagProgressStatus) {
		this.wcagProgressStatus = wcagProgressStatus;
	}

	public Boolean getAbortProcessing() {
		return abortProcessing;
	}

	public void setAbortProcessing(Boolean abortProcessing) {
		this.abortProcessing = abortProcessing;
	}
}
