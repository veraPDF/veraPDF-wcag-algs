package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.tocs.TOCIInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TOCDetectionConsumer extends WCAGConsumer implements Consumer<INode> {

    private static final String LINK = "Link";
    private static final String SPACES = "\\s\u00A0\u2007\u202F";
    private static final String SPACES_REGEX = "[" + SPACES + "]+";
    private static final String SPACES_DOTS_SPACES_REGEX = "[" + SPACES + "]*\\.*[" + SPACES + "]*";
    public static final String NON_CONTENT_REGEX = "[" + SPACES + "\u2011\u2010:\\-]";
    private static final double MAX_RIGHT_ALIGNMENT_GAP = 0.1;
//    private static final double MAX_LEFT_ALIGNMENT_GAP = 0.1;
    private static final double LENGTH_HEADING_DIFFERENCE = 1.5;
    private final Map<Integer, INode> nodes = new HashMap<>();
    private INode currentNode;

//    private Double left = null;
    private Double right = null;
    private Double maxRight = -Double.MAX_VALUE;
    private Integer pagesGap = null;
    private Integer lastPageNumber = null;

    @Override
    public void accept(INode node) {
        currentNode = node;
        detectTOC(node);
        checkTOC(node);
        checkNeighborTOCs(node);
    }
    public void detectTOC(INode node) {
        if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
            return;
        }
        if (node.getChildren().size() < 2) {
            return;
        }
        List<TOCIInfo> infos = getTOCIInfos(node);
        List<Integer> tociIndexes = detectTOCIs(infos, node);
        if (tociIndexes.size() > 1) {
            Long id = StaticContainers.getNextID();
            for (int index : tociIndexes) {
                INode child = node.getChildren().get(index);
                child.setRecognizedStructureId(id);
                StaticContainers.getAccumulatedNodeMapper().updateNode(child,
                        StaticContainers.getAccumulatedNodeMapper().get(child), 1.0, SemanticType.TABLE_OF_CONTENT_ITEM);
            }
        }
    }

    public void checkTOC(INode node) {
        if (node.getInitialSemanticType() != SemanticType.TABLE_OF_CONTENT) {
            return;
        }
        List<TOCIInfo> infos = getTOCIInfos(node);
        checkTOCIsNumbering(node.getChildren(), infos);
//        List<Integer> tociIndexes = new ArrayList<>(node.getChildren().size());
//        left = null;
        right = null;
        maxRight = -Double.MAX_VALUE;
        pagesGap = null;
        lastPageNumber = null;
//        for (int index = 0; index < node.getChildren().size(); index++) {
//            INode child = node.getChildren().get(index);
//            TOCIInfo info = infos.get(index);
//            if (child.getInitialSemanticType() != SemanticType.TABLE_OF_CONTENT && checkTOCI(child, info)) {
//                tociIndexes.add(index);
//                findHeading(getNode(info.getDestinationPageNumber()), info.getTextForSearching(), info.getDestinationPageNumber());
//            }
//        }
        List<Integer> tociIndexes = checkTOCIs(infos, node.getChildren());
        Long id = StaticContainers.getNextID();
        for (INode child : node.getChildren()) {
            if (child.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                child.setRecognizedStructureId(id);
            }
        }
        for (int index : tociIndexes) {
            INode child = node.getChildren().get(index);
            if (child.getInitialSemanticType() != SemanticType.TABLE_OF_CONTENT_ITEM) {
                child.setRecognizedStructureId(id);
            }
            StaticContainers.getAccumulatedNodeMapper().updateNode(child,
                    StaticContainers.getAccumulatedNodeMapper().get(child), 1.0, SemanticType.TABLE_OF_CONTENT_ITEM);
        }
        if (tociIndexes.size() > 1 || (tociIndexes.size() == 1 && node.getChildren().size() == 1)) {
            node.setRecognizedStructureId(id);
            StaticContainers.getAccumulatedNodeMapper().updateNode(node,
                    StaticContainers.getAccumulatedNodeMapper().get(node), 1.0, SemanticType.TABLE_OF_CONTENT);
        }
    }

    private boolean checkTOCI(INode child, TOCIInfo tociInfo) {
        if (tociInfo.getText() == null) {
            ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1000);
            return false;
        }
        if (lastPageNumber != null && !lastPageNumber.equals(child.getPageNumber())) {
//            left = null;
            right = null;
            maxRight = -Double.MAX_VALUE;
        }
        lastPageNumber = child.getPageNumber();
        if (child.getLeftX() > maxRight) {
            //next column
//            left = null;
            right = null;
        }
        maxRight = Math.max(maxRight, tociInfo.getRight());
        if (tociInfo.getPageNumberLabel() != null) {
            if (right == null) {
                right = tociInfo.getRight();
            } else if (!NodeUtils.areCloseNumbers(right, tociInfo.getRight(),
                    MAX_RIGHT_ALIGNMENT_GAP * tociInfo.getMaxTextSize())) {
                ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1003);
                return false;
            }
        }
