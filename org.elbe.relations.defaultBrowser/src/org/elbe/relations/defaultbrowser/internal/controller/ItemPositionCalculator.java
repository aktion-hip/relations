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

import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * Calculates the position of the items on circular rings.
 * 
 * @author Benno Luthiger Created on 18.12.2005
 */
public class ItemPositionCalculator {
	private final long width;
	private final long height;
	private long radius;
	private double offset;
	private double delta;
	private long count;
	private boolean hasMore = true;

	/**
	 * ItemPositionCalculator constructor
	 * 
	 * @param inWidth
	 *            long Width of item
	 * @param inHeight
	 *            long Height of item
	 * @param inRadius
	 *            long Radius of circle
	 * @param inRemaining
	 *            long Number of remaining items to place on circle
	 */
	public ItemPositionCalculator(final long inWidth, final long inHeight,
	        final long inRadius, final long inRemaining) {
		super();
		width = inWidth;
		height = inHeight;
		radius = inRadius;
		init(radius, inRemaining);
	}

	/**
	 * Recalculates with new values for the remaining items.
	 * 
	 * @param inRadius
	 *            long
	 * @param inRemaining
	 *            long
	 */
	public void recalculate(final long inRadius, final long inRemaining) {
		radius = inRadius;
		init(radius, inRemaining);
	}

	private void init(final long inRadius, final long inRemaining) {
		offset = Math.asin(width / (2.0 * inRadius));
		delta = Math.acos(Math.cos(offset) - (height * 1.0 / inRadius))
		        - offset;
		count = (long) (((2 * Math.PI) - (4 * offset)) / delta);
		count = (count % 2 != 0 ? count + 1 : count);
		offset = (2 * Math.PI - (count - 2) * delta) / 4;

		// calculate the angle between each item
		if (inRemaining <= count) {
			// if there remain less items than can be placed on the circle
			// we place all remaining items well distributed on the circle
			offset = (inRemaining < 3 ? Math.PI / 2
			        : (2 * Math.PI - (inRemaining - 2) * delta) / 4);
			count = inRemaining;
			hasMore = false;
		}
	}

	/**
	 * Returns calculated half of the number of items on the circle. Rounded to
	 * the next lower <code>long</code>.
	 * 
	 * @return long
	 */
	private long getHalfOfCount() {
		return count / 2;
	}

	/**
	 * Returns the calculated number of items on the circle.
	 * 
	 * @return long
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Returns the offsetting angle at the start.
	 * 
	 * @return double
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * Returns the calculated angle between the items.
	 * 
	 * @return double
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * Returns <code>true</code> if there are more items to place on subsequent
	 * circles.
	 * 
	 * @return boolean
	 */
	public boolean hasMore() {
		return hasMore;
	}

	/**
	 * Returns a list of positions.
	 * 
	 * @return List<PrecisionPoint> of Point
	 */
	public List<PrecisionPoint> getPositions() {
		final List<PrecisionPoint> outPositions = new Vector<PrecisionPoint>();
		for (int i = 0; i < count; i++) {
			if (i < getHalfOfCount()) {
				final double lAngle = offset + i * delta;
				outPositions.add(new PrecisionPoint(radius * Math.sin(lAngle),
				        -1 * radius * Math.cos(lAngle)));
			} else {
				final double lAngle = Math.PI + offset + (i - getHalfOfCount())
				        * delta;
				outPositions.add(new PrecisionPoint(radius * Math.sin(lAngle),
				        -1 * radius * Math.cos(lAngle)));
			}
		}
		return outPositions;
	}
}
