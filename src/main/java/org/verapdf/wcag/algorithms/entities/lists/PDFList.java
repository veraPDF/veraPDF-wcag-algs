package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.tables.*;
//import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.ArrayList;
import java.util.List;

public class PDFList extends InfoChunk {

    private final List<ListItem> listItems;

    public PDFList(Table table) {
        super(table.getBoundingBox());
        setRecognizedStructureId(table.getId());
        listItems = new ArrayList<>();
        createListItemsFromTableRows(table.getRows());
    }

    public int getNumberOfListItems() {
        return listItems.size();
    }

    public List<ListItem> getListItems() {
        return listItems;
    }

    public void add(ListItem listItem) {
        listItems.add(listItem);
        getBoundingBox().union(listItem.getBoundingBox());
    }

    private void createListItemsFromTableRows(List<TableRow> tableRows) {
        ListItem lastListItem = null;
//        Double maxGap = null;
        for (TableRow row : tableRows) {
            TableCell firstCell = row.getCells().get(0);
            if (firstCell.getString().trim().isEmpty() && (firstCell.getContent().isEmpty() ||
                    firstCell.getContent().get(0).getType() == TableToken.TableTokenType.TEXT) && lastListItem != null) {
//                double gap = TableUtils.getRowGapFactor(lastListItem, row);
//                if (maxGap == null) {
//                    maxGap = TableUtils.NEXT_LINE_MAX_TOLERANCE_FACTOR * gap;
//                } else if (gap < maxGap) {
//                    maxGap = Math.max(TableUtils.NEXT_LINE_MAX_TOLERANCE_FACTOR * gap, maxGap);
//                } else {
//                    break;
//                }
                lastListItem.add(row);
            } else {
                TableCell secondCell = row.getCells().get(1);
                if (lastListItem != null && firstCell.getBoundingBox().getTopY() < secondCell.getBoundingBox().getTopY()) {
                    for (TableTokenRow tokenRow : secondCell.getContent()) {
                        if (tokenRow.getBoundingBox().getBottomY() > firstCell.getBoundingBox().getTopY()) {
                            lastListItem.getBody().add(tokenRow);
                        } else {
                            break;
                        }
                    }
                    secondCell.getContent().removeAll(lastListItem.getBody().getContent());
                }
                lastListItem = new ListItem(row);
                add(lastListItem);
            }
        }
    }

}
