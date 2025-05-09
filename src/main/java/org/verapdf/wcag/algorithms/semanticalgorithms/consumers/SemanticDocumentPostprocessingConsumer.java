package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SemanticDocumentPostprocessingConsumer extends WCAGConsumer {

	public void runPostprocessingChecks(ITree tree) {
		updateBoundingBoxes(tree);
		checkForTitle(tree);
		checkForRepeatedCharacters(tree);
		setLowestDepthErrorFlag(tree);
		updateIDs(tree);
	}

	public void updateBoundingBoxes(ITree tree) {
		for (INode node : tree) {
			MultiBoundingBox boundingBox = new MultiBoundingBox();
			for (INode child : node.getChildren()) {
				if (!(child instanceof SemanticAnnot)) {
					boundingBox.union(child.getBoundingBox());
				}
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
		checkForRepeatedCharacters(tree.getRoot(), chunks);
		checkRepeatedAndAdd(chunks);
	}

	public void checkForRepeatedCharacters(INode node, List<TextChunk> chunks) {
		for (INode child : node.getChildren()) {
			if (child.getInitialSemanticType() != SemanticType.FIGURE && child.getInitialSemanticType() != SemanticType.CODE) {
				checkForRepeatedCharacters(child, chunks);
			}
			if (child instanceof SemanticSpan) {
				for (TextColumn textColumn : ((SemanticSpan) child).getColumns()) {
					for (TextLine textLine : textColumn.getLines()) {
						for (TextChunk textChunk : textLine.getTextChunks()) {
							if (textChunk.getValue().isEmpty()) {
								continue;
							}
							if (!chunks.isEmpty() && areChunksChained(chunks.get(chunks.size() - 1), textChunk)) {
								chunks.add(textChunk);
							} else {
								checkRepeatedAndAdd(chunks);
								chunks.clear();
								chunks.add(textChunk);
							}
						}
					}
				}
			}
		}
	}

	private boolean areChunksChained(TextChunk previousTextChunk, TextChunk secondTextChunk) {
		if (!TextChunkUtils.areTextChunksHaveSameBaseLine(previousTextChunk, secondTextChunk)) {
			return false;
		}
		char firstChar = previousTextChunk.getValue().charAt(previousTextChunk.getValue().length() - 1);
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
		if ((ListUtils.isDetectedListNode(node) || ListUtils.isInitialListNode(node)) &&
				node.getSemanticType() != node.getInitialSemanticType()) {
			node.setHasLowestDepthError();
			return;
		}
		if ((TOCUtils.isTOCNode(node) || TOCUtils.isInitialTOCNode(node)) &&
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
		return HeadingUtils.isDetectedHeadingNode(node) && !HeadingUtils.isInitialHeadingNode(node);
	}

	private void checkRepeatedAndAdd(List<TextChunk> textChunks) {
		if (textChunks.isEmpty()) {
			return;
		}
		int length = 0;
		boolean isLastCharacterWhiteSpace = false;
		Character lastCharacter = null;
		MultiBoundingBox resultBox = new MultiBoundingBox();
		for (TextChunk textChunk : textChunks) {
			char[] characters = textChunk.getValue().toCharArray();
			lastCharacter = characters[0];
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
						if (!Character.isDigit(lastCharacter)) {
							StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
									length, resultBox));
						}
					}
					resultBox = new MultiBoundingBox();
					length = 1;
					start = charIndex;
					lastCharacter = character;
					isLastCharacterWhiteSpace = TextChunkUtils.isWhiteSpaceChar(lastCharacter);
				}
			}
			updateBoundingBox(resultBox, textChunk, start, characters.length);
		}
		if (length > 2) {
			if (!Character.isDigit(lastCharacter)) {
				StaticContainers.getRepeatedCharacters().add(new RepeatedCharacters(!isLastCharacterWhiteSpace,
						length, resultBox));
			}
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

	private void updateIDs(ITree tree) {
		for (INode node : tree) {
			Long newId = StaticContainers.getIdMapper().get(node.getRecognizedStructureId());
			if (newId != null) {
				node.setRecognizedStructureId(newId);
			}
		}
	}

	@Override
	public WCAGProgressStatus getWCAGProgressStatus() {
		return WCAGProgressStatus.DOCUMENT_POSTPROCESSING;
	}
}
