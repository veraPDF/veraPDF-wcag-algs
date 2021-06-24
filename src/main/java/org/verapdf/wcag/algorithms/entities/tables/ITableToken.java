package org.verapdf.wcag.algorithms.entities.tables;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.content.IChunk;

public interface ITableToken extends IChunk {

    INode getNode();

    double getBaseLine();
    double getHeight();
}
