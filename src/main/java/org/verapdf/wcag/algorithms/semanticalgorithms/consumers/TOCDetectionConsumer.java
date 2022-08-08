package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.tocs.TOCIInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TOCDetectionConsumer implements Consumer<INode> {

    private static final String LINK = "Link";
    private static final String SPACES = "\\s\u00A0\u2007\u202F";
    private static final String SPACES_REGEX = "[" + SPACES + "]+";
    private static final String SPACES_DOTS_SPACES_REGEX = "[" + SPACES + "]*\\.*[" + SPACES + "]*";
    private static final String NON_CONTENT_REGEX = "[" + SPACES + "\u2011-]";
    private static final double MAX_RIGHT_ALIGNMENT_GAP = 1.0;
//    private static final double MAX_LEFT_ALIGNMENT_GAP = 1.0;

    private final IDocument document;
//    private Double left = null;
    private Double right = null;
    private Double maxRight = -Double.MAX_VALUE;
    private Integer pagesGap = null;
    private Integer lastPageNumber = null;

    public TOCDetectionConsumer(IDocument document) {
        this.document = document;
    }

    @Override
    public void accept(INode node) {
        if (node.getInitialSemanticType() != SemanticType.TABLE_OF_CONTENT) {
            return;
        }
        List<TOCIInfo> infos = getTOCIInfos(node);
        List<Integer> tociIndexes = new ArrayList<>(node.getChildren().size());
//        left = null;
        right = null;
        maxRight = -Double.MAX_VALUE;
        pagesGap = null;
        lastPageNumber = null;
        for (int index = 0; index < node.getChildren().size(); index++) {
            INode child = node.getChildren().get(index);
            if (child.getSemanticType() != SemanticType.TABLE_OF_CONTENT && checkTOCI(child, infos.get(index))) {
                tociIndexes.add(index);
            }
        }
        for (int index : tociIndexes) {
            INode child = node.getChildren().get(index);
            StaticContainers.getAccumulatedNodeMapper().updateNode(child,
                    StaticContainers.getAccumulatedNodeMapper().get(child), 1.0, SemanticType.TABLE_OF_CONTENT_ITEM);
        }
    }

    private boolean checkTOCI(INode child, TOCIInfo tociInfo) {
        if (tociInfo.getText() == null) {
            return false;
        }
        if (tociInfo.getDestinationPageNumber() == null) {
            return false;
        }
        if (tociInfo.getPageNumberLabel() != null) {
            if (pagesGap == null) {
                pagesGap = tociInfo.getPageNumberLabel() - tociInfo.getDestinationPageNumber();
            } else if (tociInfo.getDestinationPageNumber() + pagesGap != tociInfo.getPageNumberLabel()) {
                return false;
            }
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
            } else if (!NodeUtils.areCloseNumbers(right, tociInfo.getRight(), MAX_RIGHT_ALIGNMENT_GAP)) {
                return false;
            }
        }
//        if (left == null) {
//            left = child.getLeftX();
//        } else if (!NodeUtils.areCloseNumbers(left, child.getLeftX(), MAX_LEFT_ALIGNMENT_GAP) && left > child.getLeftX()) {
//            return false;
//        }
        if (!findText(document.getTree().getRoot(),
                tociInfo.getText().replaceAll(NON_CONTENT_REGEX,"").toUpperCase(), tociInfo.getDestinationPageNumber())) {
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

    private TOCIInfo getTOCIInfo(INode node) {
        TOCIInfo info = new TOCIInfo();
        info.setDestinationPageNumber(getDestinationPageNumber(node));
        info.setRight(node.getRightX());
        List<TextChunk> textChunks = getTextChunks(node);
        if (!textChunks.isEmpty()) {
            TextChunk lastChunk = textChunks.get(textChunks.size() - 1);
            String textValue = lastChunk.getValue();
            int pageLabelSize = 0;
            int numberOfSpaces = getNumberOfEndSpaces(textValue);
            info.setRight(lastChunk.getSymbolEndCoordinate(textValue.length() - numberOfSpaces - 1));
            textValue = textValue.substring(0, textValue.length() - numberOfSpaces);
            if (info.getDestinationPageNumber() != null && info.getDestinationPageNumber() < document.getPages().size()) {
                String pageLabel = document.getPage(info.getDestinationPageNumber()).getPageLabel();
                if (pageLabel != null && textValue.toUpperCase().endsWith(pageLabel.toUpperCase())) {
                    info.setPageNumberLabel(info.getDestinationPageNumber());
                    pageLabelSize = pageLabel.length();
                }
            }
            if (info.getPageNumberLabel() == null) {
                pageLabelSize = getNumberOfEndDigits(textValue);
                info.setPageNumberLabel(getPageNumberLabel(textValue, textValue.length() - pageLabelSize));
            }
            textValue = textChunks.stream().map(TextChunk::getValue).collect(Collectors.joining(""));
            textValue = textValue.substring(0, textValue.length() - numberOfSpaces - pageLabelSize);
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

    private static List<TextChunk> getTextChunks(INode node) {
        List<TextChunk> textChunks = new LinkedList<>();
        for (INode child : node.getChildren()) {
            if (child.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
                continue;
            }
            if (child instanceof SemanticSpan) {
                for (TextColumn column : ((SemanticSpan)child).getColumns()) {
                    for (TextLine line : column.getLines()) {
                        for (TextChunk chunk : line.getTextChunks()) {
                            if (!chunk.isEmpty() && !TextChunkUtils.isWhiteSpaceChunk(chunk)) {
                                textChunks.add(chunk);
                            }
                        }
                    }
                }
            } else {
                textChunks.addAll(getTextChunks(child));
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

    private boolean findText(INode node, String text, int pageNumber) {
        for (INode child : node.getChildren()) {
            if (child.getPageNumber() != null && child.getPageNumber() <= pageNumber &&
                    child.getLastPageNumber() >= pageNumber) {
                if (child.getPageNumber() == pageNumber && child.getLastPageNumber() == pageNumber) {
                    INode parent = node;
                    while (parent.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT && parent.getParent() != null) {
                        parent = parent.getParent();
                    }
                    if (parent.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT) {
                        return false;
                    }
                    String textValue = getTextChunks(parent).stream()
                            .filter(textChunk -> pageNumber == textChunk.getPageNumber())
                            .map(TextChunk::getValue).collect(Collectors.joining(""));
                    return textValue.replaceAll(NON_CONTENT_REGEX, "").toUpperCase().contains(text);
                }
                if (findText(child, text, pageNumber)) {
                    return true;
                }
            }
        }
        return false;
    }
}
