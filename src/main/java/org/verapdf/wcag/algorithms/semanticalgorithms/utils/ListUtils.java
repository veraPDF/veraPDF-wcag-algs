package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ListUtils {

	private static final double EPSILON = 0.0001;

	private static final Set<SemanticType> listSemanticTypes = new HashSet<>(Arrays.asList(
			SemanticType.LIST, SemanticType.LIST_ITEM,
			SemanticType.LIST_LABEL, SemanticType.LIST_BODY));

	static public boolean isListNode(INode node) {
		return listSemanticTypes.contains(node.getSemanticType());
	}

	public static boolean isList(Table table) {
		List<String> textLabels = new ArrayList<>(table.getRows().size());
		List<BoundingBox> imageLabels = new ArrayList<>(table.getRows().size());
		for (TableRow row : table.getRows()) {
			List<TableCell> cells = row.getCells();
			if (cells.size() != 2) {
				return false;
			}
			TableCell cell = cells.get(0);
			textLabels.add(cell.getString());
			if (cell.getContent().size() == 1 && cell.getContent().get(0).getType() == TableToken.TableTokenType.IMAGE) {
				imageLabels.add(cell.getContent().get(0).getBoundingBox());
			}
		}
		return imageLabels.isEmpty() ? ListLabelsUtils.isListLabels(textLabels) : isListImageLabels(imageLabels);
	}

	public static boolean isListImageLabels(List<BoundingBox> listLabels) {
		if (listLabels.size() < 2) {
			return false;
		}
		BoundingBox firstLabel = listLabels.get(0);
		double firstLabelHeight = firstLabel.getHeight();
		for (int i = 1; i < listLabels.size(); i++) {
			if (!areCloseNumbers(listLabels.get(i).getLeftX(), listLabels.get(0).getLeftX()) ||
			    !areCloseNumbers(listLabels.get(i).getRightX(), listLabels.get(0).getRightX()) ||
			    !areCloseNumbers(listLabels.get(i).getHeight(), firstLabelHeight)) {
				return false;
			}
		}
		return true;
	}

	private static boolean areCloseNumbers(double d1, double d2) {
		return Math.abs(d1 - d2) < EPSILON;
	}

}
