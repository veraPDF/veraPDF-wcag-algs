package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.LinesCollection;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class LinesPreprocessingConsumer {

    private final IDocument document;
    private final LinesCollection linesCollection;

    public LinesPreprocessingConsumer(IDocument document) {
        this.document = document;
        this.linesCollection = new LinesCollection(document);
    }

    public LinesCollection getLinesCollection() {
        return linesCollection;
    }

    public List<TableBorderBuilder> findTableBorders() {
        List<TableBorderBuilder> tableBorders = new LinkedList<>();
        for (int pageNumber = 0; pageNumber < document.getPages().size(); pageNumber++) {
            tableBorders.addAll(findTableBorders(pageNumber));
        }
        return tableBorders;
    }

    private List<TableBorderBuilder> findTableBorders(Integer pageNumber) {
        List<TableBorderBuilder> tableBorders = new LinkedList<>();
        Set<LineChunk> set = new HashSet<>(linesCollection.getHorizontalLines(pageNumber));
        set.addAll(linesCollection.getVerticalLines(pageNumber));
        for (LineChunk line : set) {
            Vertex v1 = new Vertex(line.getBoundingBox().getPageNumber(), line.getStartX(), line.getStartY(),
                    0.5 * line.getWidth());
            Vertex v2 = new Vertex(line.getBoundingBox().getPageNumber(), line.getEndX(), line.getEndY(),
                    0.5 * line.getWidth());
            boolean separateTableBorder = true;
            for (TableBorderBuilder border : tableBorders) {
                boolean isCross = false;
                if (line.isHorizontalLine()) {
                    for (LineChunk verticalLine : border.getVerticalLines()) {
                        Vertex vv = LineChunk.getIntersectionVertex(line, verticalLine);
                        if (vv != null) {
                            border.addVertex(vv);
                            isCross = true;
                        }
                    }
                } else if (line.isVerticalLine()) {
                    for (LineChunk horizontalLine : border.getHorizontalLines()) {
                        Vertex vv = LineChunk.getIntersectionVertex(horizontalLine, line);
                        if (vv != null) {
                            border.addVertex(vv);
                            isCross = true;
                        }
                    }
                }
                if (isCross) {
                    border.addVertex(v1);
                    border.addVertex(v2);
                    border.addLine(line);
                    separateTableBorder = false;
                    break;
                }
            }
            if (separateTableBorder) {
                TableBorderBuilder border = new TableBorderBuilder(line);
                tableBorders.add(border);
            }
        }
        mergeTableBorders(tableBorders);
        for (int i = 0; i < tableBorders.size();) {
            TableBorderBuilder border = tableBorders.get(i);
            if (border.getVertexesNumber() <= 2 || border.getHorizontalLinesNumber() == 0 ||
                    border.getVerticalLinesNumber() == 0) {
                tableBorders.remove(i);
            } else {
                i++;
            }
        }
        for (TableBorderBuilder border : tableBorders) {
            for (LineChunk lineChunk : border.getVerticalLines()) {
                linesCollection.getVerticalLines(pageNumber).remove(lineChunk);
            }
            for (LineChunk lineChunk : border.getHorizontalLines()) {
                linesCollection.getHorizontalLines(pageNumber).remove(lineChunk);
            }
        }
        for (int i = 0; i < tableBorders.size();) {
            TableBorderBuilder border = tableBorders.get(i);
            if ((border.getHorizontalLinesNumber() <= 2 && border.getVerticalLinesNumber() <= 1) ||
                    (border.getHorizontalLinesNumber() <= 1 && border.getVerticalLinesNumber() <= 2)) {
                tableBorders.remove(i);
            } else {
                i++;
            }
        }
        return tableBorders;
    }

    private void mergeTableBorders(List<TableBorderBuilder> tableBorders) {
        for (int i = tableBorders.size() - 2; i >= 0; i--) {
            TableBorderBuilder border = tableBorders.get(i);
            for (int j = i + 1; j < tableBorders.size();) {
                TableBorderBuilder border2 = tableBorders.get(j);
                if (border.isConnectedBorder(border2)) {
                    border.mergeBorder(border2);
                    tableBorders.remove(j);
                } else {
                    j++;
                }
            }
        }
    }
}
