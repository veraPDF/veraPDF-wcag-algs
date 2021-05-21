package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

 import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextInfoChunk;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.util.*;

public class TableCluster extends TextInfoChunk {

    private Integer id = null;

    private TableCluster header = null;
    private Integer colNumber = null;

    private List<TableRow> rows = new ArrayList<>();

    private TableClusterGap minLeftGap = null;
    private TableClusterGap minRightGap = null;

    public enum Side {
        LEFT,
        RIGHT;

        public static Side opposite(Side side) {
            if (side == LEFT) {
                return RIGHT;
            } else {
                return LEFT;
            }
        }
    }

    public TableCluster() {
    }

    public TableCluster(TableToken token) {
        this(new TableRow(token));
    }

    public TableCluster(TableRow row) {
        super(row.getBoundingBox(), row.getFontSize(), row.getBaseLine());
        rows.add(row);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setMinLeftGap(TableClusterGap leftGap) {
        minLeftGap = leftGap;
    }

    public TableClusterGap getMinLeftGap() {
        return minLeftGap;
    }

    public void setMinRightGap(TableClusterGap rightGap) {
        minRightGap = rightGap;
    }

    public TableClusterGap getMinRightGap() {
        return minRightGap;
    }

    public void setRowNumber(int rowIndex, int rowNumber) {
        rows.get(rowIndex).setRowNumber(rowNumber);
    }

    public Integer getRowNumber(int rowIndex) {
        return rows.get(rowIndex).getRowNumber();
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public void add(TableToken token) {
        add(token, false);
    }

    public void add(TableToken token, boolean newLine) {
        if (newLine || rows.isEmpty()) {
            rows.add(new TableRow(token));
        } else {
            rows.get(rows.size() - 1).add(token);
        }
        if (fontSize < token.getFontSize()) {
            fontSize = token.getFontSize();
        }
        if (baseLine < token.getBaseLine()) {
            baseLine = token.getBaseLine();
        }
        getBoundingBox().union(token.getBoundingBox());
    }

    public void add(TableRow row) {
        add(row, false);
    }

    public void add(TableRow row, boolean newLine) {
        if (newLine || rows.isEmpty()) {
            rows.add(row);
        } else {
            TableRow lastLine = rows.get(rows.size() - 1);
            lastLine.add(row);
        }
        if (fontSize < row.getFontSize()) {
            fontSize = row.getFontSize();
        }
        if (row.getBaseLine() < baseLine) {
            baseLine = row.getBaseLine();
        }
        getBoundingBox().union(row.getBoundingBox());
    }

    public void mergeWithoutRowNumbers(TableCluster other) {
        List<TableRow> result = new ArrayList<>();

        int i = 0, j = 0;
        while (i < rows.size() && j < other.rows.size()) {
            TableRow line = rows.get(i);
            TableRow otherLine = other.rows.get(j);
            double baseLine = line.getBaseLine();
            double otherBaseLine = otherLine.getBaseLine();
            double tolerance = TableUtils.ONE_LINE_TOLERANCE_FACTOR * Math.min(line.getFontSize(), otherLine.getFontSize());

            if (baseLine > otherBaseLine + tolerance) {
                result.add(line);
                ++i;
            } else if (baseLine < otherBaseLine - tolerance) {
                result.add(otherLine);
                ++j;
            } else {
                TableRow unitedLine = new TableRow(line);
                unitedLine.add(otherLine);
                result.add(unitedLine);
                ++i;
                ++j;
            }
        }
        for (; i < rows.size(); ++i) {
            result.add(rows.get(i));
        }
        for (; j < other.rows.size(); ++j) {
            result.add(other.rows.get(j));
        }

        if (fontSize < other.getFontSize()) {
            fontSize = other.getFontSize();
        }
        if (other.getBaseLine() < baseLine) {
            baseLine = other.getBaseLine();
        }
        getBoundingBox().union(other.getBoundingBox());
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public TableRow getFirstRow() {
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public TableRow getLastRow() {
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(rows.size() - 1);
    }

    public TextChunk getFirstToken() {
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0).getFirstTextChunk();
    }

    public TextChunk getLastToken() {
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(rows.size() - 1).getLastTextChunk();
    }

    public void setHeader(TableCluster header) {
        this.header = header;
    }

    public TableCluster getHeader() {
        return header;
    }

    public boolean isHeader() {
        return this == header;
    }

    public void updateMinGaps() {
        updateMinGap(Side.LEFT);
        updateMinGap(Side.RIGHT);
    }

    public void updateMinGap(Side side) {
        Map<TableCluster, Double> gapsMap = new HashMap<>();
        Map<TableCluster, Integer> countersMap = new HashMap<>();
        for (TableRow row : rows) {
            TableClusterGap rowGap = (side == Side.LEFT) ? row.getLeftGap() : row.getRightGap();
            if (rowGap != null) {
                if (gapsMap.containsKey(rowGap.getLink())) {
                    gapsMap.put(rowGap.getLink(), gapsMap.get(rowGap.getLink()) + rowGap.getGap());
                    countersMap.put(rowGap.getLink(), countersMap.get(rowGap.getLink()) + 1);
                } else {
                    gapsMap.put(rowGap.getLink(), rowGap.getGap());
                    countersMap.put(rowGap.getLink(), 1);
                }
            }
        }
        if (gapsMap.isEmpty()) {
            return;
        }

        TableClusterGap minGap = (side == Side.LEFT) ? minLeftGap : minRightGap;
        if (minGap == null) {
            minGap = new TableClusterGap(null, Double.MAX_VALUE);
            if (side == Side.LEFT) {
                minLeftGap = minGap;
            } else {
                minRightGap = minGap;
            }
        }
        minGap.setGap(Double.MAX_VALUE);
        for (Map.Entry<TableCluster, Double> gapEntry : gapsMap.entrySet()) {
            double gap = gapEntry.getValue() / countersMap.get(gapEntry.getKey());
            if (gap < minGap.getGap()) {
                minGap.setLink(gapEntry.getKey());
                minGap.setGap(gapEntry.getValue());
            }
        }
    }

    public void merge(TableCluster other, boolean update) {

        if (header == null) {
            header = other.getHeader();
        }

        for (TableRow row : rows) {
            if (row.getLeftGap() != null && row.getLeftGap().getLink() == other) {
                row.setLeftGap(null);
            }
            if (row.getRightGap() != null && row.getRightGap().getLink() == other) {
                row.setRightGap(null);
            }
        }

        Set<TableCluster> leftSet = new HashSet<>();
        Set<TableCluster> rightSet = new HashSet<>();

        for (TableRow row : other.getRows()) {
            if (row.getLeftGap() != null) {
                if (row.getLeftGap().getLink() == this) {
                    row.setLeftGap(null);
                } else {
                    leftSet.add(row.getLeftGap().getLink());
                }
            }
            if (row.getRightGap() != null) {
                if (row.getRightGap().getLink() == this) {
                    row.setRightGap(null);
                } else {
                    rightSet.add(row.getRightGap().getLink());
                }
            }
            rows.add(row);
        }

        // update right gaps for the clusters to the left
        for (TableCluster leftCluster : leftSet) {
            for (TableRow row : leftCluster.getRows()) {
                if (row.getRightGap() != null && row.getRightGap().getLink() == other) {
                    row.getRightGap().setLink(this);
                }
            }
            if (update) {
                TableClusterGap rightGap = leftCluster.getMinRightGap();
                if (rightGap != null && (rightGap.getLink() == this || rightGap.getLink() == other)) {
                    leftCluster.updateMinGap(Side.RIGHT);
                }
            }
        }

        // update left gaps for the clusters to the right
        for (TableCluster rightCluster : rightSet) {
            for (TableRow row : rightCluster.getRows()) {
                if (row.getLeftGap() != null && row.getLeftGap().getLink() == other) {
                    row.getLeftGap().setLink(this);
                }
            }
            if (update) {
                TableClusterGap leftGap = rightCluster.getMinLeftGap();
                if (leftGap != null && (leftGap.getLink() == this || leftGap.getLink() == other)) {
                    rightCluster.updateMinGap(Side.LEFT);
                }
            }
        }

        // update merged cluster min gaps
        if (update) {
            if (leftSet.size() > 0) {
                updateMinGap(Side.LEFT);
            }
            if (rightSet.size() > 0) {
                updateMinGap(Side.RIGHT);
            }
        }

        if (fontSize < other.getFontSize()) {
            fontSize = other.getFontSize();
        }
        if (other.getBaseLine() < baseLine) {
            baseLine = other.getBaseLine();
        }
        getBoundingBox().union(other.getBoundingBox());
    }

    @Override
    public String toString() {
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(rows.get(0).toString());
        for (int i = 1; i < rows.size(); ++i) {
            result.append('\n').append(rows.get(i));
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        } else {
            return id;
        }
    }
}
