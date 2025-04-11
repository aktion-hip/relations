/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.defaultbrowser.internal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.junit.jupiter.api.Test;

/**
 * @author lbenno
 */
public class ItemPositionCalculatorTest {
    private final static int HEIGHT = 30;
    private final static int WIDTH = 120;
    private final static int RADIUS = 200;

    @Test
    public void testDo() {
        ItemPositionCalculator lCalculator = new ItemPositionCalculator(WIDTH,
                HEIGHT, RADIUS, 2);
        assertEquals(2, lCalculator.getCount());
        assertEquals(1.57079, lCalculator.getOffset(), 0.001);
        assertEquals(0.33221, lCalculator.getDelta(), 0.001);
        assertFalse(lCalculator.hasMore());

        lCalculator = new ItemPositionCalculator(WIDTH, HEIGHT, RADIUS, 5);
        assertEquals(5, lCalculator.getCount());
        assertEquals(1.3216357, lCalculator.getOffset(), 0.001);
        assertEquals(0.3322140, lCalculator.getDelta(), 0.001);
        assertFalse(lCalculator.hasMore());

        lCalculator = new ItemPositionCalculator(WIDTH, HEIGHT, RADIUS, 25);
        assertEquals(16, lCalculator.getCount());
        assertEquals(0.40804704, lCalculator.getOffset(), 0.001);
        assertEquals(0.33221408, lCalculator.getDelta(), 0.001);
        assertTrue(lCalculator.hasMore());

        final PrecisionPoint[] lExpected = new PrecisionPoint[] {
                new PrecisionPoint(79, -183), new PrecisionPoint(134, -147),
                new PrecisionPoint(175, -95), new PrecisionPoint(197, -33),
                new PrecisionPoint(197, 33), new PrecisionPoint(175, 95),
                new PrecisionPoint(134, 147), new PrecisionPoint(79, 183),
                new PrecisionPoint(-79, 183), new PrecisionPoint(-134, 147),
                new PrecisionPoint(-175, 95), new PrecisionPoint(-197, 33),
                new PrecisionPoint(-197, -33), new PrecisionPoint(-175, -95),
                new PrecisionPoint(-134, -147), new PrecisionPoint(-79, -183) };
        final List<PrecisionPoint> lPoints = lCalculator.getPositions();
        assertEquals(lExpected.length, lPoints.size());
        for (int i = 0; i < lExpected.length; i++) {
            final PrecisionPoint lPoint = lPoints.get(i);
            assertEquals(lExpected[i].preciseX(), lPoint.preciseX(), 1);
            assertEquals(lExpected[i].preciseY(), lPoint.preciseY(), 1);
        }
    }

}
