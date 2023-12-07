package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.tables.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ClusterTableConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.TableBorderConsumer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeadingUtils {

    private static final Set<SemanticType> headingSemanticTypes = new HashSet<>(Arrays.asList(
            SemanticType.HEADING, SemanticType.NUMBER_HEADING));

    public static boolean isHeadings(Table table) {
        for (TableRow row : table.getRows()) {
            List<TableCell> cells = row.getCells();
            if (cells.size() != 2) {
                return false;
            }
            TableCell cell = cells.get(0);
            if (cell.isTextCell() && !cell.getContent().isEmpty()) {
                Set<INode> nodes = new HashSet<>();
                nodes.add(ClusterTableConsumer.getTableCellNode(cell));
                nodes.add(ClusterTableConsumer.getTableCellNode(cells.get(1)));
                INode node = TableBorderConsumer.findCommonParent(nodes);
                boolean isHeading = false;
                while (node != null) {
                    if (isInitialHeadingNode(node) || node.getInitialSemanticType() == SemanticType.TITLE) {
                        isHeading = true;
                        break;
                    }
                    node = node.getParent();
                }
                if (!isHeading) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isDetectedHeadingNode(INode node) {
        return headingSemanticTypes.contains(node.getSemanticType());
    }

    public static boolean isInitialHeadingNode(INode node) {
        return headingSemanticTypes.contains(node.getInitialSemanticType());
    }
}
