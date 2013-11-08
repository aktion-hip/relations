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
package org.elbe.relations.internal.utility;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.style.StyledTextComponent;
import org.elbe.relations.internal.style.StyledTextComponent.BulletsState;

/**
 * Utility class for dialogs. This class manages the dirty state of all
 * registered widgets.
 * 
 * @author Luthiger Created on 16.09.2006
 */
public class CheckDirtyService {
	private final Collection<ICheckDirtyElement> toCheck;
	protected boolean isDirty = false;
	private AbstractEditForm editForm;

	/**
	 * CheckDirtyService constructor
	 * 
	 * @param inEditForm
	 *            AbstractEditForm
	 */
	public CheckDirtyService(final AbstractEditForm inEditForm) {
		toCheck = new ArrayList<ICheckDirtyElement>();
		editForm = inEditForm;
	}

	public void register(final RequiredText inText) {
		toCheck.add(new CheckDirtyRequiredText(inText, this));
	}

	public void register(final StyledTextComponent inText) {
		toCheck.add(new CheckDirtyStyledTextComponent(inText, this));
	}

	public void register(final StyledText inText) {
		toCheck.add(new CheckDirtyStyledText(inText, this));
	}

	public void register(final Text inText) {
		toCheck.add(new CheckDirtyText(inText, this));
	}

	public void register(final Combo inCombo) {
		toCheck.add(new CheckDirtyCombo(inCombo, this));
	}

	public void register(final Button inButton) {
		toCheck.add(new CheckDirtyButton(inButton, this));
	}

	public void freeze() {
		for (final ICheckDirtyElement lElement : toCheck) {
			lElement.freeze();
		}
		isDirty = false;
	}

	public void notifyDirtySwitch(final boolean inIsDirty) {
		if (isDirty ^ inIsDirty) {
			// if there is a switch in one element, check whether this was the
			// first clean or last dirty element
			final boolean lIsDirty = getDirty();
			if (isDirty ^ lIsDirty) {
				// the dialog's dirty status switched -> notification
				isDirty = lIsDirty;
				editForm.notifyDirtySwitch(isDirty);
			}
		}
	}

	/**
	 * @return boolean <code>true</code> if at least one element is dirty,
	 *         <code>false</code> else.
	 */
	protected boolean getDirty() {
		boolean outDirty = false;
		for (final ICheckDirtyElement lElement : toCheck) {
			outDirty |= lElement.getDirty();
		}
		return outDirty;
	}

	/**
	 * Returns the form's dirty status.
	 * 
	 * @return boolean <code>true</code> if at least on registered widget is
	 *         dirty.
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Resets the last frozen state.
	 */
	public void undo() {
		for (final ICheckDirtyElement lElement : toCheck) {
			lElement.undo();
		}
		isDirty = false;
	}

	public void dispose() {
		for (final ICheckDirtyElement lElement : toCheck) {
			lElement.dispose();
		}
		toCheck.clear();
		editForm = null;
	}

	// --- inner classes ---

	protected interface ICheckDirtyElement {
		void freeze();

		boolean getDirty();

		void undo();

		void dispose();
	}

	private abstract class AbstractCheckDirty {
		private CheckDirtyService service;
		protected boolean initialized = false;
		protected boolean isDirty = false;

		public AbstractCheckDirty(final CheckDirtyService inService) {
			service = inService;
		}

		public boolean getDirty() {
			return isDirty;
		}

		protected ModifyListener createModifyListener() {
			return new ModifyListener() {
				@Override
				public void modifyText(final ModifyEvent inEvent) {
					handleEvent();
				}
			};
		}

		protected FocusListener createFocusListener() {
			return new FocusListener() {
				@Override
				public void focusGained(final FocusEvent inEvent) {
					// we handle the event only when leaving
				}

				@Override
				public void focusLost(final FocusEvent inEvent) {
					handleEvent();
				}
			};
		}

		protected void handleEvent() {
			if (!initialized)
				return;

			final boolean lIsDirty = checkDirty();
			if (isDirty ^ lIsDirty) {
				// dirty status of the element switched -> notification
				isDirty = lIsDirty;
				service.notifyDirtySwitch(lIsDirty);
			}
		}

		public void dispose() {
			service = null;
		}

		abstract protected boolean checkDirty();
	}

	private class CheckDirtyRequiredText extends AbstractCheckDirty implements
			ICheckDirtyElement {
		private RequiredText text;
		private String originalValue = ""; //$NON-NLS-1$

		public CheckDirtyRequiredText(final RequiredText inText,
				final CheckDirtyService inService) {
			super(inService);
			text = inText;
			text.addModifyListener(createModifyListener());
		}

		@Override
		public void freeze() {
			originalValue = text.getText();
			initialized = true;
			isDirty = false;
		}

		@Override
		protected boolean checkDirty() {
			return !originalValue.equals(text.getText());
		}

		@Override
		public void undo() {
			text.setText(originalValue);
		}

		@Override
		public void dispose() {
			text = null;
			super.dispose();
		}
	}

