package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SemanticNode implements INode {

	private Double correctSemanticScore;
	private BoundingBox boundingBox;
	private SemanticType semanticType;
	private Long recognizedStructureId = null;

	private INode parent = null;
	private final List<INode> children;
	private final SemanticType initialSemanticType;

	public NodeInfo nodeInfo;

	public SemanticNode() {
		nodeInfo = new NodeInfo();
		boundingBox = new BoundingBox();
		this.children = new ArrayList<>();
		this.initialSemanticType = null;
	}

	public SemanticNode(SemanticType initialSemanticType) {
		nodeInfo = new NodeInfo();
		boundingBox = new BoundingBox();
		this.children = new ArrayList<>();
		this.initialSemanticType = initialSemanticType;
	}

	public SemanticNode(BoundingBox bbox, SemanticType initialSemanticType, SemanticType semanticType) {
		this(bbox, initialSemanticType);
		this.semanticType = semanticType;
	}

	public SemanticNode(BoundingBox bbox, SemanticType initialSemanticType) {
		this.nodeInfo = new NodeInfo();
		this.children = new ArrayList<>();
		this.boundingBox = new MultiBoundingBox(bbox);
		this.initialSemanticType = initialSemanticType;
	}

	public SemanticNode(BoundingBox bbox) {
		this.nodeInfo = new NodeInfo();
		this.children = new ArrayList<>();
		this.boundingBox = new MultiBoundingBox(bbox);
		this.initialSemanticType = null;
	}

	@Override
	public Double getCorrectSemanticScore() {
		return correctSemanticScore;
	}

	@Override
	public void setCorrectSemanticScore(Double correctSemanticScore) {
		this.correctSemanticScore = correctSemanticScore;
	}

	@Override
	public Long getRecognizedStructureId() {
		return recognizedStructureId;
	}

	@Override
	public void setRecognizedStructureId(Long id) {
		recognizedStructureId = id;
	}

	@Override
	public Integer getPageNumber() {
		return boundingBox.getPageNumber();
	}

	@Override
	public void setPageNumber(Integer pageNumber) {
		boundingBox.setPageNumber(pageNumber);
	}

	@Override
	public Integer getLastPageNumber() {
		return boundingBox.getLastPageNumber();
	}

	@Override
	public void setLastPageNumber(Integer lastPageNumber) {
		boundingBox.setLastPageNumber(lastPageNumber);
	}

	@Override
	public double getLeftX() {
		return boundingBox.getLeftX();
	}

	@Override
	public double getBottomY() {
		return boundingBox.getBottomY();
	}

	@Override
	public double getRightX() {
		return boundingBox.getRightX();
	}

	@Override
	public double getTopY() {
		return boundingBox.getTopY();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(BoundingBox bbox) {
		if (bbox instanceof MultiBoundingBox) {
			boundingBox = new MultiBoundingBox(bbox);
		} else {
			boundingBox = new BoundingBox(bbox);
		}
	}

	@Override
	public SemanticType getSemanticType() {
		return semanticType;
	}

	@Override
	public void setSemanticType(SemanticType semanticType) {
		this.semanticType = semanticType;
	}

	@Override
	public SemanticType getInitialSemanticType() {
		return initialSemanticType;
	}

	@Override
	public List<INode> getChildren() {
		return children;
	}

	@Override
	public void addChild(INode child) {
		children.add(child);
	}

	@Override
	public void setParent(INode node) {
		parent = node;
	}

	@Override
	public INode getParent() {
		return parent;
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	@Override
	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(boundingBox);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SemanticNode that = (SemanticNode) o;
		return that.boundingBox.equals(boundingBox);
	}

	@Override
	public String toString() {
		return "SemanticNode{" +
		       "correctSemanticScore=" + correctSemanticScore +
		       ", pageNumber=" + boundingBox.getPageNumber() +
		       ", boundingBox=" + boundingBox +
		       ", semanticType=" + semanticType +
		       ", children=" + children +
		       '}';
	}
}
