package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

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
    }
}
