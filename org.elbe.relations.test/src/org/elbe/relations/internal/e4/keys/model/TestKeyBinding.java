/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.e4.keys.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;

/**
 * <code>MKeyBinding</code> for testing purpose.
 *
 * @author lbenno
 */
public class TestKeyBinding implements MKeyBinding {

	private String elementId;
	private Map<String, Object> transientData;
	private MCommand command;

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String value) {
		elementId = value;
	}

	@Override
	public Map<String, String> getPersistedState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContributorURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContributorURI(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getTransientData() {
		if (transientData == null) {
			transientData = new HashMap<String, Object>();
		}
		return transientData;
	}

	@Override
	public String getKeySequence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKeySequence(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public MCommand getCommand() {
		return command;
	}

	@Override
	public void setCommand(MCommand value) {
		command = value;
	}

	@Override
	public List<MParameter> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
