package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;

public class SemanticDocumentPostprocessingConsumer {

	private final AccumulatedNodeMapper accumulatedNodeMapper;

	public SemanticDocumentPostprocessingConsumer(AccumulatedNodeMapper accumulatedNodeMapper) {
		this.accumulatedNodeMapper = accumulatedNodeMapper;
	}

	public void checkForTitle(ITree tree) {
		for (INode node : tree) {
			if (checkNode(node)) {
				break;
			}
		}
	}

	private boolean checkNode(INode node) {
		if (isTextNode(node)) {
			if (isTitle(node)) {
				node.setSemanticType(SemanticType.TITLE);
				return true;
			}
			INode parent = node.getParent();
			while (parent != null && accumulatedNodeMapper.get(parent) instanceof SemanticTextNode) {
				if (isTitle(parent)) {
					parent.setSemanticType(SemanticType.TITLE);
					break;
				}
				parent = parent.getParent();
			}
			return true;
		}
		return false;
	}

	private boolean isTextNode(INode node) {
		INode accumulatedNode = accumulatedNodeMapper.get(node);
		return accumulatedNode instanceof SemanticTextNode && !((SemanticTextNode) accumulatedNode).isSpaceNode() &&
		       !node.getChildren().isEmpty();
	}

	private boolean isTitle(INode node) {
		return SemanticType.HEADING.equals(node.getSemanticType()) && !SemanticType.HEADING.equals(node.getInitialSemanticType()) ||
		       SemanticType.NUMBER_HEADING.equals(node.getSemanticType()) && !SemanticType.NUMBER_HEADING.equals(node.getInitialSemanticType());
	}
}
