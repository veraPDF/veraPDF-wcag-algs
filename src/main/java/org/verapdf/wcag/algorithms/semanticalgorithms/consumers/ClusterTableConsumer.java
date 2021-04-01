package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticGroupingNode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.tables.TableRecognitionArea;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.util.ArrayList;
import java.util.List;
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
        }
    }

    private void recognize() {
        // TODO: start recognition by cluster algorithm

        // if recognition is successful
        tables.add(currentTable);

//        System.out.println(recognitionArea);
    }
}
