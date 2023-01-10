package org.verapdf.wcag.algorithms.entities.maps;

import org.verapdf.wcag.algorithms.entities.INode;

import java.util.HashMap;
import java.util.Map;

public class ObjectKeyMapper {
    private final Map<Integer, INode> objectKeyToNodeMap;

    public ObjectKeyMapper() {
        objectKeyToNodeMap = new HashMap<>();
    }

    public INode get(Integer key) {
        if (key == null) {
            return null;
        }
        return objectKeyToNodeMap.get(key);
    }

    public void put(Integer key, INode node) {
        if (key == null) {
            return;
        }
        objectKeyToNodeMap.put(key, node);
    }
}
