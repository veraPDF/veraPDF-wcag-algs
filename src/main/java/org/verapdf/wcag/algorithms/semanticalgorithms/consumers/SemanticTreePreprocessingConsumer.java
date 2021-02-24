package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemanticTreePreprocessingConsumer implements Consumer<INode> {
    private static final Logger LOGGER = Logger.getLogger(SemanticTreePreprocessingConsumer.class.getCanonicalName());

    public void accept(INode node) {
        SemanticType initialSemanticType = node.getInitialSemanticType();
        if (initialSemanticType == null) {
            return;
        }
        if (initialSemanticType == SemanticType.SPAN) {
            if (node instanceof SemanticSpan) {
                SemanticSpan span = (SemanticSpan) node;
                for (INode child : node.getChildren()) {
                    if (child.getInitialSemanticType() == SemanticType.SPAN) {
                        LOGGER.log(Level.WARNING, "Nested semantic span: {}", child);
                    }
                    if (child instanceof SemanticSpan) {
                        span.addAll(((SemanticSpan) child).getTextChunks());
                    } else {
                        LOGGER.log(Level.WARNING, "Semantic span contains child of unexpected semantic type: {}", child);
                    }
                }
                // Semantic span can be only a leaf node
                span.getChildren().clear();
            }
            else {
                LOGGER.log(Level.WARNING, "Node instance doesn't correspond to semantic span: {}", node);
            }
        }
    }
}
