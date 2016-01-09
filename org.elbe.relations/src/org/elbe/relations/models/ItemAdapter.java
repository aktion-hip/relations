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
package org.elbe.relations.models;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.IItemFactory;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.IItemVisitor;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.bom.TextWithIcon;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.internal.utility.ItemModelHelper;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.exc.VException;

/**
 * Adapts instances of <code>IItem</code> or <code>IItemModel</code> and
 * decorates the created object with additional behavior.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ItemAdapter implements IItemModel, Comparable<Object> {
	private final IItemModel item;
	private List<IRelation> sources;
	private List<IRelation> targets;

	private LanguageService languageService;
	private Logger log;
	private final boolean hasDelegate;

	/**
	 * ItemAdapter constructor, adapting an <code>IItem</code>.
	 *
	 * @param inItem
	 *            {@link IItem} adaptee
	 * @param inImage
	 *            {@link Image}
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	public ItemAdapter(final IItem inItem, final Image inImage,
	        final IEclipseContext inContext) {
		item = new ItemAdapterDelegate(inItem, inImage, inContext);
		hasDelegate = true;
		initialize(inContext);
	}

	/**
	 * ItemAdapter constructor, adapting an <code>IItemModel</code>.
	 *
	 * @param inItem
	 *            {@link IItemModel} adaptee
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	public ItemAdapter(final IItemModel inItem,
	        final IEclipseContext inContext) {
		item = inItem;
		hasDelegate = false;
		initialize(inContext);
	}

	/**
	 * @param inContext
	 */
	private void initialize(final IEclipseContext inContext) {
		languageService = inContext.get(LanguageService.class);
		log = inContext.get(Logger.class);
		refresh();
	}

	/**
	 * Returns the original (i.e. unadapted) item.
	 *
	 * @return {@link IItem} the adaptee
	 */
	public IItem getItem() {
		return hasDelegate ? ((ItemAdapterDelegate) item).getItem() : item;
	}

	/**
	 * Refreshes the item for that it can be reused, i.e. re-initializes the
	 * sources and targets.
	 */
	public void refresh() {
		sources = new ArrayList<IRelation>();
		targets = new ArrayList<IRelation>();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getCmdID()
	 */
	@Override
	public long getID() throws VException {
		return getItem().getID();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getItemType()
	 */
	@Override
	public int getItemType() {
		return getItem().getItemType();
	}

	@Override
	public String getTitle() throws VException {
		return getItem().getTitle();
	}

	@Override
	public void addSource(final IRelation inRelation) {
		sources.add(inRelation);
	}

	@Override
	public void addTarget(final IRelation inRelation) {
		if (inRelation != null) {
			targets.add(inRelation);
		}
	}

	@Override
	public List<IRelation> getSources() {
		return sources;
	}

	@Override
	public List<IRelation> getTargets() {
		return targets;
	}

	/**
	 * @return int The type of the icon decorating the view.
	 * @see IItem#TERM as example.
	 */
	public int getIconType() {
		return getItem().getItemType() - 1;
	}

	/**
	 * We compare on the title.
	 */
	@Override
	public int compareTo(final Object inObject) {
		final Collator lCollator = languageService.getContentLanguage();
		lCollator.setStrength(Collator.SECONDARY);
		try {
			return lCollator.compare(getTitle(),
			        ((ItemAdapter) inObject).getTitle());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
			return 0;
		}
	}

	@Override
	public String toString() {
		try {
			return String.format("ItemAdapter '%s'", getTitle()); //$NON-NLS-1$
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
			return getClass().getName()
			        + RelationsMessages.getString("ItemAdapter.error"); //$NON-NLS-1$
		}
	}

	@Override
	public void visit(final IItemVisitor inVisitor) throws VException {
		item.visit(inVisitor);
	}

	/**
	 * Returns the wizard class to edit this item.
	 *
	 * @return Class&lt;? extends IItemEditWizard>
	 * @see IItemModel#getItemEditWizard()
	 */
	@Override
	public Class<? extends IItemEditWizard> getItemEditWizard() {
		return item.getItemEditWizard();
	}

	/**
	 * @see IItem#getItemDeleteAction()
	 */
	@Override
	public IAction getItemDeleteAction(final Logger inLog) {
		return item.getItemDeleteAction(inLog);
	}

	/**
	 * The item's icon.
	 *
	 * @return Image
	 */
	@Override
	public Image getImage() {
		return item.getImage();
	}

	/**
	 * Returns the light weight version of this item.
	 *
	 * @return ILightWeightItem
	 * @throws BOMException
	 */
	@Override
	public ILightWeightItem getLightWeight() throws BOMException {
		return getItem().getLightWeight();
	}

	/**
	 * Returns this item's unique ID.
	 *
	 * @return UniqueID
	 */
	public UniqueID getUniqueID() {
		try {
			return new UniqueID(getItem().getItemType(), getItem().getID());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		return null;
	}

	/**
	 * Saves the values of the title and text.
	 *
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @throws BOMException
	 */
	@Override
	public void saveTitleText(final String inTitle, final String inText)
	        throws BOMException {
		getItem().saveTitleText(inTitle, inText);
	}

	/**
	 * @see IItem#getCreated()
	 */
	@Override
	public String getCreated() throws VException {
		return getItem().getCreated();
	}

	@Override
	public void accept(final DomainObjectVisitor inVisitor) {
		getItem().accept(inVisitor);
	}

	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outHash = 1;
		outHash = lPrime * outHash
		        + ((getItem() == null) ? 0 : getItem().hashCode());
		return outHash;
	}

	/**
	 * @return <code>true</code> if ID and type are equal.
	 */
	@Override
	public boolean equals(final Object inObj) {
		if (this == inObj) {
			return true;
		}
		if (inObj == null) {
			return false;
		}
		if (getClass() != inObj.getClass()) {
			return false;
		}
		final ItemAdapter lOther = (ItemAdapter) inObj;
		if (getItem() == null) {
			if (lOther.getItem() != null) {
				return false;
			}
		} else if (!getItem().equals(lOther.getItem())) {
			return false;
		}
		return true;
	}

	// --- inner class

	/**
	 * Helper class to adapt an <code>IItem</code> instance.
	 */
	private static class ItemAdapterDelegate implements IItemModel {
		private final IItem item;
		private final Image image;
		private final IEclipseContext context;

		ItemAdapterDelegate(final IItem inItem, final Image inImage,
		        final IEclipseContext inContext) {
			item = inItem;
			image = inImage;
			context = inContext;
		}

		IItem getItem() {
			return item;
		}

		@Override
		public long getID() throws VException {
			return item.getID();
		}

		@Override
		public int getItemType() {
			return item.getItemType();
		}

		@Override
		public String getTitle() throws VException {
			return item.getTitle();
		}

		@Override
		public String getCreated() throws VException {
			return item.getCreated();
		}

		@Override
		public void visit(final IItemVisitor inVisitor) throws VException {
			if (getItemType() == IItem.TEXT) {
				final TextHelper lItem = new TextHelper((AbstractText) item,
				        context);
				lItem.visit(inVisitor);
			} else {
				item.visit(inVisitor);
			}
		}

		@Override
		public ILightWeightItem getLightWeight() throws BOMException {
			// nothing to do
			return null;
		}

		@Override
		public void saveTitleText(final String inTitle, final String inText)
		        throws BOMException {
			// nothing to do
		}

		@Override
		public void accept(final DomainObjectVisitor inVisitor) {
			// nothing to do
		}

		@Override
		public void addSource(final IRelation inRelation) {
			// nothing to do
		}

		@Override
		public void addTarget(final IRelation inRelation) {
			// nothing to do
		}

		@Override
		public List<IRelation> getSources() {
			// nothing to do
			return null;
		}

		@Override
		public List<IRelation> getTargets() {
			// nothing to do
			return null;
		}

		@Override
		public Image getImage() {
			return image;
		}

		@Override
		public Class<? extends IItemEditWizard> getItemEditWizard() {
			return ItemModelHelper.getItem(item.getItemType())
			        .getItemEditWizard();
		}

		@Override
		public IAction getItemDeleteAction(final Logger inLog) {
			return new IAction() {
				@Override
				public void run() throws BOMException {
					try {
						IItemFactory lHome = BOMHelper.getTermHome(); // default
						switch (getItemType()) {
						case IItem.TERM:
							lHome = BOMHelper.getTermHome();
							break;
						case IItem.TEXT:
							lHome = BOMHelper.getTextHome();
							break;
						case IItem.PERSON:
							lHome = BOMHelper.getPersonHome();
							break;
						}
						lHome.deleteItem(getID());
					}
					catch (final VException exc) {
						throw new BOMException(exc);
					}
				}
			};
		}
	}

	// ---

	/**
	 * Text item wrapper used for inspector visitor.
	 *
	 * @author Luthiger
	 */
	@SuppressWarnings("serial")
	private static class TextHelper extends TextWithIcon {
		public TextHelper(final AbstractText inText,
		        final IEclipseContext inContext) throws VException {
			super(inText, inContext);
		}
	}

}
