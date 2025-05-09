package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.geometry.Vertex;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import java.util.*;

public class LinesPreprocessingConsumer extends WCAGConsumer {

    private static final double MAX_LINE_WIDTH = 5.0;

    private List<List<TableBorderBuilder>> tableBorders;

    public List<List<TableBorderBuilder>> getTableBorders() {
        if (tableBorders == null) {
            findTableBorders();
        }
        return tableBorders;
    }

    public void findTableBorders() {
        tableBorders = new LinkedList<>();
        for (int pageNumber = 0; pageNumber < StaticContainers.getDocument().getNumberOfPages(); pageNumber++) {
            tableBorders.add(findTableBorders(pageNumber));
        }
    }

    private List<TableBorderBuilder> findTableBorders(Integer pageNumber) {
        List<TableBorderBuilder> tableBorders = new ArrayList<>();
        Set<LineChunk> set = new HashSet<>(StaticContainers.getLinesCollection().getHorizontalLines(pageNumber));
        set.addAll(StaticContainers.getLinesCollection().getVerticalLines(pageNumber));
        set.addAll(StaticContainers.getLinesCollection().getSquares(pageNumber));
        for (LineChunk line : set) {
            if (line.getWidth() > MAX_LINE_WIDTH) {
                continue;
            }
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
        for (int i = 0; i < tableBorders.size();) {
            TableBorderBuilder border = tableBorders.get(i);
            if ((border.getHorizontalLinesNumber() <= 2 && border.getVerticalLinesNumber() <= 1) ||
                    (border.getHorizontalLinesNumber() <= 1 && border.getVerticalLinesNumber() <= 2)) {
                tableBorders.remove(i);
            } else {
                i++;
            }
        }
        for (TableBorderBuilder border : tableBorders) {
            StaticContainers.getLinesCollection().getVerticalLines(pageNumber).removeAll(border.getVerticalLines());
            StaticContainers.getLinesCollection().getHorizontalLines(pageNumber).removeAll(border.getHorizontalLines());
        }
        return tableBorders;
    }

    private void mergeTableBorders(List<TableBorderBuilder> tableBorders) {
        for (int i = tableBorders.size() - 2; i >= 0; i--) {
            TableBorderBuilder border = tableBorders.get(i);
            List<Integer> indexes = new LinkedList<>();
            for (int j = tableBorders.size() - 1; j > i; j--) {
                TableBorderBuilder border2 = tableBorders.get(j);
                if (border.isConnectedBorder(border2)) {
                    indexes.add(j);
                }
            }
            for (Integer index : indexes) {
                border.mergeBorder(tableBorders.get(index));
                tableBorders.remove((int)index);
            }
        }
    }

    @Override
    public WCAGProgressStatus getWCAGProgressStatus() {
        return WCAGProgressStatus.LINES_PREPROCESSING;
    }
}
