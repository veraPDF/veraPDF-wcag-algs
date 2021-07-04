package org.verapdf.wcag.algorithms.entities.lists;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;

import java.util.ArrayList;
import java.util.List;

public class PDFList extends InfoChunk {

    private final Long id;
    private final java.util.List<ListItem> listItems;

    public PDFList(Table table) {
        super(table.getBoundingBox());
        this.id = table.getId();
        listItems = new ArrayList<>();
        createListItemsFromTableRows(table.getRows());
    }

    public int getNumberOfListItems() {
        return listItems.size();
    }

    public java.util.List<ListItem> getListItems() {
        return listItems;
    }

    public Long getId() {
        return id;
    }

    public void add(ListItem listItem) {
        listItems.add(listItem);
        getBoundingBox().union(listItem.getBoundingBox());
    }

    private void createListItemsFromTableRows(List<TableRow> tableRows) {
        ListItem lastListItem = null;
        for (TableRow row : tableRows) {
            TableCell cell = row.getCells().get(0);
            if (cell.getString().trim().isEmpty() && lastListItem != null) {
                lastListItem.add(row);
            } else {
                lastListItem = new ListItem(row);
                add(lastListItem);
            }
        }
    }

}
