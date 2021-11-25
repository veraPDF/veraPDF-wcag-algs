package org.verapdf.wcag.algorithms.entities.tables;

import java.util.List;
import java.util.ArrayList;

public class TableBordersCollection {

    private final List<List<TableBorder>> tableBorders;

    public TableBordersCollection(List<List<TableBorderBuilder>> tableBorderBuilders) {
        tableBorders = new ArrayList<>(tableBorderBuilders.size());
        for (List<TableBorderBuilder> builders : tableBorderBuilders) {
            List<TableBorder> borders = new ArrayList<>();
            for (TableBorderBuilder builder : builders) {
                TableBorder border = new TableBorder(builder);
                if (!border.isBadTable()) {
                    borders.add(border);
                }
            }
            tableBorders.add(borders);
        }
    }

    public List<List<TableBorder>> getTableBorders() {
        return tableBorders;
    }
}
