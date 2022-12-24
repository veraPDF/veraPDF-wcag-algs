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

    public PDFList(Long id) {
        super();
        setRecognizedStructureId(id);
        listItems = new ArrayList<>();
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

    public void add(int index, ListItem listItem) {
        listItems.add(index, listItem);
        getBoundingBox().union(listItem.getBoundingBox());
    }

    private void createListItemsFromTableRows(List<TableRow> tableRows) {
        ListItem lastListItem = null;
//        Double maxGap = null;
        TableCell previousFirstCell = null;
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
                if (firstCell.getContent().isEmpty() && lastListItem.getLabel().getRightX() >
                        row.getCells().get(1).getLeftX()) {
                    return;
                }
                addContentToPreviousListItem(listItems.size() > 1 ? listItems.get(listItems.size() - 2) : null, row.getCells().get(1),
                        previousFirstCell);
                lastListItem.add(row);
            } else {
                addContentToPreviousListItem(lastListItem, row.getCells().get(1), firstCell);
                lastListItem = new ListItem(row);
                add(lastListItem);
                previousFirstCell = firstCell;
            }
        }
    }

    private void addContentToPreviousListItem(ListItem previousListItem, TableCell secondCell, TableCell firstCell) {
        if (previousListItem == null || firstCell == null) {
            return;
        }
        if (firstCell.getBoundingBox().getTopY() >= secondCell.getBoundingBox().getTopY()) {
            return;
        }
        for (TableTokenRow tokenRow : secondCell.getContent()) {
            if (tokenRow.getBoundingBox().getTopY() > firstCell.getBoundingBox().getTopY()) {
                previousListItem.getBody().add(tokenRow);
            }
        }
        secondCell.getContent().removeAll(previousListItem.getBody().getContent());
    }

}
