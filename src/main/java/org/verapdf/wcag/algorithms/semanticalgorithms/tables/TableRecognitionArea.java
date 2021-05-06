package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableRecognitionArea {
    private static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
    private static final double NEXT_LINE_TOLERANCE_FACTOR = 1.05;
    private static final double ONE_LINE_TOLERANCE_FACTOR = 0.9;
    private static final double TABLE_GAP_FACTOR = 5.0;
    private static final double HEADERS_PROBABILITY_THRESHOLD = 0.75;

    private double adaptiveNextLineToleranceFactor;

    private boolean hasCompleteHeaders;
    private boolean isComplete;
    private boolean isValid;
    private List<TableCluster> headers;
    private List<TableCluster> clusters;
    BoundingBox boundingBox;
    double baseLine;

    public TableRecognitionArea() {
        adaptiveNextLineToleranceFactor = NEXT_LINE_TOLERANCE_FACTOR;
        hasCompleteHeaders = isComplete = isValid = false;
        headers = new ArrayList<>();
        clusters = new ArrayList<>();
        baseLine = 0d;
        boundingBox = new BoundingBox();
    }

    public void setPageNumber(int pageNumber) {
        boundingBox.setPageNumber(pageNumber);
    }

    public Integer getPageNumber() {
        return boundingBox.getPageNumber();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean hasCompleteHeaders() {
        return hasCompleteHeaders;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<TableCluster> getHeaders() {
        return headers;
    }

    public List<TableCluster> getClusters() {
        return clusters;
    }

    public void  addTokenToRecognitionArea(TextChunk token) {
        if (isComplete) {
            return;
        }

        if (token.getPageNumber() == null) {
            return;
        } else if (boundingBox.getPageNumber() == null) {
            boundingBox.setPageNumber(token.getPageNumber());
        } else if (!boundingBox.getPageNumber().equals(token.getPageNumber())) {
            hasCompleteHeaders = isComplete = true;
            return;
        }

        if (hasCompleteHeaders) {
            addCluster(token);
        } else {
            if (belongsToHeadersArea(token)) {
                expandHeaders(token);
            } else {
                hasCompleteHeaders = true;
                if (checkHeaders()) {
                    sortHeaders();
                    addCluster(token);
                } else {
                    isComplete = true;
                }
            }
        }
    }

    private boolean checkHeaders() {
        if (headers.size() < 2) {
            return false;
        }

        double avrFirstBaseLine = 0.0;
        double avrLastBaseLine = 0.0;
        double avrCenter = 0.0;
        for (TableCluster header : headers) {
            TextLine firstLine = header.getFirstLine();
            TextLine lastLine = header.getLastLine();
            avrFirstBaseLine += firstLine.getBaseLine();
            avrLastBaseLine += lastLine.getBaseLine();
            avrCenter += 0.5 * (firstLine.getBaseLine() + lastLine.getBaseLine());
        }
        avrFirstBaseLine /= headers.size();
        avrLastBaseLine /= headers.size();
        avrCenter /= headers.size();

        double maxTopDeviation = 0.0;
        double maxBottomDeviation = 0.0;
        double maxCenterDeviation = 0.0;
        for (TableCluster header : headers) {
            TextLine firstLine = header.getFirstLine();
            TextLine lastLine = header.getLastLine();
            double fontSize = firstLine.getFontSize();
            double topDeviation = Math.abs(avrFirstBaseLine - firstLine.getBaseLine()) / fontSize;
            double bottomDeviation = Math.abs(avrLastBaseLine - lastLine.getBaseLine()) / fontSize;
            double centerDeviation = Math.abs(avrCenter -0.5 * (firstLine.getBaseLine() + lastLine.getBaseLine())) / fontSize;

            if (maxTopDeviation < topDeviation) {
                maxTopDeviation = topDeviation;
            }
            if (maxBottomDeviation < bottomDeviation) {
                maxBottomDeviation = bottomDeviation;
            }
            if (maxCenterDeviation < centerDeviation) {
                maxCenterDeviation = centerDeviation;
            }
        }

        double headersProbability = 1.0 - Math.min(Math.min(maxTopDeviation, maxBottomDeviation), maxCenterDeviation);
        return headersProbability > HEADERS_PROBABILITY_THRESHOLD;
    }

    private boolean belongsToHeadersArea(TextChunk token) {
        if (headers.isEmpty()) {
            return true;
        }
        if (baseLine - token.getBaseLine() > adaptiveNextLineToleranceFactor * token.getFontSize()) {
            return false;
        }
        if (token.getBottomY() > boundingBox.getTopY() + TABLE_GAP_FACTOR * token.getFontSize()) {
            return false;
        }

        return true;
    }

    private void expandHeaders(TextChunk token) {
        if (headers.isEmpty()) {
            TableCluster header = new TableCluster(token);
            header.setHeader(header);
            headers.add(header);
            boundingBox = new BoundingBox(token.getBoundingBox());
            baseLine = token.getBaseLine();
            return;
        }

        TableCluster currentHeader = null;
        List<TableCluster> headersToRemove = new ArrayList<>();
        for (TableCluster header : headers) {
            if (currentHeader == null) {
                if (expandHeader(header, token)) {
                    currentHeader = header;
                }
            } else {
                if (joinHeaders(currentHeader, header, token)) {
                    headersToRemove.add(header);
                }
            }
        }

        if (currentHeader == null) {
            TableCluster header = new TableCluster(token);
            header.setHeader(header);
            headers.add(header);
            boundingBox.union(token.getBoundingBox());
            if (token.getBaseLine() < baseLine) {
                baseLine = token.getBaseLine();
            }
        } else {
            headers.removeAll(headersToRemove);
        }
    }

    private boolean expandHeader(TableCluster header, TextChunk token) {
        double headerBaseLine = header.getBaseLine();
        double tokenBaseLine = token.getBaseLine();
        double baseLineDiff = Math.abs(headerBaseLine - tokenBaseLine);

        if (baseLineDiff < ONE_LINE_TOLERANCE_FACTOR * token.getFontSize() &&
                ChunksMergeUtils.toLineMergeProbability(header.getLastToken(), token) > MERGE_PROBABILITY_THRESHOLD) {
            // token can be appended to the last line of the header
            header.mergeWithoutRowNumbers(token);
            if (token.getBaseLine() < baseLine) {
                baseLine = token.getBaseLine();
            }
            return true;
        }

        BoundingBox headerBBox = header.getBoundingBox();
        if (headerBBox.getLeftX() < token.getRightX() && token.getLeftX() < headerBBox.getRightX()) {
            // token belongs to the next line of the header
            double lineSpacingFactor = baseLineDiff / token.getFontSize();
            if (adaptiveNextLineToleranceFactor < lineSpacingFactor) {
                adaptiveNextLineToleranceFactor = lineSpacingFactor * NEXT_LINE_TOLERANCE_FACTOR;
            }

            header.mergeWithoutRowNumbers(token, true);
            if (token.getBaseLine() < baseLine) {
                baseLine = token.getBaseLine();
            }
            return true;
        }

        return false;
    }

    private boolean joinHeaders(TableCluster currentHeader, TableCluster header, TextChunk token) {
        BoundingBox headerBBox = header.getBoundingBox();

        if (headerBBox.getLeftX() < token.getRightX() && token.getLeftX() < headerBBox.getRightX()) {
            // token belongs to both headers => join headers

            currentHeader.mergeWithoutRowNumbers(header);
            boundingBox.union(token.getBoundingBox());
            if (token.getBaseLine() < baseLine) {
                baseLine = token.getBaseLine();
            }

            return true;
        }
        return false;
    }

    private void sortHeaders() {
        Collections.sort(headers, (Comparator.<TableCluster>
                        comparingDouble(header1 -> header1.getLeftX())
                        .thenComparingDouble(header2 -> header2.getLeftX())));
    }

    private void addCluster(TextChunk token) {
        if (baseLine - token.getBaseLine() > TABLE_GAP_FACTOR * token.getFontSize()) {
            isComplete = true;
            return;
        }

        TableCluster currentCluster = new TableCluster(token);
        TableCluster firstHeader = null;
        TableCluster lastHeader = null;
        BoundingBox tokenBBox = token.getBoundingBox();
        for (TableCluster header : headers) {
            BoundingBox headerBBox = header.getBoundingBox();
            if (headerBBox.getLeftX() < token.getRightX() && token.getLeftX() < headerBBox.getRightX()) {
                if (firstHeader == null) {
                    firstHeader = header;
                    lastHeader = header;
                } else {
                    lastHeader = header;
                }
            }
        }
        currentCluster.setHeader(firstHeader);
        currentCluster.setLastHeader(lastHeader);
        clusters.add(currentCluster);
        boundingBox.union(currentCluster.getBoundingBox());
        if (currentCluster.getBaseLine() < baseLine) {
            baseLine = currentCluster.getBaseLine();
        }
        isValid = true;
    }

    @Override
    public String toString() {
        // for debug output
        StringBuilder result = new StringBuilder("TableRecognitionArea{\n");
        result.append("    headers={\n");
        for (TableCluster header : headers) {
            result.append('[').append(header).append("]\n");
        }
        result.append("    }, clusters={\n");
        for (TableCluster cluster : clusters) {
            result.append('[').append(cluster).append("] : [").append(String.valueOf(cluster.getHeader())).append("]\n");
        }
        result.append("    }, boundingBox=").append(boundingBox).append('\n');
        result.append("    , baseLine=").append(baseLine).append("\n}");
        return result.toString();
    }
}
