package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemanticTreePreprocessingConsumer implements Consumer<INode> {
    private static final Logger LOGGER = Logger.getLogger(SemanticTreePreprocessingConsumer.class.getCanonicalName());

    public void accept(INode node) {
        if (node.getInitialSemanticType() == null) {
            return;
        }
        if (node.getInitialSemanticType() == SemanticType.SPAN) {
            if (node.getSemanticType() == SemanticType.SPAN) {
                if (node.getChildren().size() != 0) {
                    LOGGER.log(Level.WARNING, "Text chunk cannot contain children: {}", node);
                }
            } else {
                for (INode child : node.getChildren()) {
                    if (child.getInitialSemanticType() == SemanticType.SPAN) {
                        if (child.getSemanticType() != SemanticType.SPAN) {
                            LOGGER.log(Level.WARNING, "Nested semantic span: {}", child);
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "Semantic span contains child of unexpected semantic type: {}", child);
                    }
                }
            }
        }
    }
}
