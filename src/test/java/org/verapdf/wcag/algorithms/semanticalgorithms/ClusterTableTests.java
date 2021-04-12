package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.SemanticTree;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ClusterTableConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticTreePreprocessingConsumer;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ClusterTableTests {

    private static final double SEMANTIC_SCORE_TOLERANCE = 0.0001d;

    @Test
    void testTableSemanticCorrectness() throws IOException {
        INode root = JsonToPdfTree.getPdfTreeRoot("/files/NEG-fake-table.json");
        ITree tree = new SemanticTree(root);

        Consumer<INode> semanticTreeValidator = new SemanticTreePreprocessingConsumer();
        tree.forEach(semanticTreeValidator);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tree.forEach(tableFinder);

        List<INode> resultTables = tableFinder.getTables();

        Assertions.assertEquals(1, resultTables.size());
        if (resultTables.size() == 1) {
            Assertions.assertEquals(16, resultTables.get(0).getChildren().size());
        }
    }

}
