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
        map.put(SemanticType.FIGURE.getValue(), SemanticType.FIGURE);
        map.put(SemanticType.LINK.getValue(), SemanticType.LINK);
        map.put(SemanticType.ANNOT.getValue(), SemanticType.ANNOT);
        map.put(SemanticType.HEADING.getValue(), SemanticType.HEADING);
        map.put(SemanticType.NUMBER_HEADING.getValue(), SemanticType.NUMBER_HEADING);
        map.put(SemanticType.LIST.getValue(), SemanticType.LIST);
        map.put(SemanticType.LIST_ITEM.getValue(), SemanticType.LIST_ITEM);
        map.put(SemanticType.LIST_BODY.getValue(), SemanticType.LIST_BODY);
        map.put(SemanticType.LIST_LABEL.getValue(), SemanticType.LIST_LABEL);
        map.put(SemanticType.TABLE_OF_CONTENT.getValue(), SemanticType.TABLE_OF_CONTENT);
        map.put(SemanticType.TABLE_OF_CONTENT_ITEM.getValue(), SemanticType.TABLE_OF_CONTENT_ITEM);
        map.put(SemanticType.TABLE.getValue(), SemanticType.TABLE);
        map.put(SemanticType.TABLE_HEADERS.getValue(), SemanticType.TABLE_HEADERS);
        map.put(SemanticType.TABLE_BODY.getValue(), SemanticType.TABLE_BODY);
        map.put(SemanticType.TABLE_ROW.getValue(), SemanticType.TABLE_ROW);
        map.put(SemanticType.TABLE_HEADER.getValue(), SemanticType.TABLE_HEADER);
        map.put(SemanticType.TABLE_FOOTER.getValue(), SemanticType.TABLE_FOOTER);
        map.put(SemanticType.TABLE_CELL.getValue(), SemanticType.TABLE_CELL);
        map.put(SemanticType.CAPTION.getValue(), SemanticType.CAPTION);
        map.put(SemanticType.TITLE.getValue(), SemanticType.TITLE);
        map.put(SemanticType.NOTE.getValue(), SemanticType.NOTE);
        map.put(SemanticType.FOOTER.getValue(), SemanticType.FOOTER);
        map.put(SemanticType.HEADER.getValue(), SemanticType.HEADER);
        map.put(SemanticType.BLOCK_QUOTE.getValue(), SemanticType.BLOCK_QUOTE);
        map.put(SemanticType.CODE.getValue(), SemanticType.CODE);
        map.put(SemanticType.PART.getValue(), SemanticType.PART);
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
