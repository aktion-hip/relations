package org.elbe.relations.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * JUnit Plug-in test
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
@ExtendWith(MockitoExtension.class)
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

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        data = DataHouseKeeper.INSTANCE;
    }

    @BeforeEach
    public void setUp() throws Exception {
        this.image = new Image(this.device, 1, 1);
        this.languages = new LanguageService(Locale.ENGLISH.getLanguage());

        this.context = EclipseContextFactory.create("test context");
        this.context.set(Logger.class, this.log);
        this.context.set(IEventBroker.class, this.eventBroker);
        this.context.set(LanguageService.class, this.languages);
        this.context.set(IDataService.class, this.dataService);

        this.items = new IItem[10];
        this.items[0] = data.createTerm("Term1");
        this.items[1] = data.createTerm("Term2");
        this.items[2] = data.createTerm("Term3");
        this.items[3] = data.createTerm("Term4");
        this.items[4] = data.createTerm("Term5");
        this.items[5] = data.createTerm("Term6");
        this.items[6] = data.createPerson("Person1", "First1");
        this.items[7] = data.createPerson("Person1", "First1");
        this.items[8] = data.createPerson("Person1", "First1");
        this.items[9] = data.createPerson("Person1", "First1");
        data.createRelation(this.items[0], this.items[1]);
        data.createRelation(this.items[0], this.items[2]);
        data.createRelation(this.items[0], this.items[3]);
        data.createRelation(this.items[0], this.items[5]);
        data.createRelation(this.items[0], this.items[6]);
        data.createRelation(this.items[0], this.items[9]);

        this.center = new ItemAdapter(this.items[0], this.image, this.context);
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testGetAllItems() throws Exception {
        final CentralAssociationsModel lModel = CentralAssociationsModel
                .createCentralAssociationsModel(this.center, this.context);
        assertTrue(lModel.getCenter().equals(new ItemAdapter(this.center, this.context)));
        final Collection<ItemAdapter> lModelItems = lModel.getAllItems();
        assertEquals(7, lModelItems.size());
    }

    @Test
    public void testGetElements() throws Exception {
        final IAssociationsModel lAssociations = CentralAssociationsModel
                .createCentralAssociationsModel(this.center, this.context);
        final Object[] lRelated = lAssociations.getElements();
        assertEquals(6, lRelated.length);
    }

    @Test
    public void testSelect() throws Exception {
        final IAssociationsModel lAssociations = CentralAssociationsModel
                .createCentralAssociationsModel(this.center, this.context);
        assertFalse(lAssociations.select(this.items[0].getLightWeight()));
        assertFalse(lAssociations.select(this.items[1].getLightWeight()));
        assertTrue(lAssociations.select(this.items[4].getLightWeight()));
    }

    @Test
    public void testManipulations() throws Exception {
        final IAssociationsModel lAssociations = CentralAssociationsModel
                .createCentralAssociationsModel(this.center, this.context);

        final Object[] lAdd = {
                new LightWeightTermWithIcon(
                        (LightWeightTerm) this.items[4].getLightWeight()),
                new LightWeightPersonWithIcon(
                        (LightWeightPerson) this.items[6].getLightWeight()) };
        lAssociations.addAssociations(lAdd);
        assertFalse(lAssociations.select(this.items[4].getLightWeight()));
        assertEquals(8,lAssociations.getElements().length);

        final Object[] lRemove = { new ItemAdapter(this.items[4], this.image, this.context),
                new ItemAdapter(this.items[1], this.image, this.context),
                new ItemAdapter(this.items[2], this.image, this.context) };
        lAssociations.removeAssociations(lRemove);
        assertEquals(5, lAssociations.getElements().length);
        assertTrue(lAssociations.select(this.items[4].getLightWeight()));
        assertTrue(lAssociations.select(this.items[1].getLightWeight()));
        assertTrue(lAssociations.select(this.items[2].getLightWeight()));

    }

}
