package littleDirtySecret;

import com.google.gson.Gson;
import logiusAlgorithms.implementation.SemanticNode;
import logiusAlgorithms.implementation.SemanticTextChunk;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class __JsonToPdfTree {
    public static SemanticNode getPdfTreeRoot(String jsonFileName) {
        Gson gson = new Gson();
        Reader reader;
        __JsonNode jsonRoot;
        SemanticNode pdfRoot = null;
        try {
            reader = Files.newBufferedReader(Paths.get(jsonFileName));
            jsonRoot = gson.fromJson(reader, __JsonNode.class);
            pdfRoot = getPdfNode(jsonRoot);
        } catch (Exception e) {}

        return pdfRoot;
    }

    private static SemanticNode getPdfNode(__JsonNode jsonNode) {
        if (jsonNode == null)
            return null;

        SemanticNode node;
        String pdfType = jsonNode.type;

        switch (pdfType) {
            case "PDTextChunk":
                node = new SemanticTextChunk(jsonNode.value,
                        jsonNode.fontName,
                        jsonNode.fontSize,
                        jsonNode.fontWeight,
                        jsonNode.italicAngle,
                        jsonNode.color,
                        jsonNode.boundingBox,
                        jsonNode.baseLine,
                        jsonNode.pageNumber);
                break;
            default:
                node = new SemanticNode(pdfType);
                break;
        }

        if (jsonNode.children != null) {
            node.initChildren();
            for (__JsonNode child : jsonNode.children) {
                node.addChild(getPdfNode(child));
            }
        }

        return node;
    }
}
