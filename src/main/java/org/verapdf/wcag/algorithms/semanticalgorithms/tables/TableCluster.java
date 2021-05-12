package org.verapdf.wcag.algorithms.semanticalgorithms.tables;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableCluster extends InfoChunk {

    private static final double ONE_LINE_TOLERANCE_FACTOR = 0.2;

    private TableCluster header = null;
    private TableCluster lastHeader = null;

    private List<TextLine> lines = new ArrayList<>();

    private List<Integer> rowNumbers;
    private Integer colNumber = null;
    private Integer lastColNumber = null;

    public TableCluster() {}

    public TableCluster(TextChunk chunk) {
        this(new TextLine(chunk));
    }

    public TableCluster(TextLine line) {
        super(line.getBoundingBox());
        rowNumbers = new ArrayList<>();
        lines.add(line);
        rowNumbers.add(null);
        header = null;
    }

    public void setRowNumber(int lineNumber, int rowNumber) {
        if (lineNumber < lines.size()) {
            rowNumbers.set(lineNumber, rowNumber);
        }
    }

    public Integer getRowNumber(int lineNumber) {
        if (lineNumber < lines.size()) {
            return rowNumbers.get(lineNumber);
        }
        return null;
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
        if (lastColNumber == null || lastColNumber < colNumber) {
            lastColNumber = colNumber;
        }
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public void setLastColNumber(int lastColNumber) {
        this.lastColNumber = lastColNumber;
        if (colNumber == null || lastColNumber < colNumber) {
            colNumber = lastColNumber;
        }
    }

    public Integer getLastColNumber() {
        return lastColNumber;
    }

    public double getBaseLine() {
        if (lines.isEmpty()) {
            return 0d;
        }
        return lines.get(lines.size() - 1).getBaseLine();
    }

    public void mergeWithoutRowNumbers(TextChunk chunk) {
        mergeWithoutRowNumbers(chunk, false);
    }

    public void mergeWithoutRowNumbers(TextChunk chunk, boolean newLine) {
        if (newLine || lines.isEmpty()) {
            lines.add(new TextLine(chunk));
            rowNumbers.add(null);
        } else {
            lines.get(lines.size() - 1).add(chunk);
        }
        getBoundingBox().union(chunk.getBoundingBox());
    }

    public void mergeWithoutRowNumbers(TextLine line) {
        mergeWithoutRowNumbers(line, false);
    }

    public void mergeWithoutRowNumbers(TextLine line, boolean newLine) {
        if (newLine || lines.isEmpty()) {
            lines.add(line);
            rowNumbers.add(null);
        } else {
            TextLine lastLine = lines.get(lines.size() - 1);
            lastLine.add(line);
        }
        getBoundingBox().union(line.getBoundingBox());
    }

    public void mergeWithoutRowNumbers(TableCluster other) {
        List<TextLine> result = new ArrayList<>();

        int i = 0, j = 0;
        while (i < lines.size() && j < other.lines.size()) {
            TextLine line = lines.get(i);
            TextLine otherLine = other.lines.get(j);
            double baseLine = line.getBaseLine();
            double otherBaseLine = otherLine.getBaseLine();
            double tolerance = ONE_LINE_TOLERANCE_FACTOR * Math.max(line.getFontSize(), otherLine.getFontSize());

            if (baseLine > otherBaseLine + tolerance) {
                result.add(line);
                ++i;
            } else if (baseLine < otherBaseLine - tolerance) {
                result.add(otherLine);
                ++j;
            } else {
                TextLine unitedLine = new TextLine(line);
                unitedLine.add(otherLine);
                result.add(unitedLine);
                ++i;
                ++j;
            }
        }
        for (; i < lines.size(); ++i) {
            result.add(lines.get(i));
        }
        for (; j < other.lines.size(); ++j) {
            result.add(other.lines.get(j));
        }
        lines = result;
        rowNumbers = new ArrayList<>(Collections.nCopies(result.size(), null));
        getBoundingBox().union(other.getBoundingBox());
    }

    public List<TextLine> getLines() {
        return lines;
    }

    public TextLine getFirstLine() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(0);
    }

    public TextLine getLastLine() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(lines.size() - 1);
    }

    public TextChunk getFirstToken() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(0).getFirstTextChunk();
    }

    public TextChunk getLastToken() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(lines.size() - 1).getLastTextChunk();
    }

    public void setHeader(TableCluster header) {
        this.header = header;
        if (lastHeader == null) {
            lastHeader = header;
        }
    }

    public void setLastHeader(TableCluster lastHeader) {
        this.lastHeader = lastHeader;
    }

    public TableCluster getHeader() {
        return header;
    }

    public TableCluster getLastHeader() {
        return lastHeader;
    }

    public boolean isHeader() {
        return this == header;
    }

    @Override
    public String toString() {
        if (lines.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(lines.get(0).toString());
        for (int i = 1; i < lines.size(); ++i) {
            result.append('\n').append(lines.get(i));
        }
        return result.toString();
    }
}
