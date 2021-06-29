package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils.isListLabels;

public class ListUtils {

	private static final Set<SemanticType> listSemanticTypes = new HashSet<>(Arrays.asList(
			SemanticType.LIST, SemanticType.LIST_ITEM,
			SemanticType.LIST_LABEL, SemanticType.LIST_BODY));

	static public boolean isListNode(INode node) {
		return listSemanticTypes.contains(node.getSemanticType());
	}

	public static boolean isList(Table table) {
		List<String> labels = new ArrayList<>(table.getRows().size());
		for (TableRow row : table.getRows()) {
			List<TableCell> cells = row.getCells();
			if (cells.size() != 2) {
				return false;
			}
			TableCell cell = cells.get(0);
			labels.add(cell.getString());
		}
		return isListLabels(labels);
	}

}
