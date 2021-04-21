package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.SemanticTree;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ClusterTableConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticTreePreprocessingConsumer;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClusterTableTests {

    private static final double SEMANTIC_SCORE_TOLERANCE = 0.0001d;

    static Stream<Arguments> clusterTableDetectionTestParams() {
        return Stream.of(
                Arguments.of("NEG-fake-table.json", new int[][] {{4, 12}}, true),
                Arguments.of("NEG-bad-table.json", new int[][] {{4, 18}}, false),
                Arguments.of("NEG-bad-table2.json", new int[][] {{4, 19}}, false),
                Arguments.of("NEG-fake-list.json", new int[][] {{2, 6}}, true), // list should be distinguished from table
                Arguments.of("no-table.json", new int[][] {}, false),
                Arguments.of("NEG-floating-text-box.json", new int[][] {}, false),
                Arguments.of("2columns2.json", new int[][] {}, false),
//                Arguments.of("2columns3.json", new int[][] {}, false),
                Arguments.of("POS-real-table.json", new int[][] {{4, 88}, {4, 128}}, false)
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2} ) => {0}")
    @MethodSource("clusterTableDetectionTestParams")
    void testClusterTableDetection(String filename, int[][] numCells, boolean checkRoot) throws IOException {
        INode root = JsonToPdfTree.getPdfTreeRoot("/files/" + filename);
        ITree tree = new SemanticTree(root);

        Consumer<INode> semanticTreeValidator = new SemanticTreePreprocessingConsumer();
        tree.forEach(semanticTreeValidator);

        Consumer<INode> paragraphValidator = new AccumulatedNodeConsumer();
        tree.forEach(paragraphValidator);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tree.forEach(tableFinder);

        List<INode> resultTables = tableFinder.getTables();

        if (checkRoot) {
            Assertions.assertEquals(SemanticType.TABLE, root.getSemanticType());
        }
        Assertions.assertEquals(numCells.length, resultTables.size());
        for (int i = 0; i < numCells.length; ++i) {
            INode table = resultTables.get(i);
            for (int j = 0; j < 2; ++j) {
                INode subTable = table.getChildren().get(j);
                Assertions.assertEquals(numCells[i][j], subTable.getChildren().size());
            }
        }
    }

}