//        if (left == null) {
//            left = child.getLeftX();
//        } else if (!NodeUtils.areCloseNumbers(left, child.getLeftX(), MAX_LEFT_ALIGNMENT_GAP *
//                tociInfo.getMaxTextSize()) && left > child.getLeftX()) {
//            child.getErrorCodes().add(ErrorCodes.ERROR_CODE_1004);
//            return false;
//        }
        if (tociInfo.getDestinationPageNumber() == null) {
            ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1001);
            return false;
        }
        if (tociInfo.getPageNumberLabel() != null) {
            if (pagesGap == null) {
                pagesGap = tociInfo.getPageNumberLabel() - tociInfo.getDestinationPageNumber();
            } else if (tociInfo.getDestinationPageNumber() + pagesGap != tociInfo.getPageNumberLabel()) {
                ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1002);
                return false;
            }
        }
        if (!findText(tociInfo.getTextForSearching(), tociInfo.getDestinationPageNumber())) {
            ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1005);
            return false;
        }
        return true;
    }

    private List<TOCIInfo> getTOCIInfos(INode node) {
        List<TOCIInfo> infos = new ArrayList<>(node.getChildren().size());
        for (int index = 0; index < node.getChildren().size(); index++) {
            INode child = node.getChildren().get(index);
            if (child.getSemanticType() != SemanticType.TABLE_OF_CONTENT) {
                infos.add(getTOCIInfo(child));
            } else {
                infos.add(null);
            }
        }
        return infos;
    }

    public List<Integer> detectTOCIs(List<TOCIInfo> infos, INode node) {
        List<Integer> indexes = new ArrayList<>(infos.size());
        for (int index = 0; index < infos.size(); index++) {
            TOCIInfo tociInfo = infos.get(index);
            INode child = node.getChildren().get(index);
            if (tociInfo == null || child.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT ||
                    child.getSemanticType() == SemanticType.TABLE_OF_CONTENT ||
                    child.getInitialSemanticType() == SemanticType.LINK) {
                continue;
            }
            if (tociInfo.getText() == null || tociInfo.getText().isEmpty()) {
                continue;
            }
            if (tociInfo.getDestinationPageNumber() == null && (tociInfo.getPageNumberLabel() == null ||
                    tociInfo.getPageNumberLabel() > StaticContainers.getDocument().getNumberOfPages())) {
                continue;
            }
            indexes.add(index);
        }
        if (indexes.size() < 2) {
            return indexes;
        }
        List<INode> children = new ArrayList<>(node.getChildren().size());
        for (INode child : node.getChildren()) {
            children.add(new SemanticNode(child.getBoundingBox(), child.getInitialSemanticType(), child.getSemanticType()));
        }
        List<Integer> tociIndexes = new ArrayList<>(indexes);
        Integer gap = checkTOCIsWithDestinationPage(indexes, infos, children);
        checkTOCIsWithWrongDestination(indexes, tociIndexes, infos, children, gap);
        for (int i = tociIndexes.size() - 1; i >= 0; i--) {
            INode child = children.get(tociIndexes.get(i));
            if (child.getErrorCodes().contains(ErrorCodes.ERROR_CODE_1007) ||
                    (infos.get(tociIndexes.get(i)).getDestinationPageNumber() == null &&
                    child.getErrorCodes().contains(ErrorCodes.ERROR_CODE_1010))) {
                tociIndexes.remove(i);
            }
        }
        checkLeftAndRightAlignments(tociIndexes, infos, children);
        for (int i = tociIndexes.size() - 1; i >= 0; i--) {
            INode child = children.get(tociIndexes.get(i));
            if (child.getErrorCodes().contains(ErrorCodes.ERROR_CODE_1003)) {
                tociIndexes.remove(i);
            }
        }
        removeSingleTOCIIndexes(tociIndexes);
        return tociIndexes;
    }

    private void removeSingleTOCIIndexes(List<Integer> tociIndexes) {
        for (int i = tociIndexes.size() - 2; i >= 1; i--) {
            if (tociIndexes.get(i) - 1 != tociIndexes.get(i - 1) && tociIndexes.get(i) + 1 != tociIndexes.get(i + 1)) {
                tociIndexes.remove(i);
            }
        }
        if (tociIndexes.size() >= 2 && tociIndexes.get(tociIndexes.size() - 2) + 1 != tociIndexes.get(tociIndexes.size() - 1)) {
            tociIndexes.remove(tociIndexes.size() - 1);
        }
        if (tociIndexes.size() >= 2 && tociIndexes.get(0) + 1 != tociIndexes.get(1)) {
            tociIndexes.remove(0);
        }
    }

    public List<Integer> checkTOCIs(List<TOCIInfo> infos, List<INode> children) {
        List<Integer> indexes = new ArrayList<>(infos.size());
        for (int index = 0; index < infos.size(); index++) {
            TOCIInfo tociInfo = infos.get(index);
            INode child = children.get(index);
            if (tociInfo == null || child.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
                continue;
            }
            if (tociInfo.getText() == null || tociInfo.getText().isEmpty()) {
                ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1000);
                continue;
            }
            indexes.add(index);
        }
        List<Integer> tociIndexes = new ArrayList<>(indexes);
        checkLeftAndRightAlignments(indexes, infos, children);
        Integer gap = checkTOCIsWithDestinationPage(indexes, infos, children);
        checkTOCIsWithWrongDestination(indexes, tociIndexes, infos, children, gap);
        return tociIndexes;
    }

    private void checkLeftAndRightAlignments(List<Integer> indexes, List<TOCIInfo> infos, List<INode> children) {
        for (int index : indexes) {
            TOCIInfo tociInfo = infos.get(index);
            INode child = children.get(index);
            if (lastPageNumber != null && !lastPageNumber.equals(child.getPageNumber())) {
//            left = null;
                right = null;
                maxRight = -Double.MAX_VALUE;
            }
            lastPageNumber = child.getPageNumber();
            if (child.getLeftX() > maxRight) {
                //next column
//            left = null;
                right = null;
            }
            maxRight = Math.max(maxRight, tociInfo.getRight());
            if (tociInfo.getPageNumberLabel() != null) {
                if (right == null) {
                    right = tociInfo.getRight();
                } else if (!NodeUtils.areCloseNumbers(right, tociInfo.getRight(),
                        MAX_RIGHT_ALIGNMENT_GAP * tociInfo.getMaxTextSize())) {
                    ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1003);
                }
            }
//        if (left == null) {
//            left = child.getLeftX();
//        } else if (!NodeUtils.areCloseNumbers(left, child.getLeftX(), MAX_LEFT_ALIGNMENT_GAP *
//                tociInfo.getMaxTextSize()) && left > child.getLeftX()) {
//            child.getErrorCodes().add(ErrorCodes.ERROR_CODE_1004);
//        }
        }
    }

    private Integer checkTOCIsWithDestinationPage(List<Integer> indexes, List<TOCIInfo> infos, List<INode> children) {
        List<Integer> newIndexes = new ArrayList<>(indexes.size());
        for (int index : indexes) {
            TOCIInfo info = infos.get(index);
            if (info.getDestinationPageNumber() != null && findText(info.getTextForSearching(), info.getDestinationPageNumber())) {
                newIndexes.add(index);
                findHeading(getNode(info.getDestinationPageNumber()), info.getTextForSearching(), info.getDestinationPageNumber());
            }
        }
        if (newIndexes.isEmpty()) {
            return null;
        }
        List<List<Integer>> possiblePages = new LinkedList<>();
        List<Integer> pageLabels = new LinkedList<>();
        for (int index : newIndexes) {
            TOCIInfo info = infos.get(index);
            if (info.getPageNumberLabel() != null) {
                List<Integer> destinationPage = new LinkedList<>();
                destinationPage.add(info.getDestinationPageNumber());
                possiblePages.add(destinationPage);
                pageLabels.add(info.getPageNumberLabel());
            }
        }
        Integer gap = getMostCommonGap(possiblePages, pageLabels);
        if (gap != null) {
            for (Integer index : newIndexes) {
                TOCIInfo info = infos.get(index);
                if (info.getPageNumberLabel() != null && info.getPageNumberLabel() - info.getDestinationPageNumber() != gap) {
                    ErrorCodes.addErrorCodeWithArguments(children.get(index), ErrorCodes.ERROR_CODE_1002,
                            info.getDestinationPageNumber() + gap);
                }
            }
        }
        indexes.removeAll(newIndexes);
        return gap;
    }

    private void checkTOCIsWithWrongDestination(List<Integer> indexes, List<Integer> tociIndexes, List<TOCIInfo> infos,
                                                List<INode> children, Integer gap) {
        List<List<Integer>> possiblePages = new ArrayList<>(Collections.nCopies(children.size(), null));
        List<Integer> pageLabels = new ArrayList<>(Collections.nCopies(children.size(), null));
        List<Integer> notTOCIIndexes = new LinkedList<>();
        for (Integer index : indexes) {
            TOCIInfo info = infos.get(index);
            List<Integer> pages = getPagesWithText(info.getTextForSearching());
            if (pages.isEmpty()) {
                ErrorCodes.addErrorCodeWithArguments(children.get(index), ErrorCodes.ERROR_CODE_1007);
                tociIndexes.remove(index);
                notTOCIIndexes.add(index);
            } else {
                String possiblePagesArgument = pages.stream().map(x -> x + 1)
                        .map(Object::toString).collect(Collectors.joining(","));
                if (info.getDestinationPageNumber() != null) {
                    ErrorCodes.addErrorCodeWithArguments(children.get(index), ErrorCodes.ERROR_CODE_1008,
                            possiblePagesArgument, info.getDestinationPageNumber() + 1);
                } else {
                    ErrorCodes.addErrorCodeWithArguments(children.get(index), ErrorCodes.ERROR_CODE_1009,
                            possiblePagesArgument);
                }
                if (info.getPageNumberLabel() != null) {
                    possiblePages.set(index, pages);
                    pageLabels.set(index, info.getPageNumberLabel());
                }
            }
        }
        indexes.removeAll(notTOCIIndexes);
        if (!indexes.isEmpty() && gap == null) {
            gap = getMostCommonGap(possiblePages, pageLabels);
        }
        if (gap != null) {
            checkTOCIPageLabels(indexes, infos, possiblePages, children, gap);
        }
    }

    private void checkTOCIPageLabels(List<Integer> indexes, List<TOCIInfo> infos, List<List<Integer>> possiblePages,
                                     List<INode> children, Integer gap) {
        for (int index : indexes) {
            TOCIInfo info = infos.get(index);
            if (info.getPageNumberLabel() != null) {
                boolean wrongPageNumberLabel = true;
                for (Integer textPageNumber : possiblePages.get(index)) {
                    if (info.getPageNumberLabel() - textPageNumber == gap) {
                        wrongPageNumberLabel = false;
                        break;
                    }
                }
                if (wrongPageNumberLabel) {
                    ErrorCodes.addErrorCodeWithArguments(children.get(index), ErrorCodes.ERROR_CODE_1010,
                            possiblePages.get(index).stream().map(x -> x + 1).
                                    map(Object::toString).collect(Collectors.joining(",")));
                }
            }
        }
    }

    private TOCIInfo getTOCIInfo(INode node) {
        TOCIInfo info = new TOCIInfo();
        info.setDestinationPageNumber(getDestinationPageNumber(node));
        info.setRight(node.getRightX());
        List<TextChunk> textChunks = getTextChunks(node);
        info.setMaxTextSize(textChunks.stream().map(TextInfoChunk::getFontSize).max(Double::compare).orElse(0.0));
        if (!textChunks.isEmpty()) {
            TextChunk lastChunk = textChunks.get(textChunks.size() - 1);
            String textValue = lastChunk.getValue();
            int pageLabelLength = 0;
            int numberOfSpaces = getNumberOfEndSpaces(textValue);
            info.setRight(lastChunk.getSymbolEndCoordinate(textValue.length() - numberOfSpaces - 1));
            textValue = textValue.substring(0, textValue.length() - numberOfSpaces);
            if (info.getDestinationPageNumber() != null && info.getDestinationPageNumber() <
                    StaticContainers.getDocument().getNumberOfPages()) {
                String pageLabel = StaticContainers.getDocument().getPage(info.getDestinationPageNumber()).getPageLabel();
                if (pageLabel != null && textValue.toUpperCase().endsWith(pageLabel.toUpperCase())) {
                    info.setPageNumberLabel(info.getDestinationPageNumber());
                    pageLabelLength = pageLabel.length();
                }
            }
            if (info.getPageNumberLabel() == null) {
                pageLabelLength = getNumberOfEndDigits(textValue);
                info.setPageNumberLabel(getPageNumberLabel(textValue, textValue.length() - pageLabelLength));
            }
            textValue = textChunks.stream().map(TextChunk::getValue).collect(Collectors.joining(""));
            textValue = textValue.substring(0, textValue.length() - numberOfSpaces - pageLabelLength);
            textValue = textValue.substring(0, getLastRegexIndex(textValue, SPACES_DOTS_SPACES_REGEX));
            info.setText(textValue);
        }
        return info;
    }

    private Integer getDestinationPageNumber(INode node) {
        List<IAnnotation> linkAnnotations = getInheritorAnnotations(node).stream().
                filter(x -> LINK.equals(x.getAnnotationType())).collect(Collectors.toList());
        for (IAnnotation goToAnnotation : linkAnnotations) {
            Integer number = goToAnnotation.getDestinationPageNumber();
            if (number != null) {
                BoundingBox annotationBoundingBox = new BoundingBox(goToAnnotation.getBoundingBox());
                if (annotationBoundingBox.getPageNumber() == null) {
                    annotationBoundingBox.setPageNumber(node.getPageNumber());
                }
                if (annotationBoundingBox.overlaps(node.getBoundingBox())) {
                    return number;
                }
            }
        }
        return null;
    }

    private List<TextChunk> getTextChunks(INode node) {
        return getTextChunks(node, null);
    }

    private List<TextChunk> getTextChunks(INode node, Integer pageNumber) {
        List<TextChunk> textChunks = new LinkedList<>();
        if (node == currentNode) {
            return textChunks;
        }
        for (INode child : node.getChildren()) {
            if (child == currentNode || child.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
                continue;
            }
            if (child.getPageNumber() == null || (pageNumber != null && (pageNumber < child.getPageNumber() ||
                    pageNumber > child.getLastPageNumber()))) {
                continue;
            }
            if (child instanceof SemanticSpan) {
                for (TextColumn column : ((SemanticSpan)child).getColumns()) {
                    for (TextLine line : column.getLines()) {
                        for (TextChunk chunk : line.getTextChunks()) {
                            if (!chunk.isEmpty() && !TextChunkUtils.isWhiteSpaceChunk(chunk) &&
                                    (pageNumber == null || pageNumber.equals(chunk.getPageNumber()))) {
                                textChunks.add(chunk);
                            }
                        }
                    }
                }
            } else {
                textChunks.addAll(getTextChunks(child, pageNumber));
            }
        }
        return textChunks;
    }

    private static List<IAnnotation> getInheritorAnnotations(INode node) {
        if (node instanceof IAnnotation) {
            return Collections.singletonList((IAnnotation)node);
        }
        List<IAnnotation> annotations = new LinkedList<>();
        for (INode child : node.getChildren()) {
            annotations.addAll(getInheritorAnnotations(child));
        }
        return annotations;
    }

    private static int getNumberOfEndDigits(String string) {
        return getNumberOfEndRegex(string, ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX);
    }

    private static int getNumberOfEndSpaces(String string) {
        return getNumberOfEndRegex(string, SPACES_REGEX);
    }

    private static int getLastRegexIndex(String string, String regex) {
        Pattern pattern = Pattern.compile(regex + "$");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.start();
        }
        return string.length();
    }

    private static int getNumberOfEndRegex(String string, String regex) {
        return string.length() - getLastRegexIndex(string, regex);
    }

    private static Integer getPageNumberLabel(String string, int end) {
        if (end == string.length()) {
            return null;
        }
        try {
            return Integer.parseUnsignedInt(string.substring(end));
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private boolean findText(String text, int pageNumber) {
        INode currentNode = getNode(pageNumber);
        if (currentNode == null) {
            return false;
        }
        String textValue = getTextChunks(currentNode, pageNumber).stream()
                .map(TextChunk::getValue)
                .collect(Collectors.joining("")).replaceAll(NON_CONTENT_REGEX, "").toUpperCase();
        return textValue.contains(text);
    }

    private INode getNode(Integer pageNumber) {
        if (!nodes.containsKey(pageNumber)) {
            nodes.put(pageNumber, findNode(pageNumber));
        }
        return nodes.get(pageNumber);
    }

    private INode findNode(int pageNumber) {
        INode currentNode = StaticContainers.getDocument().getTree().getRoot();
        while (currentNode.getPageNumber() != pageNumber || currentNode.getLastPageNumber() != pageNumber) {
            if (currentNode.getChildren().isEmpty()) {
                return null;
            }
            int depth = currentNode.getDepth();
            for (INode child : currentNode.getChildren()) {
                if (child.getPageNumber() != null && child.getPageNumber() <= pageNumber &&
                        child.getLastPageNumber() >= pageNumber) {
                    currentNode = child;
                    break;
                }
            }
            if (currentNode.getDepth() == depth) {
                return null;
            }
        }
        if (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }
        while (currentNode.getParent() != null) {
            INode previousNode = currentNode.getPreviousNode();
            INode nextNode = currentNode.getNextNode();
            if (currentNode.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT ||
                    (nextNode != null && (nextNode.getPageNumber() == null ||
                            nextNode.getPageNumber() <= pageNumber)) ||
                    (previousNode != null && (previousNode.getLastPageNumber() == null ||
                            previousNode.getLastPageNumber() >= pageNumber))) {
                currentNode = currentNode.getParent();
            } else {
                break;
            }
        }
        if (currentNode.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
            return null;
        }
        return currentNode;
    }

    private List<Integer> getPagesWithText(String text) {
        List<Integer> pageNumbers = new LinkedList<>();
        for (int pageNumber = 0; pageNumber < StaticContainers.getDocument().getNumberOfPages(); pageNumber++) {
            if (findText(text, pageNumber)) {
                pageNumbers.add(pageNumber);
            }
        }
        return pageNumbers;
    }

    private boolean findHeading(INode node, String text, int pageNumber) {
        String textValue = null;
        if (node == null) {
            return false;
        }
        if (node.getInitialSemanticType() == SemanticType.NUMBER_HEADING ||
                node.getInitialSemanticType() == SemanticType.HEADING) {
            textValue = getTextChunks(node, pageNumber).stream()
                    .map(TextChunk::getValue).collect(Collectors.joining(""))
                    .replaceAll(NON_CONTENT_REGEX, "").toUpperCase();
            if (!textValue.contains(text)) {
                return false;
            }
        }
        if (textValue == null || textValue.length() > LENGTH_HEADING_DIFFERENCE * text.length()) {
            for (INode child : node.getChildren()) {
                if (child.getInitialSemanticType() != SemanticType.TABLE_OF_CONTENT &&
                        findHeading(child, text, pageNumber)) {
                    return true;
                }
            }
            return false;
        }
        if (node.getSemanticType() == SemanticType.HEADING || node.getSemanticType() == SemanticType.NUMBER_HEADING) {
            return true;
        }
        INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
        if (accumulatedNode instanceof SemanticTextNode) {
            if (node.getInitialSemanticType() == SemanticType.NUMBER_HEADING) {
                StaticContainers.getAccumulatedNodeMapper().updateNode(node,
                        new SemanticNumberHeading((SemanticTextNode)accumulatedNode),
                        1.0, SemanticType.NUMBER_HEADING);
            } else if (node.getInitialSemanticType() == SemanticType.HEADING) {
                StaticContainers.getAccumulatedNodeMapper().updateNode(node,
                        new SemanticHeading((SemanticTextNode)accumulatedNode),
                        1.0, SemanticType.HEADING);
            }
        }
        return true;
    }

    private Integer getMostCommonGap(List<List<Integer>> possiblePages, List<Integer> pageLabels) {
        Map<Integer, Integer> gaps = new HashMap<>();
        for (int index = 0; index < possiblePages.size(); index++) {
            Integer pageLabel = pageLabels.get(index);
            if (pageLabel == null || possiblePages.get(index) == null) {
                continue;
            }
            for (Integer textPageNumber : possiblePages.get(index)) {
                int gap = pageLabel - textPageNumber;
                if (gaps.containsKey(gap)) {
                    gaps.replace(gap, gaps.get(gap) + 1);
                } else {
                    gaps.put(gap, 1);
                }
            }
        }
        Integer mostCommonGap = null;
        int number = 0;
        for (Map.Entry<Integer, Integer> entry : gaps.entrySet()) {
            if (entry.getValue() > number) {
                mostCommonGap = entry.getKey();
                number = entry.getValue();
            }
        }
        return mostCommonGap;
    }

    private void checkNeighborTOCs(INode node) {
        if (node.getSemanticType() == SemanticType.TABLE_OF_CONTENT) {
            return;
        }
        INode previousChild = null;
        for (INode child : node.getChildren()) {
            if (child.getSemanticType() == SemanticType.TABLE_OF_CONTENT) {
                if (previousChild != null && checkNeighborTOCs(child, previousChild)) {
                    ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1006);
                    ErrorCodes.addErrorCodeWithArguments(previousChild, ErrorCodes.ERROR_CODE_1006);
                    StaticContainers.getIdMapper().put(previousChild.getRecognizedStructureId(),
                            child.getRecognizedStructureId());
                }
                previousChild = child;
            } else {
                previousChild = null;
            }
        }
    }

    private boolean checkNeighborTOCs(INode currentTOC, INode previousTOC) {
        INode previousTOCI = previousTOC.getChildren().get(previousTOC.getChildren().size() - 1);
        if (previousTOCI.getSemanticType() != SemanticType.TABLE_OF_CONTENT_ITEM) {
            return false;
        }
        INode currentTOCI = currentTOC.getChildren().get(0);
        if (currentTOCI.getSemanticType() != SemanticType.TABLE_OF_CONTENT_ITEM) {
            return false;
        }
        int numberOfPreviousTOCIErrors = previousTOCI.getErrorCodes().size();
        int numberOfCurrentTOCIErrors = previousTOCI.getErrorCodes().size();
//        left = null;
        right = null;
        maxRight = -Double.MAX_VALUE;
        pagesGap = null;
        lastPageNumber = null;
        List<TOCIInfo> tociInfos = new ArrayList<>(2);
        tociInfos.add(getTOCIInfo(previousTOCI));
        tociInfos.add(getTOCIInfo(currentTOCI));
        List<INode> tocis = new ArrayList<>(2);
        tocis.add(previousTOCI);
        tocis.add(currentTOCI);
        List<Integer> indexes = checkTOCIs(tociInfos, tocis);
        ErrorCodes.removeErrorCodeWithArgumentsAfterIndex(previousTOCI, numberOfPreviousTOCIErrors);
        ErrorCodes.removeErrorCodeWithArgumentsAfterIndex(currentTOCI, numberOfCurrentTOCIErrors);
        return indexes.size() == 2;
    }

    private static void checkTOCIsNumbering(List<INode> children, List<TOCIInfo> infos) {
        for (int i = 0; i < children.size() - 1; i++) {
            if (infos.get(i) == null || infos.get(i + 1) == null) {
                continue;
            }
            String firstTOCI = infos.get(i).getText();
            String secondTOCI = infos.get(i + 1).getText();
            if (firstTOCI == null || secondTOCI == null) {
                continue;
            }
            int commonStartLength = ListLabelsUtils.getCommonStartLength(firstTOCI, secondTOCI);
            String prefix = firstTOCI.substring(0, commonStartLength);
            if (commonStartLength != 0) {
                firstTOCI = firstTOCI.substring(commonStartLength);
                secondTOCI = secondTOCI.substring(commonStartLength);
            }
            checkArabicNumbering(children.get(i + 1), firstTOCI, secondTOCI, prefix);
        }
    }

    private static boolean checkArabicNumbering(INode child, String firstTOCI, String secondTOCI, String prefix) {
        String firstSubstring = firstTOCI.substring(0, ListLabelsDetectionAlgorithm.getRegexStartLength(firstTOCI,
                ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX));
        String secondSubstring = secondTOCI.substring(0, ListLabelsDetectionAlgorithm.getRegexStartLength(secondTOCI,
                ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX));
        if (firstSubstring.isEmpty() || secondSubstring.isEmpty()) {
            return false;
        }
        Integer firstNumber = ArabicNumbersListLabelsDetectionAlgorithm.getIntegerFromString(firstSubstring);
        Integer secondNumber = ArabicNumbersListLabelsDetectionAlgorithm.getIntegerFromString(secondSubstring);
        if (firstNumber == null || secondNumber == null) {
            return false;
        }
        if (firstNumber + 1 != secondNumber) {
            ErrorCodes.addErrorCodeWithArguments(child, ErrorCodes.ERROR_CODE_1011,
                    prefix + secondSubstring, prefix + (firstNumber + 1));
            return false;
        }
        return true;
    }

}
