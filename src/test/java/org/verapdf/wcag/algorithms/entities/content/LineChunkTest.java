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
 * You should have received a copy of the MPL license along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities.content;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineChunkTest {

    @Test
    void createLineChunkHandlesZeroLengthLine() {
        LineChunk line = LineChunk.createLineChunk(1, 10, 20, 10, 20, 2, LineChunk.BUTT_CAP_STYLE);

        assertThat(line.getStartX()).isEqualTo(10);
        assertThat(line.getStartY()).isEqualTo(20);
        assertThat(line.getEndX()).isEqualTo(10);
        assertThat(line.getEndY()).isEqualTo(20);
        assertThat(line.getWidth()).isEqualTo(2);
    }

    @Test
    void createLineChunkHandlesZeroLengthAndZeroWidthLine() {
        LineChunk line = LineChunk.createLineChunk(1, 10, 20, 10, 20, 0, LineChunk.BUTT_CAP_STYLE);

        assertThat(line.getStartX()).isEqualTo(10);
        assertThat(line.getStartY()).isEqualTo(20);
        assertThat(line.getEndX()).isEqualTo(10);
        assertThat(line.getEndY()).isEqualTo(20);
        assertThat(line.getWidth()).isEqualTo(0);
    }
}