	private class CheckDirtyStyledText extends AbstractCheckDirty implements
			ICheckDirtyElement {
		private final StyledText text;
		private String originalValue = ""; //$NON-NLS-1$

		public CheckDirtyStyledText(final StyledText inText,
				final CheckDirtyService inService) {
			super(inService);
			text = inText;
			text.addModifyListener(createModifyListener());
		}

		@Override
		public void freeze() {
			originalValue = text.getText();
			initialized = true;
			isDirty = false;
		}

		@Override
		public void undo() {
			text.setText(originalValue);
		}

		@Override
		protected boolean checkDirty() {
			return !originalValue.equals(text.getText());
		}
	}

	private class CheckDirtyStyledTextComponent extends AbstractCheckDirty
			implements ICheckDirtyElement {
		private StyledTextComponent text;
		private String originalValue = ""; //$NON-NLS-1$
		private StyleRange[] originalStyle = new StyleRange[0];
		private BulletsState bulletsState = null;

		public CheckDirtyStyledTextComponent(final StyledTextComponent inText,
				final CheckDirtyService inService) {
			super(inService);
			text = inText;
			text.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent inEvent) {
					handleEvent();
				}
			});
		}

		@Override
		public void freeze() {
			originalValue = text.getText();
			originalStyle = text.getStyleRanges();
			bulletsState = text.getBulletsState();
			initialized = true;
			isDirty = false;
		}

		@Override
		protected boolean checkDirty() {
			return !originalValue.equals(text.getText()) || styleChanged()
					|| bulletsChanged();
		}

		private boolean bulletsChanged() {
			if (bulletsState == null)
				return false;
			return !bulletsState.equals(text.getBulletsState());
		}

		private boolean styleChanged() {
			final StyleRange[] lRanges = text.getStyleRanges();
			if (originalStyle.length != lRanges.length)
				return true;
			for (int i = 0; i < lRanges.length; i++) {
				if (!lRanges[i].equals(originalStyle[i]))
					return true;
			}
			return false;
		}

		@Override
		public void undo() {
			text.setText(originalValue);
			text.setStyleRanges(originalStyle);
			bulletsState.undo(((StyledText) text.getControl()));
		}

		@Override
		public void dispose() {
			text = null;
			super.dispose();
		}
	}

	private class CheckDirtyText extends AbstractCheckDirty implements
			ICheckDirtyElement {
		private Text text;
		private String originalValue = ""; //$NON-NLS-1$

		public CheckDirtyText(final Text inText,
				final CheckDirtyService inService) {
			super(inService);
			text = inText;
			text.addModifyListener(createModifyListener());
		}

		@Override
		public void freeze() {
			originalValue = text.getText();
			initialized = true;
			isDirty = false;
		}

		@Override
		protected boolean checkDirty() {
			return !originalValue.equals(text.getText());
		}

		@Override
		public void undo() {
			text.setText(originalValue);
		}

		@Override
		public void dispose() {
			text = null;
			super.dispose();
		}
	}

	private class CheckDirtyCombo extends AbstractCheckDirty implements
			ICheckDirtyElement {
		private Combo combo;
		private int originalValue = 0;

		public CheckDirtyCombo(final Combo inCombo,
				final CheckDirtyService inService) {
			super(inService);
			combo = inCombo;
			combo.addModifyListener(createModifyListener());
		}

		@Override
		public void freeze() {
			originalValue = combo.getSelectionIndex();
			initialized = true;
			isDirty = false;
		}

		@Override
		protected boolean checkDirty() {
			return originalValue != combo.getSelectionIndex();
		}

		@Override
		public void undo() {
			combo.select(originalValue);
		}

		@Override
		public void dispose() {
			combo = null;
			super.dispose();
		}
	}

	private class CheckDirtyButton extends AbstractCheckDirty implements
			ICheckDirtyElement {
		private Button button;
		private boolean originalValue = false;

		public CheckDirtyButton(final Button inButton,
				final CheckDirtyService inService) {
			super(inService);
			button = inButton;
			button.addFocusListener(createFocusListener());
		}

		@Override
		public void freeze() {
			originalValue = button.getSelection();
			initialized = true;
			isDirty = false;
		}

		@Override
		protected boolean checkDirty() {
			return originalValue ^ button.getSelection();
		}

		@Override
		public void undo() {
			button.setSelection(originalValue);
		}

		@Override
		public void dispose() {
			button = null;
			super.dispose();
		}
	}

}
