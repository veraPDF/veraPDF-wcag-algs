package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.List;

public interface INode extends IObject {

	SemanticType getSemanticType();

	void setSemanticType(SemanticType semanticType);

	SemanticType getInitialSemanticType();

	Double getCorrectSemanticScore();

	void setCorrectSemanticScore(Double correctSemanticScore);

	List<INode> getChildren();

	void setParent(INode node);

	INode getParent();

	INode getNextNeighbor();

	INode getPreviousNeighbor();

	INode getNextNode();

	INode getPreviousNode();

	void setIndex(Integer index);

	Integer getIndex();

	boolean isRoot();

	void addChild(INode child);

	boolean isLeaf();

	public NodeInfo getNodeInfo();

	public int getDepth();

	public void setDepth(int depth);

	public boolean getHasLowestDepthError();

	public void setHasLowestDepthError();

	public IAttributesDictionary getAttributesDictionary();

	public void setAttributesDictionary(IAttributesDictionary AttributesDictionary);

	public Integer getObjectKeyNumber();
}
