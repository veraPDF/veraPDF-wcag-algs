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
import org.verapdf.wcag.algorithms.entities.tables.Table;
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
                Arguments.of("NEG-fake-table.json", new int[][] {{4, 4}}, true),
                Arguments.of("NEG-bad-table.json", new int[][] {{4, 5}}, false),
                Arguments.of("NEG-bad-table2.json", new int[][] {{4, 5}}, false),
                Arguments.of("NEG-fake-list.json", new int[][] {{2, 4}}, true), // list should be distinguished from table
                Arguments.of("no-table.json", new int[][] {}, false),
                Arguments.of("NEG-floating-text-box.json", new int[][] {}, false),
                Arguments.of("2columns2.json", new int[][] {}, false),
                Arguments.of("2columns3.json", new int[][] {}, false),
                Arguments.of("NEG-bad-table3.json", new int[][] {{2, 4}}, true),
                Arguments.of("NEG-bad-table3-full.json", new int[][] {{2, 24}}, false), // should be 6 separated tables
                Arguments.of("real-table.json", new int[][] {{4, 5}, {4, 9}}, false),
                Arguments.of("fake-table1.json", new int[][] {}, false),
                Arguments.of("fake-table2.json", new int[][] {{4, 5}, {4, 9}}, false),
                Arguments.of("fake-table3.json", new int[][] {}, false)
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2} ) => {0}")
    @MethodSource("clusterTableDetectionTestParams")
    void testClusterTableDetection(String filename, int[][] checkSizes, boolean checkRoot) throws IOException {
        INode root = JsonToPdfTree.getPdfTreeRoot("/files/" + filename);
        ITree tree = new SemanticTree(root);

        Consumer<INode> semanticTreeValidator = new SemanticTreePreprocessingConsumer();
        tree.forEach(semanticTreeValidator);

        Consumer<INode> paragraphValidator = new AccumulatedNodeConsumer();
        tree.forEach(paragraphValidator);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tree.forEach(tableFinder);

        List<Table> resultTables = tableFinder.getTables();

        if (checkRoot) {
            Assertions.assertEquals(SemanticType.TABLE, root.getSemanticType());
        }
        Assertions.assertEquals(checkSizes.length, resultTables.size());

        for (int i = 0; i < checkSizes.length; ++i) {
            Assertions.assertEquals(checkSizes[i][0], resultTables.get(i).numberOfColumns());
            Assertions.assertEquals(checkSizes[i][1], resultTables.get(i).numberOfRows());
        }
    }

}
