package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

import java.util.ArrayList;
import java.util.List;

public class TableRecognitionArea {
    private static final double MERGE_PROBABILITY_THRESHOLD = 0.75;
    private static final double NEXT_LINE_TOLERANCE_FACTOR = 1.1;
    private static final double ONE_LINE_TOLERANCE_FACTOR = 0.1;
    private static final double TABLE_GAP_FACTOR = 5.0;

    private boolean hasCompleteHeaders;
    private boolean isComplete;
    private boolean isValid;
    private List<TableCluster> headers;
    private List<TableCluster> clusters;
    BoundingBox boundingBox;
    double baseLine;

    public TableRecognitionArea() {
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

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<TableCluster> getHeaders() {
        return headers;
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
            if (baseLine - token.getBaseLine() > NEXT_LINE_TOLERANCE_FACTOR * token.getFontSize()) {
                if (headers.size() < 2) {
                    hasCompleteHeaders = isComplete = true;
                } else {
                    addCluster(token);
                }
            } else {
                expandHeaders(token);
            }
        }
    }

    private void expandHeaders(TextChunk token) {
        if (headers.isEmpty()) {
            TableCluster header = new TableCluster(token);
            header.setHeader(header);
            headers.add(header);
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

        if (headerBaseLine - tokenBaseLine < ONE_LINE_TOLERANCE_FACTOR * token.getFontSize() &&
                ChunksMergeUtils.toLineMergeProbability(header.getLastToken(), token) > MERGE_PROBABILITY_THRESHOLD) {
            // token can be appended to the last line of the header
            header.add(token);
            if (token.getBaseLine() < baseLine) {
                baseLine = token.getBaseLine();
            }
            return true;
        }

        BoundingBox headerBBox = header.getBoundingBox();
        if (headerBBox.getLeftX() < token.getRightX() && token.getLeftX() < headerBBox.getRightX()) {
            // token belongs to the next line of the header
            header.add(token, true);
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

            currentHeader.add(header);
            return true;
        }
        return false;
    }

    private void addCluster(TextChunk token) {
        if (baseLine - token.getBaseLine() > TABLE_GAP_FACTOR * token.getFontSize()) {
            isComplete = true;
            return;
        }

        TableCluster currentCluster = new TableCluster(token);
        TableCluster currentHeader = null;
        BoundingBox tokenBBox = token.getBoundingBox();
        for (TableCluster header : headers) {
            BoundingBox headerBBox = header.getBoundingBox();
            if (headerBBox.getLeftX() < token.getRightX() && token.getLeftX() < headerBBox.getRightX()) {
                if (currentHeader == null) {
                    currentHeader = header;
                } else {
                    isComplete = true;
                    isValid = false;
                    return;
                }
            }
        }
        currentCluster.setHeader(currentHeader);
        clusters.add(currentCluster);
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
