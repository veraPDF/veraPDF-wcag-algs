package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;

import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemanticDocumentPreprocessingConsumer implements Consumer<INode> {
    private static final Logger LOGGER = Logger.getLogger(SemanticDocumentPreprocessingConsumer.class.getCanonicalName());

    private final IDocument document;
    private final LinesCollection linesCollection;

    public SemanticDocumentPreprocessingConsumer(IDocument document, LinesCollection linesCollection) {
        this.document = document;
        this.linesCollection = linesCollection;
    }

    public void accept(INode node) {
        // setup parent nodes for children
        for (INode child : node.getChildren()) {
            child.setParent(node);
        }

        if (node instanceof SemanticSpan) {
            if (node.getChildren().size() != 0) {
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
    }

    private void checkUnderlinedText(SemanticSpan span) {
        for (TextLine textLine : span.getLines()) {
            for (TextChunk textChunk : textLine.getTextChunks()) {
                checkUnderlinedText(textChunk);
            }
        }
    }

    private void checkUnderlinedText(TextChunk textChunk) {
        if (textChunk.getPageNumber() != null && document != null) {
            for (LineChunk lineChunk : getHorizontalLines(textChunk)) {
                if (isUnderlinedText(textChunk, lineChunk)) {
                    textChunk.setIsUnderlinedText();
                    return;
                }
            }
        }
    }

    private SortedSet<LineChunk> getHorizontalLines(TextChunk textChunk) {
        SortedSet<LineChunk> lines = linesCollection.getHorizontalLines(textChunk.getPageNumber());
        return lines.subSet(new LineChunk(textChunk.getPageNumber(), Double.MIN_VALUE, textChunk.getBaseLine(),
                        Double.MIN_VALUE, textChunk.getBaseLine()),
                new LineChunk(textChunk.getPageNumber(), Double.MAX_VALUE,
                        textChunk.getBaseLine() - 0.3 * textChunk.getBoundingBox().getHeight(), Double.MAX_VALUE,
                        textChunk.getBaseLine() - 0.3 * textChunk.getBoundingBox().getHeight()));
    }

    private boolean isUnderlinedText(TextChunk textChunk, LineChunk lineChunk) {
        if (NodeUtils.areOverlapping(textChunk, lineChunk) &&
                (lineChunk.getWidth() < 0.3 * textChunk.getBoundingBox().getHeight())) {
            return true;
        }
        return false;
    }
}
