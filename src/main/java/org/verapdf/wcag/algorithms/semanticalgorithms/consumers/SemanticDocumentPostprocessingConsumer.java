package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

public class SemanticDocumentPostprocessingConsumer {

	public void runPostprocessingChecks(ITree tree) {
		checkForTitle(tree);
		checkForRepeatedCharacters(tree);
		setLowestDepthErrorFlag(tree);
	}


	public void setLowestDepthErrorFlag(ITree tree) {
		for (INode child : tree.getRoot().getChildren()) {
			setLowestDepthErrorFlag(child);
		}
	}

	public void checkForTitle(ITree tree) {
		for (INode node : tree) {
			if (checkNode(node)) {
				break;
			}
		}
	}

	public void checkForRepeatedCharacters(ITree tree) {
		String valueToCheck = "";
		MultiBoundingBox multiBoundingBox = new MultiBoundingBox();
		for (INode node : tree) {
			if (node instanceof SemanticSpan) {
				for (TextColumn textColumn : ((SemanticSpan) node).getColumns()) {
					for (TextLine textLine : textColumn.getLines()) {
						for (TextChunk textChunk : textLine.getTextChunks()) {
							if (!valueToCheck.isEmpty() && areTextChunksChained(valueToCheck, textChunk)) {
								valueToCheck += textChunk.getValue();
								multiBoundingBox.union(textChunk.getBoundingBox());
							} else {
								checkRepeatedAndAdd(valueToCheck, multiBoundingBox);
								valueToCheck = textChunk.getValue();
								multiBoundingBox = new MultiBoundingBox(textChunk.getBoundingBox());
							}
						}
					}
				}
			}
		}
		checkRepeatedAndAdd(valueToCheck, multiBoundingBox);
	}

	private boolean areTextChunksChained(String previousValue, TextChunk secondTextChunk) {
		char firstChar = previousValue.charAt(previousValue.length() - 1);
		char secondChar = secondTextChunk.getValue().charAt(0);
		if (TextChunkUtils.isWhiteSpaceChar(firstChar) && TextChunkUtils.isWhiteSpaceChar(secondChar)) {
			return true;
		}
		return firstChar == secondChar;
	}

	private boolean checkNode(INode node) {
		if (isTextNode(node)) {
			if (isTitle(node)) {
				node.setSemanticType(SemanticType.TITLE);
				return true;
			}
			INode parent = node.getParent();
			while (parent != null && StaticContainers.getAccumulatedNodeMapper().get(parent) instanceof SemanticTextNode) {
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

	private void setLowestDepthErrorFlag(INode node) {
		if ((TableUtils.isTableNode(node) || TableUtils.isInitialTableNode(node)) &&
				node.getSemanticType() != node.getInitialSemanticType()) {
			node.setHasLowestDepthError();
			return;
		}
		if ((ListUtils.isListNode(node) || ListUtils.isInitialListNode(node)) &&
				node.getSemanticType() != node.getInitialSemanticType()) {
			node.setHasLowestDepthError();
			return;
		}
		for (INode child : node.getChildren()) {
			setLowestDepthErrorFlag(child);
		}
	}

	private boolean isTextNode(INode node) {
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		return accumulatedNode instanceof SemanticTextNode && !((SemanticTextNode) accumulatedNode).isSpaceNode() &&
		       !node.getChildren().isEmpty();
	}

	private boolean isTitle(INode node) {
		return SemanticType.HEADING.equals(node.getSemanticType()) && !SemanticType.HEADING.equals(node.getInitialSemanticType()) ||
		       SemanticType.NUMBER_HEADING.equals(node.getSemanticType()) && !SemanticType.NUMBER_HEADING.equals(node.getInitialSemanticType());
	}

	private void checkRepeatedAndAdd(String value, BoundingBox boundingBox) {
		if (value == null || value.isEmpty()) {
			return;
		}
		char[] characters = value.toCharArray();
		char lastCharacter = characters[0];
		boolean isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
		int length = 0;
		for (char character : characters) {
			if (isLastCharacterWhiteSpace && TextChunkUtils.isWhiteSpaceChar(character) ||
			    lastCharacter == character) {
				length++;
			} else {
				if (length > 2) {
					StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
					                                                                    length, boundingBox));
				}
				length = 1;
				lastCharacter = character;
				isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
			}
		}
		if (length > 2) {
			StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
			                                                                    length, boundingBox));
		}
	}
}
