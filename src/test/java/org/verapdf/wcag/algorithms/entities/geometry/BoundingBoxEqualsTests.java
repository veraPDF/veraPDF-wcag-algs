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
package org.verapdf.wcag.algorithms.entities.geometry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class BoundingBoxEqualsTests {

    @Test
    public void testEqualBoxesAreEqual() {
        BoundingBox first = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox second = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        Assertions.assertEquals(first, second);
    }

    @Test
    public void testBoxesDifferingOnlyInTopYAreNotEqual() {
        BoundingBox first = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox second = new BoundingBox(0, 0.0, 0.0, 100.0, 70.0);
        Assertions.assertNotEquals(first, second);
    }

    /**
     * equals() and hashCode() must agree: equal boxes have equal hash codes,
     * which is what every HashSet/HashMap of BoundingBox-keyed entities relies on.
     */
    @Test
    public void testEqualBoxesShareHashCode() {
        BoundingBox first = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox second = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void testEqualsAgreesWithAreSameBoundingBoxes() {
        BoundingBox first = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox same = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox other = new BoundingBox(0, 0.0, 0.0, 100.0, 70.0);
        Assertions.assertEquals(BoundingBox.areSameBoundingBoxes(first, same), first.equals(same));
        Assertions.assertEquals(BoundingBox.areSameBoundingBoxes(first, other), first.equals(other));
    }

    @Test
    public void testHashSetDeduplicatesEqualBoxes() {
        Set<BoundingBox> boxes = new HashSet<>();
        boxes.add(new BoundingBox(0, 0.0, 0.0, 100.0, 50.0));
        boxes.add(new BoundingBox(0, 0.0, 0.0, 100.0, 50.0));
        Assertions.assertEquals(1, boxes.size());
    }

    @Test
    public void testBoxesOnDifferentPagesAreNotEqual() {
        BoundingBox first = new BoundingBox(0, 0.0, 0.0, 100.0, 50.0);
        BoundingBox second = new BoundingBox(1, 0.0, 0.0, 100.0, 50.0);
        Assertions.assertNotEquals(first, second);
    }
}
