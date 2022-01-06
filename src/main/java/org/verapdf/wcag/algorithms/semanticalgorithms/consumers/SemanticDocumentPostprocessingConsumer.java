package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;

public class SemanticDocumentPostprocessingConsumer {

	private final AccumulatedNodeMapper accumulatedNodeMapper;
	private List<RepeatedCharacters> repeatedCharacters = new ArrayList<>();

	public SemanticDocumentPostprocessingConsumer(AccumulatedNodeMapper accumulatedNodeMapper) {
		this.accumulatedNodeMapper = accumulatedNodeMapper;
	}

	public List<RepeatedCharacters> getRepeatedCharacters() {
		return repeatedCharacters;
	}

	public void checkForTitle(ITree tree) {
		for (INode node : tree) {
			if (checkNode(node)) {
				break;
			}
		}
	}

	public void checkForRepeatedElements(ITree tree) {
		String valueToCheck = "";
		MultiBoundingBox multiBoundingBox = new MultiBoundingBox();
		for (INode node : tree) {
			if (node instanceof SemanticSpan) {
				for (TextLine textLine : ((SemanticSpan) node).getLines()) {
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
					repeatedCharacters.add(new RepeatedCharacters(!isLastCharacterWhiteSpace, length, boundingBox));
				}
				length = 1;
				lastCharacter = character;
				isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
			}
		}
		if (length > 2) {
			repeatedCharacters.add(new RepeatedCharacters(!isLastCharacterWhiteSpace, length, boundingBox));
		}
	}
}
