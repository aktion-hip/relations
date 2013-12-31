package org.elbe.relations.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.LightWeightPersonWithIcon;
import org.elbe.relations.internal.bom.LightWeightTermWithIcon;
import org.elbe.relations.internal.preferences.LanguageService;
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
public class AssociationsModelTest {
	private static DataHouseKeeper data;

	@Mock
	private Device device;
	@Mock
	private IEventBroker eventBroker;
	@Mock
	private Logger log;
	@Mock
	private IDataService dataService;

	private Image image;
	private LanguageService languages;
	private IEclipseContext context;

	private IItem[] items;
	private ItemAdapter center;

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

		items = new IItem[10];
		items[0] = data.createTerm("Term1");
		items[1] = data.createTerm("Term2");
		items[2] = data.createTerm("Term3");
		items[3] = data.createTerm("Term4");
		items[4] = data.createTerm("Term5");
		items[5] = data.createTerm("Term6");
		items[6] = data.createPerson("Person1", "First1");
		items[7] = data.createPerson("Person1", "First1");
		items[8] = data.createPerson("Person1", "First1");
		items[9] = data.createPerson("Person1", "First1");
		data.createRelation(items[0], items[1]);
		data.createRelation(items[0], items[2]);
		data.createRelation(items[0], items[3]);
		data.createRelation(items[0], items[5]);
		data.createRelation(items[0], items[6]);
		data.createRelation(items[0], items[9]);

		center = new ItemAdapter(items[0], image, context);
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testGetAllItems() throws Exception {
		final CentralAssociationsModel lModel = CentralAssociationsModel
		        .createCentralAssociationsModel(center, context);
		assertTrue(lModel.getCenter().equals(new ItemAdapter(center, context)));
		final Collection<ItemAdapter> lModelItems = lModel.getAllItems();
		assertEquals(7, lModelItems.size());
	}

	@Test
	public void testGetElements() throws Exception {
		final IAssociationsModel lAssociations = CentralAssociationsModel
		        .createCentralAssociationsModel(center, context);
		final Object[] lRelated = lAssociations.getElements();
		assertEquals(6, lRelated.length);
	}

	@Test
	public void testSelect() throws Exception {
		final IAssociationsModel lAssociations = CentralAssociationsModel
		        .createCentralAssociationsModel(center, context);
		assertFalse(lAssociations.select(items[0].getLightWeight()));
		assertFalse(lAssociations.select(items[1].getLightWeight()));
		assertTrue(lAssociations.select(items[4].getLightWeight()));
	}

	@Test
	public void testManipulations() throws Exception {
		final IAssociationsModel lAssociations = CentralAssociationsModel
		        .createCentralAssociationsModel(center, context);

		final Object[] lAdd = {
		        new LightWeightTermWithIcon(
		                (LightWeightTerm) items[4].getLightWeight()),
		        new LightWeightPersonWithIcon(
		                (LightWeightPerson) items[6].getLightWeight()) };
		lAssociations.addAssociations(lAdd);
		assertFalse("Filter new related",
		        lAssociations.select(items[4].getLightWeight()));
		assertEquals("Number of related after add", 8,
		        lAssociations.getElements().length);

		final Object[] lRemove = { new ItemAdapter(items[4], image, context),
		        new ItemAdapter(items[1], image, context),
		        new ItemAdapter(items[2], image, context) };
		lAssociations.removeAssociations(lRemove);
		assertEquals("Number of related after remove", 5,
		        lAssociations.getElements().length);
		assertTrue("Filter new unrelated 1",
		        lAssociations.select(items[4].getLightWeight()));
		assertTrue("Filter new unrelated 2",
		        lAssociations.select(items[1].getLightWeight()));
		assertTrue("Filter new unrelated 3",
		        lAssociations.select(items[2].getLightWeight()));

	}

}
