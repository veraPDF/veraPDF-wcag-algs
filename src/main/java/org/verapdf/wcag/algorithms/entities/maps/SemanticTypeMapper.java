package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.HashMap;
import java.util.Map;

public class SemanticTypeMapper {

    private static final Map<String, SemanticType> map = new HashMap<>();

    static {
        map.put(SemanticType.PARAGRAPH.getValue(), SemanticType.PARAGRAPH);
        map.put(SemanticType.SPAN.getValue(), SemanticType.SPAN);
        map.put(SemanticType.FORM.getValue(), SemanticType.FORM);
        map.put(SemanticType.LINK.getValue(), SemanticType.LINK);
        map.put(SemanticType.ANNOT.getValue(), SemanticType.ANNOT);
        map.put(SemanticType.HEADING.getValue(), SemanticType.HEADING);
        map.put(SemanticType.NUMBER_HEADING.getValue(), SemanticType.NUMBER_HEADING);
        map.put(SemanticType.LIST.getValue(), SemanticType.LIST);
        map.put(SemanticType.LIST_ITEM.getValue(), SemanticType.LIST_ITEM);
        map.put(SemanticType.LIST_BODY.getValue(), SemanticType.LIST_BODY);
        map.put(SemanticType.LIST_LABEL.getValue(), SemanticType.LIST_LABEL);
        map.put(SemanticType.TABLE.getValue(), SemanticType.TABLE);
        map.put(SemanticType.TABLE_HEADERS.getValue(), SemanticType.TABLE_HEADERS);
        map.put(SemanticType.TABLE_BODY.getValue(), SemanticType.TABLE_BODY);
        map.put(SemanticType.TABLE_ROW.getValue(), SemanticType.TABLE_ROW);
        map.put(SemanticType.TABLE_HEADER.getValue(), SemanticType.TABLE_HEADER);
        map.put(SemanticType.TABLE_FOOTER.getValue(), SemanticType.TABLE_FOOTER);
        map.put(SemanticType.TABLE_CELL.getValue(), SemanticType.TABLE_CELL);
        map.put(SemanticType.CAPTION.getValue(), SemanticType.CAPTION);
    }

    public static boolean containsType(String type) {
        return map.containsKey(type);
    }

    public static boolean containsType(SemanticType type) {
        return map.containsValue(type);
    }

    public static SemanticType getSemanticType(String type) {
        if (type == null) {
            return null;
        }
        if (type.matches("^H[1-9][0-9]*$")) {
            return SemanticType.NUMBER_HEADING;
        }
        return map.get(type);
    }

}
