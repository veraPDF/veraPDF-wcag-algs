package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ClusterTableConsumer implements Consumer<INode> {

    private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeConsumer.class.getCanonicalName());

    private TableRecognitionArea recognitionArea;

    private List<INode> tables;
    private INode currentTable;

    public  ClusterTableConsumer() {
        recognitionArea = new TableRecognitionArea();

        tables = new ArrayList<>();
        currentTable = new SemanticGroupingNode(SemanticType.TABLE);
    }

    public List<INode> getTables() {
        return tables;
    }

    @Override
    public void accept(INode node) {

        if (node instanceof SemanticSpan) {

            SemanticSpan span = (SemanticSpan) node;
            for (TextChunk chunk : span.getLines()) {

                if (TextChunkUtils.isSpaceChunk(chunk)) {
                    continue;
                }

                recognitionArea.addTokenToRecognitionArea(chunk);

                if (recognitionArea.isComplete()) {
                    if (recognitionArea.isValid()) {
                        recognize();
                    }
                    currentTable = new SemanticGroupingNode(SemanticType.TABLE);
                    recognitionArea = new TableRecognitionArea();
                    accept(node);

                } else {
                    // node.setSemanticType(SemanticType.TABLE_CELL);
                    currentTable.getChildren().add(node);
                }
            }
        }

        if (node.isRoot()) {
            if (recognitionArea.isValid()) {
                recognize();
            }

            updateTreeWithRecognizedTables(node);
        }
    }

    private void recognize() {
        // System.out.println(recognitionArea);

        // TODO: start recognition by cluster algorithm

        // if recognition was successful
        tables.add(currentTable);
    }

    /**
     * main algorithm complexity: max{ O(t * h), O(N) },
     * where N - number of nodes, h - tree height, t - number of table cells
     * node info initialization: O(T * N), where T - number of tables.
     * The worst case is when all table roots are the same node - tree root
     */
    private void updateTreeWithRecognizedTables(INode root) {
        initTreeNodeInfo(root);

        for (INode table : tables) {
            INode tableRoot = null;
            for (INode node : new SemanticTree(table)) {
                if (!node.isLeaf()) {
                    continue;
                }

                node.setSemanticType(SemanticType.TABLE_CELL);
                node.setCorrectSemanticScore(1.0);
                node.getNodeInfo().counter = 1;
                if (tableRoot == null) {
                    tableRoot = node;
                }

                while (!node.isRoot()) {
                    INode parent = node.getParent();
                    NodeInfo parentInfo = parent.getNodeInfo();

                    parentInfo.counter++;
                    if (parentInfo.counter > 1) {
                        if (parentInfo.depth < tableRoot.getNodeInfo().depth) {
                            tableRoot = parent;
                        }
                        break;
                    }
                    node = parent;
                }
            }

            tableRoot.setSemanticType(SemanticType.TABLE);
            tableRoot.setCorrectSemanticScore(1.0);

            // clean up counters
            initTreeNodeInfo(tableRoot);
            while (!tableRoot.isRoot()) {
                tableRoot = tableRoot.getParent();
                tableRoot.getNodeInfo().counter = 0;
            }
        }
    }

    private void initTreeNodeInfo(INode root) {
        Stack<INode> nodeStack = new Stack<>();
        nodeStack.push(root);

        while (!nodeStack.isEmpty()) {
            INode node = nodeStack.pop();
            NodeInfo nodeInfo = node.getNodeInfo();

            nodeInfo.counter = 0;
            if (node.isRoot()) {
                nodeInfo.depth = 0;
            } else {
                nodeInfo.depth = node.getParent().getNodeInfo().depth + 1;
            }

            for (INode child : node.getChildren()) {
                nodeStack.push(child);
            }
        }
    }
}
