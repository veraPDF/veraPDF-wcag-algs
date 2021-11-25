package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.tables.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class TableBordersTests {

    static Stream<Arguments> clusterTableBorderDetectionTestParams() {
        return Stream.of(
                Arguments.of("NEG-fake-table", new int[][] {{10}}, new int[][] {{4}}, new int[][] {{4}}),
                Arguments.of("NEG-floating-text-box", new int[][] {{6}}, new int[][] {{}}, new int[][] {{}}),
                Arguments.of("PDFUA-Ref-2-02_Invoice", new int[][] {{}}, new int[][] {{}}, new int[][] {{}}),
                Arguments.of("PDFUA-Ref-2-05_BookChapter-german",
                        new int[][] {{},{},{},{},{5},{},{62},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},//check
                        new int[][] {{},{},{},{},{},{},{6},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},
                        new int[][] {{},{},{},{},{},{},{5},{},{},{},{},{},{},{},{},{},{},{},{},{},{}}),
                Arguments.of("PDFUA-Ref-2-06_Brochure", new int[][] {{9,10},{}}, new int[][] {{},{}}, new int[][] {{},{}}),
                Arguments.of("tableBorders/table_word", new int[][] {{38}}, new int[][] {{4}}, new int[][] {{3}}),
                Arguments.of("tableBorders/table_libre", new int[][] {{8}}, new int[][] {{3}}, new int[][] {{3}})
        );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2}, {3}) => {0}")
    @MethodSource("clusterTableBorderDetectionTestParams")
    void testTableBorderDetection(String filename, int[][] list, int[][] listN, int[][] listM) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/tables/" + filename + ".json");
        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        List<List<TableBorderBuilder>> tableBorderBuilders = linesPreprocessingConsumer.getTableBorders();
        Assertions.assertEquals(list.length, tableBorderBuilders.size());
        for (int pageNumber = 0; pageNumber < tableBorderBuilders.size(); pageNumber++) {
            List<TableBorderBuilder> borders = tableBorderBuilders.get(pageNumber);
            Assertions.assertEquals(list[pageNumber].length, borders.size());
            for (int i = 0; i < borders.size(); i++) {
                TableBorderBuilder border = borders.get(i);
                Assertions.assertEquals(list[pageNumber][i], border.getHorizontalLinesNumber() +
                        border.getVerticalLinesNumber());
            }
        }
        TableBordersCollection tableBordersCollection = new TableBordersCollection(tableBorderBuilders);
        List<List<TableBorder>> tableBorders = tableBordersCollection.getTableBorders();
        Assertions.assertEquals(listN.length, tableBorders.size());
        for (int pageNumber = 0; pageNumber < tableBorders.size(); pageNumber++) {
            List<TableBorder> borders = tableBorders.get(pageNumber);
            Assertions.assertEquals(listN[pageNumber].length, borders.size());
            for (int i = 0; i < borders.size(); i++) {
                TableBorder border = borders.get(i);
                Assertions.assertEquals(listN[pageNumber][i], border.getN());
                Assertions.assertEquals(listM[pageNumber][i], border.getM());
            }
        }
    }
}
