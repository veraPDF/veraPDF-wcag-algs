package org.verapdf.wcag.algorithms.entities;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;
import java.util.Set;

public interface INode {

	SemanticType getSemanticType();

	void setSemanticType(SemanticType semanticType);

	SemanticType getInitialSemanticType();

	void setRecognizedStructureId(Long id);

	Long getRecognizedStructureId();

	Double getCorrectSemanticScore();

	void setCorrectSemanticScore(Double correctSemanticScore);

	Integer getPageNumber();

	void setPageNumber(Integer pageNumber);

	Integer getLastPageNumber();

	void setLastPageNumber(Integer lastPageNumber);

	double getLeftX();

	double getRightX();

	double getBottomY();

	double getTopY();

	BoundingBox getBoundingBox();

	void setBoundingBox(BoundingBox boundingBox);

	List<INode> getChildren();

	void setParent(INode node);

	INode getParent();

	INode getNextNeighbor();

	INode getPreviousNeighbor();

	INode getNextNode();

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

	public Set<Integer> getErrorCodes();

	public void setErrorCodes(Set<Integer> errorCodes);
}
