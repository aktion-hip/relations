/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
package org.elbe.relations.data.bom;

import org.elbe.relations.data.utility.IItemVisitor;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.exc.VException;

/**
 * Generic interface for all Items (term, text, person).
 *
 * @author Benno Luthiger Created on 16.12.2005
 */
public interface IItem {
    // constants
    int RELATION = 0;
    int TERM = 1;
    int TEXT = 2;
    int PERSON = 3;

    /**
     * The item's unique ID.
     *
     * @return long
     * @throws VException
     */
    long getID() throws VException;

    /**
     * The type of this item.
     *
     * @return int
     */
    int getItemType();

    /**
     * Everey item has at least a title.
     *
     * @return String
     * @throws VException
     */
    String getTitle() throws VException;

    /**
     * Every item has some info concerning the time when it was created and last
     * modified.
     *
     * @return String Information in the form of <i>Created: 24.12.1961, 7:55;
     *         Modified: 12.10.2006, 19:12</i>.
     * @throws VException
     */
    String getCreated() throws VException;

    /**
     * Implementing the visitor pattern
     *
     * @param inVisitor
     *            IItemVisitor
     * @throws VException
     */
    void visit(IItemVisitor inVisitor) throws VException;

    /**
     * Returns the light weight version of this item.
     *
     * @return ILightWeightItem
     * @throws BOMException
     */
    ILightWeightItem getLightWeight() throws BOMException;

    /**
     * Saves the values of the title and text.
     *
     * @param inTitle
     *            String
     * @param inText
     *            String
     * @throws BOMException
     */
    void saveTitleText(String inTitle, String inText) throws BOMException;

    /**
     * Implements visitor pattern.
     *
     * @param inVisitor
     *            DomainObjectVisitor
     */
    void accept(DomainObjectVisitor inVisitor);

}
