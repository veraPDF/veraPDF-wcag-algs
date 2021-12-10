package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.SemanticImageNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.entities.tables.TableToken;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorderCell;

public class TableBorderConsumer {

    private final AccumulatedNodeMapper accumulatedNodeMapper;
    private final TableBordersCollection tableBorders;

    public TableBorderConsumer(TableBordersCollection tableBorders, AccumulatedNodeMapper accumulatedNodeMapper) {
        this.tableBorders = tableBorders;
        this.accumulatedNodeMapper = accumulatedNodeMapper;
    }

    public void recognizeTables(ITree tree) {
        for (INode node : tree) {
            if (node.getChildren().isEmpty()) {
                if (node instanceof SemanticTextNode) {
                    SemanticTextNode textNode = (SemanticTextNode) node;
                    for (TextLine line : textNode.getLines()) {
                        for (TextChunk chunk : line.getTextChunks()) {
                            add(new TableToken(chunk, node));
                        }
                    }
                } else if ((node instanceof SemanticImageNode)) {
                    SemanticImageNode imageNode = (SemanticImageNode) node;
                    add(new TableToken(imageNode.getImage(), imageNode));
                }
            }
        }
    }

    private void add(TableToken token) {
        TableBorder tableBorder = tableBorders.getTableBorder(token.getBoundingBox());
        if (tableBorder != null) {
            TableBorderCell tableBorderCell = tableBorder.getTableBorderCell(token.getBoundingBox());
            if (tableBorderCell != null) {
                tableBorderCell.addContent(token);
            }
        }
    }
}
