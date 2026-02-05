/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderCell;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderRow;

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
    
    public void clear() {
        for (SortedSet<TableBorder> tables : tableBorders) {
            tables.clear();
        }
    }

    public void clearContentsOfTable() {
        for (SortedSet<TableBorder> tables : tableBorders) {
            for (TableBorder table : tables) {
                for (TableBorderRow row : table.getRows()) {
                    for (TableBorderCell cell : row.getCells()) {
                        cell.getContents().clear();
                    }
                }
            }
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
            if (boundingBox.getIntersectionPercent(tableBorder.getBoundingBox()) > TableBorder.MIN_CELL_CONTENT_INTERSECTION_PERCENT) {
                return tableBorder;
            }
        }
        return null;
    }
    
    public void removeTableBorder(TableBorder tableBorder, int pageNumber) {
        tableBorders.get(pageNumber).remove(tableBorder);
    }
}
