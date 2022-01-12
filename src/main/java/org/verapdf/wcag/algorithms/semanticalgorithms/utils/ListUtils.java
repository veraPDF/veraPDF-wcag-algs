package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticList;
import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.*;

import static org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils.TABLE_PROBABILITY_THRESHOLD;

public class ListUtils {

	private static final Set<SemanticType> listSemanticTypes = new HashSet<>(Arrays.asList(
			SemanticType.LIST, SemanticType.LIST_ITEM,
			SemanticType.LIST_LABEL, SemanticType.LIST_BODY));

	static public boolean isListNode(INode node) {
		return listSemanticTypes.contains(node.getSemanticType());
	}

	static public boolean isInitialListNode(INode node) {
		return listSemanticTypes.contains(node.getInitialSemanticType());
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
			if (cell.isTextCell()) {
				textLabels.add(cell.getString());
			} else if (cell.getContent().size() == 1 && cell.getContent().get(0).getType() == TableToken.TableTokenType.IMAGE) {
				imageLabels.add(cell.getContent().get(0).getBoundingBox());
			} else {
				return false;
			}
		}
		if (!imageLabels.isEmpty() && !textLabels.isEmpty()) {
			return false;
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
			if (!NodeUtils.areCloseNumbers(listLabels.get(i).getLeftX(), listLabels.get(0).getLeftX()) ||
			    !NodeUtils.areCloseNumbers(listLabels.get(i).getRightX(), listLabels.get(0).getRightX()) ||
			    !NodeUtils.areCloseNumbers(listLabels.get(i).getHeight(), firstLabelHeight)) {
				return false;
			}
		}
		return true;
	}

	public static void updateTreeWithRecognizedLists(INode node, List<INode> children, Set<ListInterval> listIntervals) {
		for (ListInterval listInterval : listIntervals) {
			updateTreeWithRecognizedList(node, children, listInterval);
		}
	}

	public static void updateTreeWithRecognizedList(INode node, List<INode> children, ListInterval listInterval) {
		Long listId = Table.getNextTableListId();
		for (int i = listInterval.start; i <= listInterval.end; i++) {
			updateTreeWithRecognizedListItem(children.get(i), listId);
		}
		if (node.getRecognizedStructureId() == null) {
			double probability = ((double) (listInterval.end - listInterval.start + 1)) / children.size();
			if (probability >= TABLE_PROBABILITY_THRESHOLD) {
				StaticContainers.getAccumulatedNodeMapper().updateNode(node, new SemanticList(node), probability, SemanticType.LIST);
				node.setRecognizedStructureId(listId);
			}
		}
	}

	public static void updateTreeWithRecognizedListItem(INode item, Long listId) {
		item.setSemanticType(SemanticType.LIST_ITEM);
		item.setCorrectSemanticScore(1.0);
		item.setRecognizedStructureId(listId);
		List<INode> children = item.getChildren();
		if (children.size() == 1) {
			INode child = children.get(0);
			child.setSemanticType(SemanticType.LIST_BODY);
			child.setCorrectSemanticScore(1.0);
			child.setRecognizedStructureId(listId);
		} else if (children.size() > 1) {
			INode child = children.get(0);
			child.setSemanticType(SemanticType.LIST_LABEL);
			child.setRecognizedStructureId(listId);
			for (int i = 1; i < children.size(); i++) {
				child = children.get(i);
				child.setSemanticType(SemanticType.LIST_BODY);
				child.setCorrectSemanticScore(1.0);
				child.setRecognizedStructureId(listId);
			}
		}
	}

	public static Set<ListInterval> getChildrenListIntervals(Set<ListInterval> listIntervals, List<INode> children,
															  List<? extends InfoChunk> childrenFirstLines) {
		ListIntervalsCollection listIntervalsCollection = new ListIntervalsCollection();
		for (ListInterval listInterval : listIntervals) {
			double right = -Double.MAX_VALUE;
			int start = listInterval.start;
			for (int i = listInterval.start + 1; i <= listInterval.end; i++) {
				InfoChunk line1 = childrenFirstLines.get(i - 1);
				InfoChunk line2 = childrenFirstLines.get(i);
				if (line1.getPageNumber() + 1 < line2.getPageNumber()) {
					start = listInterval.end;
					break;
				}
				INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(children.get(i));
				if (Objects.equals(line1.getPageNumber(), line2.getPageNumber())) {
					if (!NodeUtils.areCloseNumbers(line1.getLeftX(), line2.getLeftX(),
							line1.getBoundingBox().getHeight() / 2) && right >= accumulatedChild.getLeftX()) {
						if (start < i - 1) {
							listIntervalsCollection.put(new ListInterval(start, i - 1));
						}
						start = i;
						right = -Double.MAX_VALUE;
					}
					right = Math.max(right, accumulatedChild.getRightX());
				} else {
					right = accumulatedChild.getRightX();
				}
			}
			if (start < listInterval.end) {
				listIntervalsCollection.put(new ListInterval(start, listInterval.end));
			}
		}
		return listIntervalsCollection.getSet();
	}
}
