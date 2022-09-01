package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemImageInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemLineArtInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListDetectionConsumer implements Consumer<INode> {

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
        List<ListItemTextInfo> textChildrenInfo = new ArrayList<>(Collections.nCopies(childrenSize, null));
        List<ListItemInfo> imageChildrenInfo = new ArrayList<>(Collections.nCopies(childrenSize, null));
        List<ListItemInfo> lineArtChildrenInfo = new ArrayList<>(Collections.nCopies(childrenSize, null));
        for (INode child : node.getChildren()) {
            if (child != null) {
                if (SemanticType.LIST.equals(child.getSemanticType()) && child.getChildren().stream()
                        .filter(i -> SemanticType.LIST_ITEM
                                .equals(i.getSemanticType()))
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
                textChildrenInfo.add(child.getIndex(), new ListItemTextInfo(child.getIndex(), child.getSemanticType(),
                        line, line.getValue().trim()));
                INode newChild = child;
                while (!newChild.getChildren().isEmpty()) {
                    newChild = newChild.getChildren().get(0);
                }
                if (newChild instanceof SemanticImageNode) {
                    ImageChunk image = ((SemanticImageNode) newChild).getImage();
                    if (image.getRightX() <= line.getLeftX() && image.getBoundingBox().getHeight() <
                            ListUtils.LIST_LABEL_HEIGHT_EPSILON * line.getBoundingBox().getHeight()) {
                        imageChildrenInfo.add(child.getIndex(), new ListItemImageInfo(child.getIndex(),
                                child.getSemanticType(), image));
                    }
                } else if (newChild instanceof SemanticFigure) {
                    LineArtChunk lineArt = ((SemanticFigure) newChild).getLineArt();
                    if (lineArt.getRightX() <= line.getLeftX() && lineArt.getBoundingBox().getHeight() <
                            ListUtils.LIST_LABEL_HEIGHT_EPSILON * line.getBoundingBox().getHeight()) {
                        lineArtChildrenInfo.add(child.getIndex(), new ListItemLineArtInfo(child.getIndex(),
                                child.getSemanticType(), lineArt));
                    }
                }
            }
        }
        List<ListItemTextInfo> textInfo = textChildrenInfo.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (!updateTreeWithOneElementList(node, textInfo) && textInfo.size() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getListItemsIntervals(textInfo), node.getChildren(),  textChildrenInfo));
        }
        if (imageChildrenInfo.stream().filter(Objects::nonNull).count() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getImageListItemsIntervals(imageChildrenInfo), node.getChildren(), imageChildrenInfo));
        }
        if (lineArtChildrenInfo.stream().filter(Objects::nonNull).count() > 1) {
            ListUtils.updateTreeWithRecognizedLists(node, ListUtils.getChildrenListIntervals(
                    ListLabelsUtils.getImageListItemsIntervals(lineArtChildrenInfo), node.getChildren(), lineArtChildrenInfo));
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
                List<Integer> listItemsIndexes = new ArrayList<>(Collections.singletonList(originalIndex));
                ListUtils.updateTreeWithRecognizedList(node, new ListInterval(listItemsIndexes,
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
}
