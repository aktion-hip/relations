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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.models.ItemWithIcon;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.utility.RelatedItemHelper;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit Plug-in test
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class CentralAssociationsModelTest {
	private static DataHouseKeeper data;

	@Mock
	private Device device;
	@Mock
	private IEventBroker eventBroker;
	@Mock
	private Logger log;
	@Mock
	private IDataService dataService;
	@Mock
	private IBrowserManager browserManager;

	private Image image;
	private LanguageService languages;
	private IEclipseContext context;

	private AbstractTerm term1;
	private AbstractTerm term2;
	private AbstractPerson person1;
	private AbstractPerson person2;
	private AbstractPerson person3;

	private IAssociationsModel model;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
		image = new Image(device, 1, 1);
		languages = new LanguageService(Locale.ENGLISH.getLanguage());

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IEventBroker.class, eventBroker);
		context.set(LanguageService.class, languages);
		context.set(IDataService.class, dataService);
		context.set(IBrowserManager.class, browserManager);

		term1 = data.createTerm("term 1");
		term2 = data.createTerm("term 2");
		person1 = data.createPerson("person1", "1");
		person2 = data.createPerson("person2", "2");
		person3 = data.createPerson("person3", "3");
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testSaveChanges() throws Exception {
		final int lType = IItem.PERSON;
		final UniqueID lTerm2 = new UniqueID(term2.getItemType(), term2.getID());
		final UniqueID lPerson1 = new UniqueID(lType, person1.getID());
		final UniqueID lPerson2 = new UniqueID(lType, person2.getID());
		final UniqueID lPerson3 = new UniqueID(lType, person3.getID());

		when(dataService.retrieveItem(lPerson1)).thenReturn(
		        new ItemAdapter(person1, image, context));
		when(dataService.retrieveItem(lPerson2)).thenReturn(
		        new ItemAdapter(person2, image, context));
		when(dataService.retrieveItem(lPerson3)).thenReturn(
		        new ItemAdapter(person3, image, context));
		when(dataService.retrieveItem(lTerm2)).thenReturn(
		        new ItemAdapter(term2, image, context));

		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		final RelationHome lHome = data.getRelationHome();
		assertEquals(0, lHome.getCount());

		assertEquals("Number of associated 1", 0, model.getElements().length);

		UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
		model.addAssociations(lAdd);
		assertEquals("Number of associated 2", 3, model.getElements().length);

		UniqueID[] lRemove = new UniqueID[] { lPerson2 };
		model.removeAssociations(lRemove);
		assertEquals("Number of associated 3", 2, model.getElements().length);

		lAdd = new UniqueID[] { lTerm2 };
		model.addAssociations(lAdd);
		assertEquals("Number of associated 4", 3, model.getElements().length);

		model.saveChanges();
		assertEquals("Count relations 1", 3, lHome.getCount());
		assertRelations("Relations 1", lHome.getRelations(term1),
		        new UniqueID[] { lPerson1, lPerson3, lTerm2 });

		lRemove = new UniqueID[] { lPerson1 };
		model.removeAssociations(lRemove);
		model.saveChanges();
		assertEquals("Count relations 2", 2, lHome.getCount());
		assertRelations("Relations 2", lHome.getRelations(term1),
		        new UniqueID[] { lPerson3, lTerm2 });

		lAdd = new UniqueID[] { lPerson1, lPerson2 };
		model.addAssociations(lAdd);
		model.saveChanges();
		assertEquals("Count relations 3", 4, lHome.getCount());
		assertRelations("Relations 3", lHome.getRelations(term1),
		        new UniqueID[] { lPerson1, lPerson2, lPerson3, lTerm2 });
	}

	private void assertRelations(final String inComment,
	        final QueryResult inResult, final UniqueID[] inExpected)
	        throws VException, SQLException {
		while (inResult.hasMoreElements()) {
			final DomainObject lRelation = (DomainObject) inResult.next();
			assertRelation(inComment,
			        new UniqueID(lRelation.get(RelationHome.KEY_TYPE1) + ":"
			                + lRelation.get(RelationHome.KEY_ITEM1)),
			        new UniqueID(lRelation.get(RelationHome.KEY_TYPE2) + ":"
			                + lRelation.get(RelationHome.KEY_ITEM2)),
			        inExpected);
		}
	}

	private void assertRelation(final String inComment, final UniqueID inItem1,
	        final UniqueID inItem2, final UniqueID[] inExpected) {
		for (int i = 0; i < inExpected.length; i++) {
			if (inExpected[i].equals(inItem1)) {
				return;
			}
			if (inExpected[i].equals(inItem2)) {
				return;
			}
		}
		fail(inComment);
	}

	@Test
	public void testUndoChanges() throws Exception {
		final int lType = IItem.PERSON;
		final UniqueID lTerm2 = new UniqueID(term2.getItemType(), term2.getID());
		final UniqueID lPerson1 = new UniqueID(lType, person1.getID());
		final UniqueID lPerson2 = new UniqueID(lType, person2.getID());
		final UniqueID lPerson3 = new UniqueID(lType, person3.getID());

		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		when(dataService.retrieveItem(lPerson1)).thenReturn(
		        new ItemAdapter(person1, image, context));
		when(dataService.retrieveItem(lPerson2)).thenReturn(
		        new ItemAdapter(person2, image, context));
		when(dataService.retrieveItem(lPerson3)).thenReturn(
		        new ItemAdapter(person3, image, context));
		when(dataService.retrieveItem(lTerm2)).thenReturn(
		        new ItemAdapter(term2, image, context));

		final RelationHome lHome = data.getRelationHome();
		assertEquals(0, model.getElements().length);

		final UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
		model.addAssociations(lAdd);
		assertEquals(3, model.getElements().length);

		model.saveChanges();
		assertEquals(3, lHome.getCount());
		assertRelations("Relations 1", lHome.getRelations(term1), lAdd);

		when(dataService.retrieveItem(lPerson2)).thenReturn(
		        getRelatedPersons(term1).get(1));
		final UniqueID[] lRemove = new UniqueID[] { lPerson2 };
		model.removeAssociations(lRemove);
		assertEquals(2, model.getElements().length);
		model.undoChanges();
		assertEquals(3, model.getElements().length);
		assertRelations("Relations 2", lHome.getRelations(term1), lAdd);

		final UniqueID[] lAdd2 = new UniqueID[] { lTerm2 };
		model.addAssociations(lAdd2);
		assertEquals(4, model.getElements().length);
		model.undoChanges();
		assertEquals(3, model.getElements().length);
		assertRelations("Relations 3", lHome.getRelations(term1), lAdd);
	}

	@Test
	public void testIsAssociatedUniqueIDArray() throws Exception {
		final int lType = IItem.PERSON;
		final UniqueID lTerm2 = new UniqueID(term2.getItemType(), term2.getID());
		final UniqueID lPerson1 = new UniqueID(lType, person1.getID());
		final UniqueID lPerson2 = new UniqueID(lType, person2.getID());
		final UniqueID lPerson3 = new UniqueID(lType, person3.getID());

		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		final RelationHome lHome = data.getRelationHome();
		final UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
		model.addAssociations(lAdd);
		model.saveChanges();
		assertEquals(3, lHome.getCount());

		assertTrue(model.isAssociated(lPerson1));
		assertTrue(model.isAssociated(lAdd));

		assertFalse(model.isAssociated(lTerm2));
		assertFalse(model.isAssociated(new UniqueID[] { lPerson1, lPerson2,
		        lPerson3, lTerm2 }));
	}

	@Test
	public void testRemoveRelation() throws Exception {
		final RelationHome lHome = data.getRelationHome();
		lHome.newRelation(term1, term2);
		lHome.newRelation(term1, person1);
		lHome.newRelation(term1, person2);
		lHome.newRelation(term1, person3);
		lHome.newRelation(term2, person3);

		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		Object[] lAssociated = model.getElements();
		assertEquals("number of associated 1", 4, lAssociated.length);

		final List<IRelation> lRelations = ((CentralAssociationsModel) model)
		        .getCenter().getSources();
		assertEquals("number of relations 1", 4, lRelations.size());

		final IRelation lToDelete = lRelations.get(1);
		assertTrue(
		        "source is term1",
		        lToDelete.getSourceItem().equals(
		                new ItemAdapter(term1, image, context)));
		assertTrue(
		        "target is person1",
		        lToDelete.getTargetItem().equals(
		                new ItemAdapter(person1, image, context)));
		assertTrue("person1 is element",
		        Arrays.asList(lAssociated).contains(person1));

		model.removeRelation(lToDelete);

		lAssociated = model.getElements();
		assertEquals("number of associated 2", 3, lAssociated.length);
		assertFalse("person1 is not element", Arrays.asList(lAssociated)
		        .contains(person1));
	}

	@Test
	public void testRemoveAssociations() throws Exception {
		final RelationHome lHome = data.getRelationHome();
		lHome.newRelation(term1, term2);
		lHome.newRelation(term1, person1);
		lHome.newRelation(term1, person2);
		lHome.newRelation(term1, person3);
		lHome.newRelation(term2, person3);

		// refresh with new relations
		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		Object[] lAssociated = model.getElements();
		assertEquals("number of associated 1", 4, lAssociated.length);

		List<Object> lAsList = Arrays.asList(lAssociated);
		assertTrue("person1 is element", lAsList.contains(person1));
		assertTrue("person2 is element", lAsList.contains(person2));

		final ItemAdapter[] lToDelete = new ItemAdapter[] {
		        new ItemAdapter(person2, image, context),
		        new ItemAdapter(person1, image, context) };
		model.removeAssociations(lToDelete);
		lAssociated = model.getElements();
		assertEquals("number of associated 2", 2, lAssociated.length);

		lAsList = Arrays.asList(lAssociated);
		assertFalse("person1 is not element", lAsList.contains(person1));
		assertFalse("person2 is not element", lAsList.contains(person2));
		assertTrue("term2 is element", lAsList.contains(term2));
		assertTrue("person3 is element", lAsList.contains(person3));

		final UniqueID[] lToDelete2 = new UniqueID[] {
		        new UniqueID(person3.getItemType(), person3.getID()),
		        new UniqueID(term2.getItemType(), term2.getID()) };
		when(dataService.retrieveItem(lToDelete2[0])).thenReturn(
		        getRelatedPersons(term1).get(2));
		when(dataService.retrieveItem(lToDelete2[1])).thenReturn(
		        getRelatedTerms(term1).get(0));
		model.removeAssociations(lToDelete2);

		lAssociated = model.getElements();
		assertEquals("number of associated 3", 0, lAssociated.length);
	}

	@Test
	public void testGetAssociationsModel() throws Exception {
		final RelationHome lHome = data.getRelationHome();
		lHome.newRelation(term1, term2);
		lHome.newRelation(term1, person1);
		lHome.newRelation(term1, person2);
		lHome.newRelation(term1, person3);
		lHome.newRelation(term2, person3);

		// refresh with new relations
		model = CentralAssociationsModel.createCentralAssociationsModel(
		        new ItemAdapter(term1, image, context), context);

		Object[] lAssociated = model.getElements();
		assertEquals("number of associated 1", 4, lAssociated.length);

		final IAssociationsModel lModel2 = ((CentralAssociationsModel) model)
		        .getAssociationsModel(new ItemAdapter(term2, image, context));
		lAssociated = lModel2.getElements();
		assertEquals("number of associated 2", 2, lAssociated.length);

		final List<Object> lAsList = Arrays.asList(lAssociated);
		assertTrue("term1 is element", lAsList.contains(term1));
		assertTrue("person3 is element", lAsList.contains(person3));
	}

	@Test
	public void testEquals() throws Exception {
		final RelationHome lHome = data.getRelationHome();
		lHome.newRelation(term1, term2);
		lHome.newRelation(term1, person1);
		lHome.newRelation(term1, person2);
		lHome.newRelation(term1, person3);
		lHome.newRelation(term2, person3);

		final CentralAssociationsModel lModel1 = CentralAssociationsModel
		        .createCentralAssociationsModel(new ItemAdapter(term1, image,
		                context), context);
		final CentralAssociationsModel lModel2 = CentralAssociationsModel
		        .createCentralAssociationsModel(new ItemAdapter(term2, image,
		                context), context);
		final CentralAssociationsModel lModel3 = CentralAssociationsModel
		        .createCentralAssociationsModel(new ItemAdapter(term1, image,
		                context), context);

		assertTrue("model equals self", lModel1.equals(lModel1));
		assertTrue("hashCode equals self",
		        lModel1.hashCode() == lModel1.hashCode());
		assertFalse("model not equals null", lModel1.equals(null));
		assertFalse("model not equals String",
		        lModel1.equals(lModel1.toString()));
		assertFalse("String not equals model", "model".equals(lModel1));
		assertFalse("model not equals term model", lModel1.equals(term1));
		assertFalse("model 1 not equals term model 2", lModel1.equals(lModel2));
		assertTrue("model 1 equals model 3", lModel1.equals(lModel3));
		assertTrue("model 3 equals model 1", lModel3.equals(lModel1));
		assertEquals("hashCode 1 equals hashCode 3", lModel1.hashCode(),
		        lModel3.hashCode());

		final UniqueID lPersonID = new UniqueID(person2.getItemType(),
		        person2.getID());
		when(dataService.retrieveItem(lPersonID)).thenReturn(
		        new ItemAdapter(person2, image, context));

		lModel3.removeAssociations(new UniqueID[] { lPersonID });
		assertFalse("model 1 not equals model 3 after remove",
		        lModel1.equals(lModel3));
	}

	private List<IItemModel> getRelatedPersons(final IItem inItem)
	        throws Exception {
		final ItemAdapter lItem = new ItemAdapter(inItem, image, context);
		return getRelatedItems(RelatedItemHelper.getRelatedPersons(lItem));
	}

	private List<IItemModel> getRelatedTerms(final IItem inItem)
	        throws Exception {
		final ItemAdapter lItem = new ItemAdapter(inItem, image, context);
		return getRelatedItems(RelatedItemHelper.getRelatedTerms(lItem));
	}

	private List<IItemModel> getRelatedItems(
	        final Collection<ItemWithIcon> inRelated) {
		final List<IItemModel> out = new ArrayList<IItemModel>();
		for (final ItemWithIcon lRelated : inRelated) {
			out.add(new ItemAdapter(lRelated.getItem(), image, context));
		}
		return out;
	}

}
