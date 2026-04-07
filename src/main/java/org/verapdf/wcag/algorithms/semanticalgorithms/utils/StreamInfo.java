package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import java.util.ArrayList;
import java.util.List;

public class StreamInfo implements Comparable<StreamInfo> {
    
    private final int operatorIndex;
    private final String xObjectName;
    private int startIndex;
    private int endIndex;
    private int length;
    private Integer mcid;
    
    public StreamInfo(int operatorIndex, String xObjectName) {
        this.operatorIndex = operatorIndex;
        this.xObjectName = xObjectName;
    }

    public StreamInfo(int operatorIndex, String xObjectName, int startIndex, int endIndex) {
        this(operatorIndex, xObjectName, startIndex, endIndex, endIndex, null);
    }

    public StreamInfo(int operatorIndex, String xObjectName, int startIndex, int endIndex, int length, Integer mcid) {
        this.operatorIndex = operatorIndex;
        this.xObjectName = xObjectName;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.length = length;
        this.mcid = mcid;
    }


    public StreamInfo(StreamInfo streamInfo) {
        this(streamInfo.operatorIndex, streamInfo.xObjectName, streamInfo.startIndex, streamInfo.endIndex, 
                streamInfo.length, streamInfo.mcid);
    }

    public int getOperatorIndex() {
        return operatorIndex;
    }

    public String getXObjectName() {
        return xObjectName;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Integer getMcid() {
        return mcid;
    }

    public void setMcid(Integer mcid) {
        this.mcid = mcid;
    }

    public static void updateStreamInfos(List<StreamInfo> streamInfos, int length, int start, int end) {
        if (streamInfos.isEmpty()) {
            return;
        }
        int gap = start;
        int index = 0;
        while (gap > 0) {
            StreamInfo streamInfo = streamInfos.get(index);
            if (streamInfo.endIndex - streamInfo.startIndex >= gap) {
                streamInfo.startIndex += gap;
                gap = 0;
            } else {
                gap -= (streamInfo.endIndex - streamInfo.startIndex);
                streamInfo.startIndex = streamInfo.endIndex;
            }
            index++;
        }
        gap = length - end;
        index = streamInfos.size() - 1;
        while (gap > 0) {
            StreamInfo streamInfo = streamInfos.get(index);
            if (streamInfo.endIndex - streamInfo.startIndex >= gap) {
                streamInfo.endIndex -= gap;
                gap = 0;
            } else {
                gap -= (streamInfo.endIndex - streamInfo.startIndex);
                streamInfo.startIndex = streamInfo.endIndex;
            }
            index--;
        }
        streamInfos.removeIf(streamInfo -> streamInfo.startIndex == streamInfo.endIndex);
    }

    public static void updateStreamInfos(List<StreamInfo> streamInfos, List<Integer> extraCharIndexes) {
        if (streamInfos == null || extraCharIndexes.isEmpty()) {
            return;
        }
        List<StreamInfo> result = new ArrayList<>();
        int currentGlobalIndex = 0;
        int removeIndexPos = 0;
        for (StreamInfo streamInfo : streamInfos) {
            int length = streamInfo.getEndIndex() - streamInfo.getStartIndex();
            int localStart = 0;
            while (removeIndexPos < extraCharIndexes.size()) {
                int removeIndex = extraCharIndexes.get(removeIndexPos);
                if (removeIndex < currentGlobalIndex) {
                    removeIndexPos++;
                    continue;
                }
                if (removeIndex >= currentGlobalIndex + length) {
                    break;
                }
                int localRemoveIndex = removeIndex - currentGlobalIndex;
                if (localRemoveIndex > localStart) {
                    int newStart = streamInfo.getStartIndex() + localStart;
                    int newEnd = streamInfo.getStartIndex() + localRemoveIndex;
                    StreamInfo newStreamInfo = new StreamInfo(streamInfo);
                    newStreamInfo.startIndex = newStart;
                    newStreamInfo.endIndex = newEnd;
                    result.add(newStreamInfo);
                }

                localStart = localRemoveIndex + 1;
                removeIndexPos++;
            }
            if (localStart < length) {
                int newStart = streamInfo.getStartIndex() + localStart;
                int newEnd = streamInfo.getEndIndex();
                StreamInfo newStreamInfo = new StreamInfo(streamInfo);
                newStreamInfo.startIndex = newStart;
                newStreamInfo.endIndex = newEnd;
                result.add(newStreamInfo);
            }
            currentGlobalIndex += length;
        }
        streamInfos.clear();
        streamInfos.addAll(result);
        streamInfos.removeIf(streamInfo -> streamInfo.startIndex == streamInfo.endIndex);
    }

    @Override
    public int compareTo(StreamInfo o) {
        if (operatorIndex != o.operatorIndex) {
            return operatorIndex - o.operatorIndex;
        }
        if (startIndex != o.startIndex) {
            return startIndex - o.startIndex;
        }
        return 0;
    }
}
