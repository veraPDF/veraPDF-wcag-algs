package org.verapdf.wcag.algorithms.entities.tables;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class TableBordersCollection {

    private final List<SortedSet<TableBorder>> tableBorders;

    public TableBordersCollection(List<List<TableBorderBuilder>> tableBorderBuilders) {
        tableBorders = new ArrayList<>(tableBorderBuilders.size());
        for (List<TableBorderBuilder> builders : tableBorderBuilders) {
            SortedSet<TableBorder> borders = new TreeSet<>(new TableBorder.TableBordersComparator());
            for (TableBorderBuilder builder : builders) {
                TableBorder border = new TableBorder(builder);
                if (!border.isBadTable()) {
                    borders.add(border);
                }
            }
            tableBorders.add(borders);
        }
    }

    public List<SortedSet<TableBorder>> getTableBorders() {
        return tableBorders;
    }
}
