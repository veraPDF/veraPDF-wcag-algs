package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.HashMap;
import java.util.Map;

public class SemanticTypeMapper {

    private static final Map<String, SemanticType> map = new HashMap<>();

    static {
        map.put(SemanticType.PARAGRAPH.getValue(), SemanticType.PARAGRAPH);
        map.put(SemanticType.SPAN.getValue(), SemanticType.SPAN);
    }

    public static boolean containsType(String type) {
        return map.containsKey(type);
    }

    public static SemanticType getSemanticType(String type) {
        return map.get(type);
    }

}
