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
import java.util.SortedSet;
import java.util.stream.Stream;

public class TableBordersTests {

    static Stream<Arguments> clusterTableBorderDetectionTestParams() {
        return Stream.of(
                Arguments.of("NEG-fake-table", new int[][] {{10}}, new int[][] {{4}}, new int[][] {{4}}),
                Arguments.of("NEG-floating-text-box", new int[][] {{6}}, new int[][] {{}}, new int[][] {{}}),
                Arguments.of("PDFUA-Ref-2-02_Invoice", new int[][] {{}}, new int[][] {{}}, new int[][] {{}}),
                Arguments.of("PDFUA-Reference-01_(Matterhorn-Protocol_1-02)",
                        new int[][] {{},{62},{53},{173},{113,93,53,93,113},{73,73,193,53},{173,53,173,173},
                                {133,93,93,73,113},{93,53,53,53,53,53,73,53},{393},{53,73,233},{333},{133}},
                        new int[][] {{},{5},{6},{8},{5,4,2,4,5},{3,3,9,2},{8,2,8,8},{6,4,4,3,5},{4,2,2,2,2,2,3,2},
                                {19},{2,3,11},{16},{6}},
                        new int[][] {{},{3},{2},{6},{6,6,6,6,6},{6,6,6,6},{6,6,6,6},{6,6,6,6,6},{6,6,6,6,6,6,6,6},
                                {6},{6,6,6},{6},{6}}),
                Arguments.of("PDFUA-Ref-2-05_BookChapter-german",
                        new int[][] {{},{},{},{},{5},{},{62},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},//check
                        new int[][] {{},{},{},{},{},{},{6},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},
                        new int[][] {{},{},{},{},{},{},{5},{},{},{},{},{},{},{},{},{},{},{},{},{},{}}),
                Arguments.of("PDFUA-Ref-2-06_Brochure", new int[][] {{10,9},{}}, new int[][] {{},{}}, new int[][] {{},{}}),
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
            borders.sort(new TableBorderBuilder.TableBorderBuildersComparator());
            Assertions.assertEquals(list[pageNumber].length, borders.size());
            for (int i = 0; i < borders.size(); i++) {
                TableBorderBuilder border = borders.get(i);
                Assertions.assertEquals(list[pageNumber][i], border.getHorizontalLinesNumber() +
                        border.getVerticalLinesNumber());
            }
        }
        TableBordersCollection tableBordersCollection = new TableBordersCollection(tableBorderBuilders);
        List<SortedSet<TableBorder>> tableBorders = tableBordersCollection.getTableBorders();
        Assertions.assertEquals(listN.length, tableBorders.size());
        for (int pageNumber = 0; pageNumber < tableBorders.size(); pageNumber++) {
            SortedSet<TableBorder> borders = tableBorders.get(pageNumber);
            Assertions.assertEquals(listN[pageNumber].length, borders.size());
            int i = 0;
            for (TableBorder border : borders) {
                Assertions.assertEquals(listN[pageNumber][i], border.getNumberOfRows());
                Assertions.assertEquals(listM[pageNumber][i], border.getNumberOfColumns());
                i++;
            }
        }
    }
}
