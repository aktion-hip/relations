/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
package org.elbe.relations.internal.utility;

import java.io.Closeable;
import java.io.IOException;

/** ItemVisitor used in <code>InspectorView</code>.
 *
 * @author Luthiger */
public class InspectorViewVisitor extends AbstractItemVisitor implements Closeable {
    private boolean isTitleEditable;
    private boolean isTextEditable;
    private String realText;

    @Override
    public void setSubTitle(final String subTitle) {
        // not used
    }

    @Override
    public String getSubTitle() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void setTitleEditable(final boolean titleEditable) {
        this.isTitleEditable = titleEditable;
    }

    public boolean isTitleEditable() {
        return this.isTitleEditable;
    }

    @Override
    public void setTextEditable(final boolean textEditable) {
        this.isTextEditable = textEditable;
    }

    public boolean isTextEditable() {
        return this.isTextEditable;
    }

    /**
     * @return String the real text field's content as is.
     */
    @Override
    public String getRealText() {
        return this.realText == null ? getText() : this.realText;
    }

    @Override
    public void setRealText(final String text) {
        this.realText = text;
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }

}
