package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.entities.lists.ListItem;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemImageInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemLineArtInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ErrorCodes;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListDetectionConsumer extends WCAGConsumer implements Consumer<INode> {

    @Override
    public void accept(INode node) {
        boolean isLeafChild  = node.getChildren()
                .stream()
                .allMatch(child -> ((child instanceof SemanticSpan) ||
                        (child instanceof SemanticImageNode) ||
                        (child instanceof SemanticFigure) ||
                        child.getSemanticType() == null));
        if (isLeafChild) {
            return;
        }
        acceptSemanticList(node);
        if (node.getSemanticType() != SemanticType.LIST) {
            checkNeighborLists(node);
        }
        checkListItem(node);
        checkListInsideList(node);
    }

    private void checkListInsideList(INode node) {
        if (node.getInitialSemanticType() == SemanticType.LIST && node.getChildren().size() == 1 &&
                node.getChildren().get(0).getSemanticType() == SemanticType.LIST) {
            ErrorCodes.addErrorCodeWithArguments(node, ErrorCodes.ERROR_CODE_1201);
            node.setSemanticType(SemanticType.LIST);
        }
    }

    private void acceptSemanticList(INode node) {
        if (node.getSemanticType() == SemanticType.TABLE_OF_CONTENT) {
            return;
        }
        INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
        TableBorder tableBorder = StaticContainers.getTableBordersCollection().getTableBorder(node.getBoundingBox());
        if (accumulatedNode != null && tableBorder != null &&
                tableBorder.getTableBorderCell(accumulatedNode.getBoundingBox()) == null) {
            return;
        }
        int childrenSize = node.getChildren().size();
        List<ListItemTextInfo> textChildrenInfo = new ArrayList<>(childrenSize);
        List<ListItemImageInfo> imageChildrenInfo = new ArrayList<>(childrenSize);
        List<ListItemLineArtInfo> lineArtChildrenInfo = new ArrayList<>(childrenSize);
        for (INode child : node.getChildren()) {
            if (child == null) {
                continue;
            }
            if (SemanticType.LIST.equals(child.getSemanticType()) && child.getChildren().stream()
                    .filter(i -> SemanticType.LIST_ITEM.equals(i.getSemanticType()))
                    .count() > 1) {
                continue;
            }
            INode accumulatedChild = StaticContainers.getAccumulatedNodeMapper().get(child);
            if (!(accumulatedChild instanceof SemanticTextNode)) {
                continue;
            }
            SemanticTextNode textNode = (SemanticTextNode)accumulatedChild;
            if (textNode.isSpaceNode() || textNode.isEmpty()) {
                continue;
            }
            TextLine line = textNode.getFirstNonSpaceLine();
            textChildrenInfo.add(new ListItemTextInfo(child.getIndex(), child.getSemanticType(),
                    line, line.getValue().trim()));
            INode newChild = child;
            while (!newChild.getChildren().isEmpty()) {
                newChild = newChild.getChildren().get(0);
            }
            if (newChild instanceof SemanticImageNode) {
                ImageChunk image = ((SemanticImageNode) newChild).getImage();
                if (image.getRightX() <= line.getLeftX() && image.getBoundingBox().getHeight() <
                        ListUtils.LIST_LABEL_HEIGHT_EPSILON * line.getBoundingBox().getHeight()) {
                    imageChildrenInfo.add(new ListItemImageInfo(child.getIndex(),
                            child.getSemanticType(), image));
                }
            } else if (newChild instanceof SemanticFigure) {
                LineArtChunk lineArt = ((SemanticFigure) newChild).getLineArt();
                if (lineArt.getRightX() <= line.getLeftX() && lineArt.getBoundingBox().getHeight() <
                        ListUtils.LIST_LABEL_HEIGHT_EPSILON * line.getBoundingBox().getHeight()) {
                    lineArtChildrenInfo.add(new ListItemLineArtInfo(child.getIndex(),
                            child.getSemanticType(), lineArt));
                }
            }
        }
        if (!updateTreeWithOneElementList(node, textChildrenInfo) && textChildrenInfo.size() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getListItemsIntervals(textChildrenInfo), node.getChildren()));
        }
        if (imageChildrenInfo.size() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getImageListItemsIntervals(imageChildrenInfo), node.getChildren()));
        }
        if (lineArtChildrenInfo.size() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getImageListItemsIntervals(lineArtChildrenInfo), node.getChildren()));
        }
    }

    private boolean updateTreeWithOneElementList(INode node, List<ListItemTextInfo> itemsInfo) {
        if (SemanticType.LIST.equals(node.getInitialSemanticType()) &&
                itemsInfo.size() == itemsInfo.stream()
                        .filter(i -> SemanticType.LIST.equals(i.getSemanticType()))
                        .count() + 1) {
            int index = IntStream.range(0, itemsInfo.size())
                    .filter(i -> !SemanticType.LIST.equals(itemsInfo.get(i).getSemanticType()))
                    .findFirst().orElse(0);
            if (ListLabelsUtils.isListLabel(itemsInfo.get(index).getListItem())) {
                int originalIndex = itemsInfo.get(index).getIndex();
                List<ListItemInfo> listItemsInfos = new ArrayList<>(Collections.singletonList(itemsInfo.get(index)));
                ListUtils.updateTreeWithRecognizedList(node, new ListInterval(listItemsInfos,
                        itemsInfo.stream()
                                .map(ListItemTextInfo::getIndex)
                                .filter(i -> i != originalIndex)
                                .collect(Collectors.toList()),
                        1));
            }
            return true;
        }
        return false;
    }

    public static boolean isTwoListItemsOnTwoPages(INode node) {
        if (node.getPageNumber() == null || node.getLastPageNumber() == null ||
                node.getPageNumber() + 1 != node.getLastPageNumber()) {
            return false;
        }
        INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
        if (!(accumulatedNode instanceof SemanticTextNode)) {
            return false;
        }
        SemanticTextNode textNode = (SemanticTextNode)accumulatedNode;
        if (textNode.isSpaceNode() || textNode.isEmpty()) {
            return false;
        }
        TextLine line = textNode.getFirstNonSpaceLine();
        if (!Objects.equals(node.getPageNumber(), line.getPageNumber())) {
            return false;
        }
        TextLine secondLine = textNode.getFirstNonSpaceLine(node.getPageNumber() + 1);
        List<ListItemTextInfo> textChildrenInfo = new ArrayList<>(2);
        textChildrenInfo.add(new ListItemTextInfo(0, node.getSemanticType(),
                line, line.getValue().trim()));
        textChildrenInfo.add(new ListItemTextInfo(1, node.getSemanticType(),
                secondLine, secondLine.getValue().trim()));
        Set<ListInterval> set = ListLabelsUtils.getListItemsIntervals(textChildrenInfo);
        if (set.size() != 1) {
            return false;
        }
        ListInterval interval = set.iterator().next();
        if (interval.getStart() != 0 || interval.getEnd() != 1) {
            return false;
        }
        return true;
    }

    private void checkNeighborLists(INode node) {
        INode previousChild = null;
        for (INode currentChild : node.getChildren()) {
            if (currentChild.getSemanticType() == SemanticType.LIST) {
                if (previousChild != null) {
                    INode accumulatedPreviousNode = StaticContainers.getAccumulatedNodeMapper().get(previousChild);
                    INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(currentChild);
                    if (accumulatedNode instanceof SemanticList && accumulatedPreviousNode instanceof SemanticList &&
                            checkNeighborLists(currentChild, previousChild, (SemanticList) accumulatedNode,
                                (SemanticList) accumulatedPreviousNode)) {
                        ErrorCodes.addErrorCodeWithArguments(currentChild, ErrorCodes.ERROR_CODE_1200);
                        ErrorCodes.addErrorCodeWithArguments(previousChild, ErrorCodes.ERROR_CODE_1200);
                        StaticContainers.getIdMapper().put(previousChild.getRecognizedStructureId(),
                                currentChild.getRecognizedStructureId());
                    }
                }
                previousChild = currentChild;
            } else {
                previousChild = null;
            }
        }
    }

    private boolean checkNeighborLists(INode currentNode, INode previousNode, SemanticList currentList, SemanticList previousList) {
        ListItemInfo currentInfo = currentList.getListInterval().getFirstListItemInfo();
        ListItemInfo previousInfo = previousList.getListInterval().getLastListItemInfo();
        if (!checkInfos(currentNode, previousNode, currentInfo, previousInfo)) {
            return false;
        }
        int targetListItemNumber;
        Set<ListInterval> listIntervals;
        if (previousInfo instanceof ListItemTextInfo && currentInfo instanceof ListItemTextInfo) {
            List<ListItemTextInfo> textChildrenInfo = getTextChildrenInfo(previousList, currentList);
            targetListItemNumber = textChildrenInfo.size();
            listIntervals = ListLabelsUtils.getListItemsIntervals(textChildrenInfo);
        } else if ((previousInfo instanceof ListItemImageInfo && currentInfo instanceof ListItemImageInfo) ||
                (previousInfo instanceof ListItemLineArtInfo && currentInfo instanceof ListItemLineArtInfo)) {
            List<ListItemInfo> childrenInfo = getImageChildrenInfo(previousList, currentList);
            targetListItemNumber = childrenInfo.size();
            listIntervals = ListLabelsUtils.getImageListItemsIntervals(childrenInfo);
        } else {
            return false;
        }
        if (listIntervals.size() != 1) {
            return false;
        }
        ListInterval interval = listIntervals.iterator().next();
        if (interval.getNumberOfListItems() != targetListItemNumber) {
            return false;
        }
        return checkListItemsPositions(currentNode, previousNode, currentList, previousList);
    }

    private boolean checkInfos(INode currentNode, INode previousNode, ListItemInfo currentInfo, ListItemInfo previousInfo) {
        for (int i = 0; i < currentInfo.getIndex(); i++) {
            if (currentNode.getChildren().get(i).getSemanticType() != SemanticType.LIST) {
                return false;
            }
        }
        for (int i = previousNode.getChildren().size() - 1; i > previousInfo.getIndex(); i--) {
            if (previousNode.getChildren().get(i).getSemanticType() != SemanticType.LIST) {
                return false;
            }
        }
        return true;
    }

    private List<ListItemTextInfo> getTextChildrenInfo(SemanticList previousList, SemanticList currentList) {
        List<ListItemTextInfo> textChildrenInfo = new ArrayList<>(4);
        if (previousList.getNumberOfListItems() != 1) {
            textChildrenInfo.add((ListItemTextInfo)previousList.getListInterval().getPenultListItemInfo());
        }
        textChildrenInfo.add((ListItemTextInfo)previousList.getListInterval().getLastListItemInfo());
        textChildrenInfo.add((ListItemTextInfo)currentList.getListInterval().getFirstListItemInfo());
        if (currentList.getNumberOfListItems() != 1) {
            textChildrenInfo.add((ListItemTextInfo)currentList.getListInterval().getSecondListItemInfo());
        }
        return textChildrenInfo;
    }

    private List<ListItemInfo> getImageChildrenInfo(SemanticList previousList, SemanticList currentList) {
        List<ListItemInfo> childrenInfo = new ArrayList<>(4);
        if (previousList.getNumberOfListItems() != 1) {
            childrenInfo.add(previousList.getListInterval().getPenultListItemInfo());
        }
        childrenInfo.add(previousList.getListInterval().getLastListItemInfo());
        childrenInfo.add(currentList.getListInterval().getFirstListItemInfo());
        if (currentList.getNumberOfListItems() != 1) {
            childrenInfo.add(currentList.getListInterval().getSecondListItemInfo());
        }
        return childrenInfo;
    }

    private boolean checkListItemsPositions(INode currentNode, INode previousNode,
                                            SemanticList currentList, SemanticList previousList) {
        List<ListItemInfo> listItemInfos = new ArrayList<>();
        ListItemInfo newInfo = ListItemInfo.createListItemInfo(previousList.getListInterval().getLastListItemInfo());
        newInfo.setIndex(0);
        listItemInfos.add(newInfo);
        newInfo = ListItemInfo.createListItemInfo(currentList.getListInterval().getFirstListItemInfo());
        newInfo.setIndex(1);
        listItemInfos.add(newInfo);
        List<INode> children = new ArrayList<>();
        children.add(previousNode.getChildren().get(previousNode.getChildren().size() - 1));
        children.add(currentNode.getChildren().get(0));
        ListIntervalsCollection listIntervalsCollection = new ListIntervalsCollection();
        ListUtils.checkChildrenListInterval(listIntervalsCollection, new ListInterval(listItemInfos,
                Collections.emptyList(), 1), children);
        Set<ListInterval> listIntervals = listIntervalsCollection.getSet();
        if (listIntervals.size() != 1) {
            return false;
        }
        ListInterval interval = listIntervals.iterator().next();
        if (interval.getNumberOfListItems() != listItemInfos.size()) {
            return false;
        }
        return true;
    }

    private static void checkListItem(INode node) {
        if (node.getInitialSemanticType() != SemanticType.LIST_ITEM) {
            return;
        }
        if (isTwoListItemsOnTwoPages(node)) {
            Long listId = StaticContainers.getNextID();
            PDFList list = new PDFList(listId);
            list.add(new ListItem(node.getBoundingBox().getBoundingBox(node.getPageNumber()), listId));
            list.add(new ListItem(node.getBoundingBox().getBoundingBox(node.getPageNumber() + 1), listId));
            StaticContainers.getListsCollection().add(list);
        }
    }

    public WCAGProgressStatus getWCAGProgressStatus() {
        return WCAGProgressStatus.LIST_DETECTION;
    }
}
