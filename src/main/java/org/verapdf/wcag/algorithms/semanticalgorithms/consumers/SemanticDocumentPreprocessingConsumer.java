package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import java.util.SortedSet;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemanticDocumentPreprocessingConsumer extends WCAGConsumer implements Consumer<INode> {
    private static final Logger LOGGER = Logger.getLogger(SemanticDocumentPreprocessingConsumer.class.getCanonicalName());

    private long textChunksNumber;
    private long structElementsNumber;

    public SemanticDocumentPreprocessingConsumer() {
        setNodeParents();
        this.textChunksNumber = 0L;
        this.structElementsNumber = 0L;
    }

    public void setNodeParents() {
        Stack<INode> nodeStack = new Stack<>();
        INode root = StaticContainers.getDocument().getTree().getRoot();
        nodeStack.push(root);
        root.setDepth(0);
        nodeStack.add(root);
        while (!nodeStack.isEmpty()) {
            INode node = nodeStack.pop();
            for (int i = 0; i < node.getChildren().size(); i++) {
                INode child = node.getChildren().get(i);
                child.setParent(node);
                child.setIndex(i);
                child.setDepth(node.getDepth() + 1);
                nodeStack.push(child);
            }
        }
    }

    @Override
    public void accept(INode node) {
        // setup parent nodes for children

        if (node instanceof SemanticSpan) {
            textChunksNumber++;
            if (!node.getChildren().isEmpty()) {
                LOGGER.log(Level.WARNING, "Text chunk cannot contain children: {}", node);
            }
        } else if (node.getInitialSemanticType() == SemanticType.SPAN) {
            for (INode child : node.getChildren()) {
                if (child.getInitialSemanticType() == SemanticType.SPAN) {
                    LOGGER.log(Level.WARNING, "Nested semantic span: {}", child);
                } else if (!(child instanceof SemanticSpan)) {
                    LOGGER.log(Level.WARNING, "Semantic span contains child of unexpected semantic type: {}", child);
                }
            }
        }
        if (node instanceof SemanticSpan) {
            checkUnderlinedText(((SemanticSpan)node));
        }
        StaticContainers.getObjectKeyMapper().put(node.getObjectKeyNumber(), node);
        if (node.getChildren().isEmpty()) {
            return;
        }
        structElementsNumber++;
        MultiBoundingBox boundingBox = new MultiBoundingBox(node.getBoundingBox());
        for (INode child : node.getChildren()) {
            if (!(child instanceof SemanticFigure) && !(child instanceof SemanticAnnot)) {
                boundingBox.union(child.getBoundingBox());
            }
        }
        node.setBoundingBox(boundingBox);
    }

    private void checkUnderlinedText(SemanticSpan span) {
        for (TextColumn textColumn : span.getColumns()) {
            for (TextLine textLine : textColumn.getLines()) {
                for (TextChunk textChunk : textLine.getTextChunks()) {
                    if (!TextChunkUtils.isWhiteSpaceChunk(textChunk)) {
                        checkUnderlinedText(textChunk);
                    }
                }
            }
        }
    }

    private void checkUnderlinedText(TextChunk textChunk) {
        if (textChunk.getPageNumber() != null && StaticContainers.getDocument() != null) {
            for (LineChunk lineChunk : getHorizontalLines(textChunk)) {
                if (isUnderlinedText(textChunk, lineChunk)) {
                    textChunk.setIsUnderlinedText();
                    return;
                }
            }
        }
    }

    private SortedSet<LineChunk> getHorizontalLines(TextChunk textChunk) {
        SortedSet<LineChunk> lines = StaticContainers.getLinesCollection().getHorizontalLines(textChunk.getPageNumber());
        return lines.subSet(new LineChunk(textChunk.getPageNumber(), -Double.MAX_VALUE, textChunk.getBaseLine(),
                        -Double.MAX_VALUE, textChunk.getBaseLine()),
                new LineChunk(textChunk.getPageNumber(), Double.MAX_VALUE,
                        textChunk.getBaseLine() - NodeUtils.UNDERLINED_TEXT_EPSILONS[1] * textChunk.getBoundingBox().getHeight(), Double.MAX_VALUE,
                        textChunk.getBaseLine() - NodeUtils.UNDERLINED_TEXT_EPSILONS[1] * textChunk.getBoundingBox().getHeight()));
    }

    private boolean isUnderlinedText(TextChunk textChunk, LineChunk lineChunk) {
        if (NodeUtils.areOverlapping(textChunk, lineChunk) &&
                (lineChunk.getWidth() < NodeUtils.UNDERLINED_TEXT_EPSILONS[2] * textChunk.getBoundingBox().getHeight())) {
            return true;
        }
        return false;
    }

    public long getTextChunksNumber() {
        return textChunksNumber;
    }

    public long getStructElementsNumber() {
        return structElementsNumber;
    }

    @Override
    public WCAGProgressStatus getWCAGProgressStatus() {
        return WCAGProgressStatus.DOCUMENT_PREPROCESSING;
    }
}
