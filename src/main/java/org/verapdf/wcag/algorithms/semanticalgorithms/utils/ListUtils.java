package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticList;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableCell;
import org.verapdf.wcag.algorithms.entities.tables.TableRow;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.*;

import static org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils.TABLE_PROBABILITY_THRESHOLD;

public class ListUtils {

	public static final double LIST_LABEL_HEIGHT_EPSILON = 2.5;

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

	public static void updateTreeWithRecognizedLists(INode node, Set<ListInterval> listIntervals) {
		for (ListInterval listInterval : listIntervals) {
			updateTreeWithRecognizedList(node, listInterval);
		}
	}

	public static void updateTreeWithRecognizedList(INode node, ListInterval listInterval) {
		Long listId = StaticContainers.getNextID();
		List<INode> children = node.getChildren();
		for (Integer index : listInterval.getListItemsIndexes()) {
			updateTreeWithRecognizedListItem(children.get(index), listId);
		}
		if (node.getRecognizedStructureId() == null) {
			double probability = ((double) (listInterval.getNumberOfListItemsAndLists())) / node.getChildren().size();
			if (probability >= TABLE_PROBABILITY_THRESHOLD) {
				INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
				StaticContainers.getAccumulatedNodeMapper().updateNode(node,
						new SemanticList((SemanticTextNode)accumulatedNode, listInterval), probability, SemanticType.LIST);
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
	                                                         List<? extends ListItemInfo> itemsInfo) {
		ListIntervalsCollection listIntervalsCollection = new ListIntervalsCollection();
		for (ListInterval listInterval : listIntervals) {
			int start = listInterval.getListItemsStart();
			List<Integer> listItemsIndexes = new ArrayList<>(Arrays.asList(start));
			INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(children.get(start));
			double right = accumulatedChild.getRightX();
			int lastChildNumberOFColumns;
			int numberOfColumns = getInitialListColumnsNumber(accumulatedChild);
			int previousIndex = start;
			for (int i : listInterval.getListItemsIndexes().subList(1, listInterval.getListItemsIndexes().size())) {
				lastChildNumberOFColumns = 0;
				if (accumulatedChild instanceof SemanticTextNode) {
					SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
					if (textNode.getColumnsNumber() > 1) {
						right = textNode.getPenultColumn().getRightX();
					}
					lastChildNumberOFColumns = Math.max(textNode.getColumnsNumber() - 2, 0);
				}
				InfoChunk line1 = itemsInfo.get(previousIndex).getListItemValue();
				InfoChunk line2 = itemsInfo.get(i).getListItemValue();
				accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(children.get(i));
				if (line1.getPageNumber() + 1 < line2.getPageNumber()) {
					updateListIntervalCollection(listIntervalsCollection, listInterval, listItemsIndexes, numberOfColumns);

					right = -Double.MAX_VALUE;
					numberOfColumns = getInitialListColumnsNumber(accumulatedChild);
					listItemsIndexes = new ArrayList<>(Arrays.asList(i));
					previousIndex = i;
					continue;
				}
				if (Objects.equals(line1.getPageNumber(), line2.getPageNumber())) {
					if (!isOneColumn(line1, line2) && right >= accumulatedChild.getLeftX()) {
						updateListIntervalCollection(listIntervalsCollection, listInterval, listItemsIndexes, numberOfColumns);

						right = -Double.MAX_VALUE;
						numberOfColumns = getInitialListColumnsNumber(accumulatedChild);
						listItemsIndexes = new ArrayList<>(Arrays.asList(i));
					} else {
						listItemsIndexes.add(i);
					}
					if (!isOneColumn(line1, line2)) {
						numberOfColumns += lastChildNumberOFColumns + 1;
					}
					right = Math.max(right, accumulatedChild.getRightX());
				} else {
					numberOfColumns++;
					right = accumulatedChild.getRightX();
					listItemsIndexes.add(i);
				}
				previousIndex = i;
			}
			updateListIntervalCollection(listIntervalsCollection, listInterval, listItemsIndexes, numberOfColumns);
		}
		return listIntervalsCollection.getSet();
	}

	private static void updateListIntervalCollection(ListIntervalsCollection listIntervalsCollection, ListInterval listInterval,
	                                                 List<Integer> listItemsIndexes, int numberOfColumns) {
		if (listItemsIndexes.size() > 1) {
			listIntervalsCollection.put(new ListInterval(listItemsIndexes, listInterval
					.getListsIndexesContainedInListItemsIndexes(listItemsIndexes), numberOfColumns));
		}
	}

	private static int getInitialListColumnsNumber(INode node) {
		return (node instanceof SemanticTextNode) ? Math.max(((SemanticTextNode)node).getColumnsNumber() - 1, 1) : 1;
	}

	private static boolean isOneColumn(InfoChunk line1, InfoChunk line2) {
		return NodeUtils.areCloseNumbers(line1.getLeftX(), line2.getLeftX(), line1.getBoundingBox().getHeight() / 2);
	}
}
