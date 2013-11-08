package org.elbe.relations.internal.utility;

import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.elbe.relations.RelationsImages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.utility.ItemCollator;
import org.elbe.relations.internal.models.ItemWithIcon;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * Helper class to collect all related items.
 * 
 * @author Benno Luthiger
 */
public class RelatedItemHelper {

	/**
	 * Retrieves all related terms.
	 * 
	 * @param inItem
	 *            IItem The item the terms are related to.
	 * @return Collection<ItemWithIcon>
	 * @throws VException
	 * @throws SQLException
	 */
	public static Collection<ItemWithIcon> getRelatedTerms(final IItem inItem)
			throws VException, SQLException {
		final Collection<ItemWithIcon> outRelated = getItems(BOMHelper
				.getJoinRelatedTerm1Home().select(getKey1(inItem, IItem.TERM)),
				new TermWrapper());
		outRelated.addAll(getItems(
				BOMHelper.getJoinRelatedTerm2Home().select(
						getKey2(inItem, IItem.TERM)), new TermWrapper()));
		return outRelated;
	}

	/**
	 * Retrieves all related texts.
	 * 
	 * @param inItem
	 *            IItem The item the texts are related to.
	 * @return Collection<IItemModel>
	 * @throws VException
	 * @throws SQLException
	 */
	public static Collection<ItemWithIcon> getRelatedTexts(final IItem inItem)
			throws VException, SQLException {
		final Collection<ItemWithIcon> outRelated = getItems(BOMHelper
				.getJoinRelatedText1Home().select(getKey1(inItem, IItem.TEXT)),
				new TextWrapper());
		outRelated.addAll(getItems(
				BOMHelper.getJoinRelatedText2Home().select(
						getKey2(inItem, IItem.TEXT)), new TextWrapper()));
		return outRelated;
	}

	/**
	 * Retrieves all related persons.
	 * 
	 * @param inItem
	 *            IItem The item the persons are related to.
	 * @return Collection<IItemModel>
	 * @throws VException
	 * @throws SQLException
	 */
	public static Collection<ItemWithIcon> getRelatedPersons(final IItem inItem)
			throws VException, SQLException {
		final Collection<ItemWithIcon> outRelated = getItems(
				BOMHelper.getJoinRelatedPerson1Home().select(
						getKey1(inItem, IItem.PERSON)), new PersonWrapper());
		outRelated.addAll(getItems(BOMHelper.getJoinRelatedPerson2Home()
				.select(getKey2(inItem, IItem.PERSON)), new PersonWrapper()));
		return outRelated;
	}

	/**
	 * Retrieves all related items of any type.
	 * 
	 * @param inItem
	 *            The item whose related will be retrieved.
	 * @return Collection<IItem> of <code>IItem</code>
	 * @throws VException
	 * @throws SQLException
	 */
	public static Collection<ItemWithIcon> getRelatedItems(final IItem inItem)
			throws VException, SQLException {
		// term
		final List<ItemWithIcon> outRelated = getItems(BOMHelper
				.getJoinRelatedTerm1Home().select(getKey1(inItem, IItem.TERM)),
				new TermWrapper());
		outRelated.addAll(getItems(
				BOMHelper.getJoinRelatedTerm2Home().select(
						getKey2(inItem, IItem.TERM)), new TermWrapper()));
		// text
		outRelated.addAll(getItems(
				BOMHelper.getJoinRelatedText1Home().select(
						getKey1(inItem, IItem.TEXT)), new TextWrapper()));
		outRelated.addAll(getItems(
				BOMHelper.getJoinRelatedText2Home().select(
						getKey2(inItem, IItem.TEXT)), new TextWrapper()));
		// person
		outRelated.addAll(getItems(BOMHelper.getJoinRelatedPerson1Home()
				.select(getKey1(inItem, IItem.PERSON)), new PersonWrapper()));
		outRelated.addAll(getItems(BOMHelper.getJoinRelatedPerson2Home()
				.select(getKey2(inItem, IItem.PERSON)), new PersonWrapper()));
		// sort
		final Collator lCollator = new ItemCollator();
		lCollator.setStrength(Collator.SECONDARY);
		Collections.sort(outRelated, lCollator);
		return outRelated;
	}

	private static KeyObject getKey1(final IItem inItem, final int inType)
			throws VException {
		final KeyObject outKey = new KeyObjectImpl();
		outKey.setValue(RelationHome.KEY_ITEM2, new Long(inItem.getID()));
		outKey.setValue(RelationHome.KEY_TYPE2,
				new Integer(inItem.getItemType()));
		outKey.setValue(RelationHome.KEY_TYPE1, new Integer(inType));
		return outKey;
	}

	private static KeyObject getKey2(final IItem inItem, final int inType)
			throws VException {
		final KeyObject outKey = new KeyObjectImpl();
		outKey.setValue(RelationHome.KEY_ITEM1, new Long(inItem.getID()));
		outKey.setValue(RelationHome.KEY_TYPE1,
				new Integer(inItem.getItemType()));
		outKey.setValue(RelationHome.KEY_TYPE2, new Integer(inType));
		return outKey;
	}

	private static List<ItemWithIcon> getItems(final QueryResult inResult,
			final IWrapper inWrapper) throws SQLException, VException {
		final List<ItemWithIcon> outItems = new ArrayList<ItemWithIcon>();
		while (inResult.hasMoreElements()) {
			outItems.add(inWrapper.wrap((IItem) inResult.next()));
		}
		return outItems;
	}

	private interface IWrapper {
		ItemWithIcon wrap(IItem inModel);
	}

	private static class TermWrapper implements IWrapper {
		@Override
		public ItemWithIcon wrap(final IItem inModel) {
			return new ItemWithIcon(inModel, RelationsImages.TERM.getImage());
		}
	}

	private static class PersonWrapper implements IWrapper {
		@Override
		public ItemWithIcon wrap(final IItem inModel) {
			return new ItemWithIcon(inModel, RelationsImages.PERSON.getImage());
		}
	}

	private static class TextWrapper implements IWrapper {
		@Override
		public ItemWithIcon wrap(final IItem inModel) {
			return new ItemWithIcon(inModel, RelationsImages.TEXT.getImage());
		}
	}

}
