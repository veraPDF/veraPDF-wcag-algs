package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;
import org.verapdf.wcag.algorithms.entities.tables.TableTokenRow;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.ArrayList;
import java.util.List;

public class TableRecognitionArea {
    private double adaptiveNextLineToleranceFactor;

    private boolean hasCompleteHeaders;
    private boolean isComplete;
    private boolean isValid;
    private final List<TableCluster> headers;
    private final List<TableCluster> clusters;
    private BoundingBox boundingBox;
    private double headersBaseLine;
    private double baseLine;
    private TableBorder tableBorder;

    public TableRecognitionArea() {
        adaptiveNextLineToleranceFactor = TableUtils.NEXT_LINE_TOLERANCE_FACTOR;
        hasCompleteHeaders = isComplete = isValid = false;
        headers = new ArrayList<>();
        clusters = new ArrayList<>();
        headersBaseLine = Double.MAX_VALUE;
        baseLine = Double.MAX_VALUE;
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

    public boolean addTokenToRecognitionArea(TableToken token) {
        if (isComplete) {
            return false;
        }

        if (token.getPageNumber() == null) {
            return false;
        } else if (boundingBox.getPageNumber() == null) {
            boundingBox.setPageNumber(token.getPageNumber());
        } else if (!boundingBox.getPageNumber().equals(token.getPageNumber())) {
            hasCompleteHeaders = isComplete = true;
            headersBaseLine = baseLine;
            return false;
        }

        if (hasCompleteHeaders) {
            return addCluster(token);
        } else {
            if (belongsToHeadersArea(token)) {
                return expandHeaders(token);//check bounding box?
            } else {
                hasCompleteHeaders = true;
                headersBaseLine = baseLine;
                if (checkHeaders()) {
                    return addCluster(token);
                } else {
                    isComplete = true;
                }
            }
        }
        return false;
    }

    private boolean checkHeaders() {
        if (headers.size() < 2) {
            return false;
        }

        double avrFirstBaseLine = 0.0;
        double avrLastBaseLine = 0.0;
        double avrCenter = 0.0;
        for (TableCluster header : headers) {
            TableTokenRow firstLine = header.getFirstRow();
            TableTokenRow lastLine = header.getLastRow();
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
            TableTokenRow firstLine = header.getFirstRow();
            TableTokenRow lastLine = header.getLastRow();
            double fontSize = firstLine.getFontSize();
            double topDeviation = Math.abs(avrFirstBaseLine - firstLine.getBaseLine()) / fontSize;
            double bottomDeviation = Math.abs(avrLastBaseLine - lastLine.getBaseLine()) / fontSize;
            double centerDeviation = Math.abs(avrCenter - 0.5 * (firstLine.getBaseLine() + lastLine.getBaseLine())) / fontSize;

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
        return headersProbability > TableUtils.HEADERS_PROBABILITY_THRESHOLD;
    }

    private boolean belongsToHeadersArea(TableToken token) {
        if (headers.isEmpty()) {
            return true;
        }
        if (baseLine - token.getBaseLine() > adaptiveNextLineToleranceFactor * token.getFontSize()) {
            return false;
        }
        if (token.getBottomY() > boundingBox.getTopY() + TableUtils.TABLE_GAP_FACTOR * token.getFontSize()) {
            return false;
        }

        return true;
    }

    private boolean expandHeaders(TableToken token) {
        if (headers.isEmpty()) {
            TableCluster header = new TableCluster(token);
            header.setHeader(header);
            headers.add(header);
            boundingBox = new BoundingBox(token.getBoundingBox());
            baseLine = token.getBaseLine();
            return true;
        }

        boolean tokenAdded = false;
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
                    tokenAdded = true;
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
            return true;
        } else {
            headers.removeAll(headersToRemove);
        }
        return tokenAdded;
    }

    private boolean expandHeader(TableCluster header, TableToken token) {
        double headerBaseLine = header.getBaseLine();
        double tokenBaseLine = token.getBaseLine();
        double baseLineDiff = Math.abs(headerBaseLine - tokenBaseLine);

        if (baseLineDiff < TableUtils.ONE_LINE_TOLERANCE_FACTOR * token.getFontSize() &&
                ChunksMergeUtils.toLineMergeProbability(header.getLastToken(), token) > TableUtils.MERGE_PROBABILITY_THRESHOLD) {
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
            double lineSpacingFactor = baseLineDiff / token.getFontSize();
            if (lineSpacingFactor < TableUtils.NEXT_LINE_MAX_TOLERANCE_FACTOR) {
                if (adaptiveNextLineToleranceFactor < lineSpacingFactor) {
                    adaptiveNextLineToleranceFactor = lineSpacingFactor * TableUtils.NEXT_LINE_TOLERANCE_FACTOR;
                }

                header.add(token, true);
                if (token.getBaseLine() < baseLine) {
                    baseLine = token.getBaseLine();
                }
                return true;
            }
        }

        return false;
    }

    private boolean joinHeaders(TableCluster currentHeader, TableCluster header, TableToken token) {
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

    private boolean addCluster(TableToken token) {
        if (baseLine - token.getBaseLine() > TableUtils.TABLE_GAP_FACTOR * token.getFontSize() ||
                headersBaseLine < token.getBaseLine() ||
                (tableBorder != null && !tableBorder.getBoundingBox().contains(token.getBoundingBox()))) {
            isComplete = true;
            return false;
        }

        TableCluster cluster = new TableCluster(token);
        clusters.add(cluster);
        boundingBox.union(cluster.getBoundingBox());
        if (cluster.getBaseLine() < baseLine) {
            baseLine = cluster.getBaseLine();
        }
        isValid = true;
        return true;
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
            result.append('[').append(cluster).append("] : [").append(cluster.getHeader()).append("]\n");
        }
        result.append("    }, boundingBox=").append(boundingBox).append('\n');
        result.append("    , baseLine=").append(baseLine).append("\n}");
        return result.toString();
    }

    public TableBorder getTableBorder() {
        return tableBorder;
    }

    public void setTableBorder(TableBorder tableBorder) {
        this.tableBorder = tableBorder;
    }
}
