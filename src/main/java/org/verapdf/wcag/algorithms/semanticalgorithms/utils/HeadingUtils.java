/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
