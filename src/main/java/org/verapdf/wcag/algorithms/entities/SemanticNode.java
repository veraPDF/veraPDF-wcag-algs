package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.*;

public class SemanticNode extends BaseObject implements INode {

	private Double correctSemanticScore;
	private SemanticType semanticType;

	private Integer index = null;
	private INode parent = null;
	private final List<INode> children;
	private final SemanticType initialSemanticType;
	private int depth;
	private boolean hasLowestDepthError = false;
	private IAttributesDictionary attributesDictionary;

	public NodeInfo nodeInfo;

	public SemanticNode() {
		this((SemanticType)null);
	}

	public SemanticNode(SemanticType initialSemanticType) {
		this(initialSemanticType, new LinkedList<>(), new LinkedList<>());
	}

	public SemanticNode(SemanticType initialSemanticType, List<Integer> errorCodes, List<List<Object>> errorArguments) {
		super(new BoundingBox(), errorCodes, errorArguments);
		nodeInfo = new NodeInfo();
		this.children = new ArrayList<>();
		this.initialSemanticType = initialSemanticType;
	}

	public SemanticNode(BoundingBox bbox, SemanticType initialSemanticType, SemanticType semanticType) {
		this(bbox, initialSemanticType);
		this.semanticType = semanticType;
	}

	public SemanticNode(BoundingBox bbox, SemanticType initialSemanticType) {
		super(new MultiBoundingBox(bbox));
		this.nodeInfo = new NodeInfo();
		this.children = new ArrayList<>();
		this.initialSemanticType = initialSemanticType;
	}

	public SemanticNode(BoundingBox bbox) {
		this(bbox, null);
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
	public Integer getIndex() {
		return index;
	}

	@Override
	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public INode getNextNeighbor() {
		if (parent != null && index != null && index + 1 < parent.getChildren().size()) {
			return parent.getChildren().get(index + 1);
		}
		return null;
	}

	@Override
	public INode getPreviousNeighbor() {
		if (parent != null && index != null && index > 0) {
			return parent.getChildren().get(index - 1);
		}
		return null;
	}

	@Override
	public INode getNextNode() {
		INode nextNeighbor = getNextNeighbor();
		if (nextNeighbor != null) {
			return nextNeighbor;
		} else {
			return parent != null ? parent.getNextNode() : null;
		}
	}

	@Override
	public INode getPreviousNode() {
		INode previousNeighbor = getPreviousNeighbor();
		if (previousNeighbor != null) {
			return previousNeighbor;
		} else {
			return parent != null ? parent.getPreviousNode() : null;
		}
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
	public int getDepth() {
		return depth;
	}

	@Override
	public void setHasLowestDepthError() {
		this.hasLowestDepthError = true;
	}

	@Override
	public IAttributesDictionary getAttributesDictionary() {
		return attributesDictionary != null ? attributesDictionary : new AttributesDictionary();
	}

	@Override
	public void setAttributesDictionary(IAttributesDictionary AttributesDictionary) {
		this.attributesDictionary = AttributesDictionary;
	}

	@Override
	public boolean getHasLowestDepthError() {
		return hasLowestDepthError;
	}

	@Override
	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getBoundingBox());
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
		return that.getBoundingBox().equals(getBoundingBox());
	}

	@Override
	public String toString() {
		return "SemanticNode{" +
		       "initialSemanticType=" + initialSemanticType +
		       ", correctSemanticScore=" + correctSemanticScore +
		       ", pageNumber=" + getBoundingBox().getPageNumber() +
		       ", boundingBox=" + getBoundingBox() +
		       ", semanticType=" + semanticType +
		       ", children=" + children +
		       '}';
	}
}
