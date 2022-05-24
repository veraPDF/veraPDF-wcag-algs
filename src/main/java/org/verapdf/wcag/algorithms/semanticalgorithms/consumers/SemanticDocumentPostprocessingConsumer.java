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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticDocumentPostprocessingConsumer {

	public void runPostprocessingChecks(ITree tree) {
		updateBoundingBoxes(tree);
		checkForTitle(tree);
		checkForRepeatedCharacters(tree);
		setLowestDepthErrorFlag(tree);
	}

	public void updateBoundingBoxes(ITree tree) {
		for (INode node : tree) {
			MultiBoundingBox boundingBox = new MultiBoundingBox();
			for (INode child : node.getChildren()) {
				boundingBox.union(child.getBoundingBox());
			}
			if (!node.getChildren().isEmpty()) {
				node.setBoundingBox(boundingBox);
			}
		}
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
		List<TextChunk> chunks = new ArrayList<>();
		for (INode node : tree) {
			if (node instanceof SemanticSpan) {
				for (TextColumn textColumn : ((SemanticSpan) node).getColumns()) {
					for (TextLine textLine : textColumn.getLines()) {
						for (TextChunk textChunk : textLine.getTextChunks()) {
							if (textChunk.getValue().isEmpty()) {
								continue;
							}
							if (!chunks.isEmpty() && areChunksChained(chunks.get(chunks.size() - 1).getValue(), textChunk)) {
								chunks.add(textChunk);
							} else {
								checkRepeatedAndAdd(chunks);
								chunks = new ArrayList<>(Collections.singletonList(textChunk));
							}
						}
					}
				}
			}
		}
		checkRepeatedAndAdd(chunks);
	}

	private boolean areChunksChained(String previousValue, TextChunk secondTextChunk) {
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

	private void checkRepeatedAndAdd(List<TextChunk> textChunks) {
		if (textChunks.isEmpty()) {
			return;
		}
		int length = 0;
		boolean isLastCharacterWhiteSpace = false;
		MultiBoundingBox resultBox = new MultiBoundingBox();
		for (TextChunk textChunk : textChunks) {
			char[] characters = textChunk.getValue().toCharArray();
			char lastCharacter = characters[0];
			isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
			int start = 0;
			length++;
			for (int charIndex = 1; charIndex < characters.length; charIndex++) {
				char character = characters[charIndex];
				if (isLastCharacterWhiteSpace && TextChunkUtils.isWhiteSpaceChar(character) || lastCharacter == character) {
					length++;
				} else {
					if (length > 2) {
						updateBoundingBox(resultBox, textChunk, start, charIndex);
						StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
						                                                                    length, resultBox));
						resultBox = new MultiBoundingBox();
					}
					length = 1;
					start = charIndex;
					lastCharacter = character;
					isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
				}
			}
			updateBoundingBox(resultBox, textChunk, start, characters.length);
		}
		if (length > 2) {
			StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
			                                                                    length, resultBox));
		}
	}

	private void updateBoundingBox(MultiBoundingBox resultBox, TextChunk textChunk, int start, int end) {
		BoundingBox boundingBox = new BoundingBox(textChunk.getBoundingBox());
		if (textChunk.isLeftRightHorizontalText()) {
			boundingBox.setLeftX(textChunk.getSymbolStartCoordinate(start));
			boundingBox.setRightX(textChunk.getSymbolEndCoordinate(end - 1));
		} else if (textChunk.isRightLeftHorizontalText()) {
			boundingBox.setLeftX(textChunk.getSymbolEndCoordinate(end - 1));
			boundingBox.setRightX(textChunk.getSymbolStartCoordinate(start));
		} else if (textChunk.isBottomUpVerticalText()) {
			boundingBox.setBottomY(textChunk.getSymbolStartCoordinate(start));
			boundingBox.setTopY(textChunk.getSymbolEndCoordinate(end - 1));
		} else if (textChunk.isUpBottomVerticalText()) {
			boundingBox.setBottomY(textChunk.getSymbolEndCoordinate(end - 1));
			boundingBox.setTopY(textChunk.getSymbolStartCoordinate(start));
		}
		resultBox.union(boundingBox);
	}
}
