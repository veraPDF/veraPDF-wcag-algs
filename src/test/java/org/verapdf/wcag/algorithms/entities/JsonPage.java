package org.verapdf.wcag.algorithms.entities;

import java.util.List;
import java.util.Objects;

public class JsonPage {

	private int pageNumber;
	private String pageLabel;
	private List<JsonNode> artifacts;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getPageLabel() {
		return pageLabel;
	}

	public void setPageLabel(String pageLabel) {
		this.pageLabel = pageLabel;
	}

	public List<JsonNode> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<JsonNode> artifacts) {
		this.artifacts = artifacts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonPage jsonPage = (JsonPage) o;
		return Objects.equals(artifacts, jsonPage.artifacts) &&
				Objects.equals(pageLabel, jsonPage.pageLabel) && jsonPage.pageNumber == pageNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pageNumber, pageLabel, artifacts);
	}

	@Override
	public String toString() {
		return "JsonPage{" +
		       ", pageNumber=" + pageNumber +
				", pageLabel=" + pageLabel +
				", artifacts=" + artifacts +
				'}';
	}
}
