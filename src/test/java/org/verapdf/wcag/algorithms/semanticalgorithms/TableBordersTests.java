package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class TableBordersTests {

    static Stream<Arguments> clusterTableBorderDetectionTestParams() {
        return Stream.of(
                Arguments.of("table_word", new int[] {38}),
                Arguments.of("table_libre", new int[] {8})
        );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
    @MethodSource("clusterTableBorderDetectionTestParams")
    void testTableBorderDetection(String filename, int[] list) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/tables/tableBorders/" + filename + ".json");
        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        List<TableBorderBuilder> tableBorderBuilders = linesPreprocessingConsumer.findTableBorders();
        Assertions.assertEquals(list.length, tableBorderBuilders.size());
        for (int i = 0; i < tableBorderBuilders.size(); i++) {
            TableBorderBuilder border = tableBorderBuilders.get(i);
            Assertions.assertEquals(list[i],
                    border.getHorizontalLinesNumber() + border.getVerticalLinesNumber());
        }
    }
}
