package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class TableBordersCollection {

    private final List<SortedSet<TableBorder>> tableBorders;

    public TableBordersCollection() {
        tableBorders = new ArrayList<>();
    }

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

    public SortedSet<TableBorder> getTableBorders(Integer pageNumber) {
        if (pageNumber != null && pageNumber < tableBorders.size()) {
            return tableBorders.get(pageNumber);
        }
        return new TreeSet<>();
    }

    public TableBorder getTableBorder(BoundingBox boundingBox) {
        SortedSet<TableBorder> tableBorders = getTableBorders(boundingBox.getPageNumber());
        for (TableBorder tableBorder : tableBorders) {
            if (tableBorder.getBoundingBox().contains(boundingBox, TableBorder.TABLE_BORDER_EPSILON,
                    TableBorder.TABLE_BORDER_EPSILON)) {
                return tableBorder;
            }
        }
        return null;
    }
}
