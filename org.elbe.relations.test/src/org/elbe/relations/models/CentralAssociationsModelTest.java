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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
        this.context.set(IBrowserManager.class, this.browserManager);

        this.term1 = data.createTerm("term 1");
        this.term2 = data.createTerm("term 2");
        this.person1 = data.createPerson("person1", "1");
        this.person2 = data.createPerson("person2", "2");
        this.person3 = data.createPerson("person3", "3");
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testSaveChanges() throws Exception {
        final int lType = IItem.PERSON;
        final UniqueID lTerm2 = new UniqueID(this.term2.getItemType(), this.term2.getID());
        final UniqueID lPerson1 = new UniqueID(lType, this.person1.getID());
        final UniqueID lPerson2 = new UniqueID(lType, this.person2.getID());
        final UniqueID lPerson3 = new UniqueID(lType, this.person3.getID());

        when(this.dataService.retrieveItem(lPerson1)).thenReturn(
                new ItemAdapter(this.person1, this.image, this.context));
        when(this.dataService.retrieveItem(lPerson2)).thenReturn(
                new ItemAdapter(this.person2, this.image, this.context));
        when(this.dataService.retrieveItem(lPerson3)).thenReturn(
                new ItemAdapter(this.person3, this.image, this.context));
        when(this.dataService.retrieveItem(lTerm2)).thenReturn(
                new ItemAdapter(this.term2, this.image, this.context));

        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        final RelationHome lHome = data.getRelationHome();
        assertEquals(0, lHome.getCount());

        assertEquals(0, this.model.getElements().length);

        UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
        this.model.addAssociations(lAdd);
        assertEquals(3, this.model.getElements().length);

        UniqueID[] lRemove = new UniqueID[] { lPerson2 };
        this.model.removeAssociations(lRemove);
        assertEquals(2, this.model.getElements().length);

        lAdd = new UniqueID[] { lTerm2 };
        this.model.addAssociations(lAdd);
        assertEquals(3, this.model.getElements().length);

        this.model.saveChanges();
        assertEquals(3, lHome.getCount());
        assertRelations(lHome.getRelations(this.term1),
                new UniqueID[] { lPerson1, lPerson3, lTerm2 });

        lRemove = new UniqueID[] { lPerson1 };
        this.model.removeAssociations(lRemove);
        this.model.saveChanges();
        assertEquals(2, lHome.getCount());
        assertRelations(lHome.getRelations(this.term1),
                new UniqueID[] { lPerson3, lTerm2 });

        lAdd = new UniqueID[] { lPerson1, lPerson2 };
        this.model.addAssociations(lAdd);
        this.model.saveChanges();
        assertEquals(4, lHome.getCount());
        assertRelations(lHome.getRelations(this.term1),
                new UniqueID[] { lPerson1, lPerson2, lPerson3, lTerm2 });
    }

    private void assertRelations(final QueryResult inResult, final UniqueID[] inExpected)
            throws VException, SQLException {
        while (inResult.hasMoreElements()) {
            final DomainObject lRelation = (DomainObject) inResult.next();
            assertRelation(new UniqueID(lRelation.get(RelationHome.KEY_TYPE1) + ":"
                    + lRelation.get(RelationHome.KEY_ITEM1)),
                    new UniqueID(lRelation.get(RelationHome.KEY_TYPE2) + ":"
                            + lRelation.get(RelationHome.KEY_ITEM2)),
                    inExpected);
        }
    }

    private void assertRelation(final UniqueID inItem1,
            final UniqueID inItem2, final UniqueID[] inExpected) {
        for (int i = 0; i < inExpected.length; i++) {
            if (inExpected[i].equals(inItem1)) {
                return;
            }
            if (inExpected[i].equals(inItem2)) {
                return;
            }
        }
        fail();
    }

    @Test
    public void testUndoChanges() throws Exception {
        final int lType = IItem.PERSON;
        final UniqueID lTerm2 = new UniqueID(this.term2.getItemType(), this.term2.getID());
        final UniqueID lPerson1 = new UniqueID(lType, this.person1.getID());
        final UniqueID lPerson2 = new UniqueID(lType, this.person2.getID());
        final UniqueID lPerson3 = new UniqueID(lType, this.person3.getID());

        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        when(this.dataService.retrieveItem(lPerson1)).thenReturn(
                new ItemAdapter(this.person1, this.image, this.context));
        when(this.dataService.retrieveItem(lPerson2)).thenReturn(
                new ItemAdapter(this.person2, this.image, this.context));
        when(this.dataService.retrieveItem(lPerson3)).thenReturn(
                new ItemAdapter(this.person3, this.image, this.context));
        when(this.dataService.retrieveItem(lTerm2)).thenReturn(
                new ItemAdapter(this.term2, this.image, this.context));

        final RelationHome lHome = data.getRelationHome();
        assertEquals(0, this.model.getElements().length);

        final UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
        this.model.addAssociations(lAdd);
        assertEquals(3, this.model.getElements().length);

        this.model.saveChanges();
        assertEquals(3, lHome.getCount());
        assertRelations(lHome.getRelations(this.term1), lAdd);

        when(this.dataService.retrieveItem(lPerson2)).thenReturn(
                getRelatedPersons(this.term1).get(1));
        final UniqueID[] lRemove = new UniqueID[] { lPerson2 };
        this.model.removeAssociations(lRemove);
        assertEquals(2, this.model.getElements().length);
        this.model.undoChanges();
        assertEquals(3, this.model.getElements().length);
        assertRelations(lHome.getRelations(this.term1), lAdd);

        final UniqueID[] lAdd2 = new UniqueID[] { lTerm2 };
        this.model.addAssociations(lAdd2);
        assertEquals(4, this.model.getElements().length);
        this.model.undoChanges();
        assertEquals(3, this.model.getElements().length);
        assertRelations(lHome.getRelations(this.term1), lAdd);
    }

    @Test
    public void testIsAssociatedUniqueIDArray() throws Exception {
        final int lType = IItem.PERSON;
        final UniqueID lTerm2 = new UniqueID(this.term2.getItemType(), this.term2.getID());
        final UniqueID lPerson1 = new UniqueID(lType, this.person1.getID());
        final UniqueID lPerson2 = new UniqueID(lType, this.person2.getID());
        final UniqueID lPerson3 = new UniqueID(lType, this.person3.getID());

        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        final RelationHome lHome = data.getRelationHome();
        final UniqueID[] lAdd = new UniqueID[] { lPerson1, lPerson2, lPerson3 };
        this.model.addAssociations(lAdd);
        this.model.saveChanges();
        assertEquals(3, lHome.getCount());

        assertTrue(this.model.isAssociated(lPerson1));
        assertTrue(this.model.isAssociated(lAdd));

        assertFalse(this.model.isAssociated(lTerm2));
        assertFalse(this.model.isAssociated(new UniqueID[] { lPerson1, lPerson2,
                lPerson3, lTerm2 }));
    }

    @Test
    public void testRemoveRelation() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        lHome.newRelation(this.term1, this.term2);
        lHome.newRelation(this.term1, this.person1);
        lHome.newRelation(this.term1, this.person2);
        lHome.newRelation(this.term1, this.person3);
        lHome.newRelation(this.term2, this.person3);

        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        Object[] lAssociated = this.model.getElements();
        assertEquals(4, lAssociated.length);

        final List<IRelation> lRelations = ((CentralAssociationsModel) this.model)
                .getCenter().getSources();
        assertEquals(4, lRelations.size());

        final IRelation lToDelete = lRelations.get(1);
        assertTrue(lToDelete.getSourceItem().equals(
                new ItemAdapter(this.term1, this.image, this.context)));
        assertTrue(lToDelete.getTargetItem().equals(
                new ItemAdapter(this.person1, this.image, this.context)));
        assertTrue(
                Arrays.asList(lAssociated).contains(this.person1));

        this.model.removeRelation(lToDelete);

        lAssociated = this.model.getElements();
        assertEquals(3, lAssociated.length);
        assertFalse(Arrays.asList(lAssociated)
                .contains(this.person1));
    }

    @Test
    public void testRemoveAssociations() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        lHome.newRelation(this.term1, this.term2);
        lHome.newRelation(this.term1, this.person1);
        lHome.newRelation(this.term1, this.person2);
        lHome.newRelation(this.term1, this.person3);
        lHome.newRelation(this.term2, this.person3);

        // refresh with new relations
        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        Object[] lAssociated = this.model.getElements();
        assertEquals(4, lAssociated.length);

        List<Object> lAsList = Arrays.asList(lAssociated);
        assertTrue(lAsList.contains(this.person1));
        assertTrue(lAsList.contains(this.person2));

        final ItemAdapter[] lToDelete = new ItemAdapter[] {
                new ItemAdapter(this.person2, this.image, this.context),
                new ItemAdapter(this.person1, this.image, this.context) };
        this.model.removeAssociations(lToDelete);
        lAssociated = this.model.getElements();
        assertEquals(2, lAssociated.length);

        lAsList = Arrays.asList(lAssociated);
        assertFalse(lAsList.contains(this.person1));
        assertFalse(lAsList.contains(this.person2));
        assertTrue(lAsList.contains(this.term2));
        assertTrue(lAsList.contains(this.person3));

        final UniqueID[] lToDelete2 = new UniqueID[] {
                new UniqueID(this.person3.getItemType(), this.person3.getID()),
                new UniqueID(this.term2.getItemType(), this.term2.getID()) };
        when(this.dataService.retrieveItem(lToDelete2[0])).thenReturn(
                getRelatedPersons(this.term1).get(2));
        when(this.dataService.retrieveItem(lToDelete2[1])).thenReturn(
                getRelatedTerms(this.term1).get(0));
        this.model.removeAssociations(lToDelete2);

        lAssociated = this.model.getElements();
        assertEquals(0, lAssociated.length);
    }

    @Test
    public void testGetAssociationsModel() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        lHome.newRelation(this.term1, this.term2);
        lHome.newRelation(this.term1, this.person1);
        lHome.newRelation(this.term1, this.person2);
        lHome.newRelation(this.term1, this.person3);
        lHome.newRelation(this.term2, this.person3);

        // refresh with new relations
        this.model = CentralAssociationsModel.createCentralAssociationsModel(
                new ItemAdapter(this.term1, this.image, this.context), this.context);

        Object[] lAssociated = this.model.getElements();
        assertEquals(4, lAssociated.length);

        final IAssociationsModel lModel2 = ((CentralAssociationsModel) this.model)
                .getAssociationsModel(new ItemAdapter(this.term2, this.image, this.context));
        lAssociated = lModel2.getElements();
        assertEquals(2, lAssociated.length);

        final List<Object> lAsList = Arrays.asList(lAssociated);
        assertTrue(lAsList.contains(this.term1));
        assertTrue(lAsList.contains(this.person3));
    }

    @Test
    public void testEquals() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        lHome.newRelation(this.term1, this.term2);
        lHome.newRelation(this.term1, this.person1);
        lHome.newRelation(this.term1, this.person2);
        lHome.newRelation(this.term1, this.person3);
        lHome.newRelation(this.term2, this.person3);

        final CentralAssociationsModel lModel1 = CentralAssociationsModel
                .createCentralAssociationsModel(new ItemAdapter(this.term1, this.image,
                        this.context), this.context);
        final CentralAssociationsModel lModel2 = CentralAssociationsModel
                .createCentralAssociationsModel(new ItemAdapter(this.term2, this.image,
                        this.context), this.context);
        final CentralAssociationsModel lModel3 = CentralAssociationsModel
                .createCentralAssociationsModel(new ItemAdapter(this.term1, this.image,
                        this.context), this.context);

        assertTrue(lModel1.equals(lModel1));
        assertTrue(
                lModel1.hashCode() == lModel1.hashCode());
        assertFalse(lModel1.equals(null));
        assertFalse(
                lModel1.equals(lModel1.toString()));
        assertFalse("model".equals(lModel1));
        assertFalse(lModel1.equals(this.term1));
        assertFalse(lModel1.equals(lModel2));
        assertTrue(lModel1.equals(lModel3));
        assertTrue(lModel3.equals(lModel1));
        assertEquals(lModel1.hashCode(),
                lModel3.hashCode());

        final UniqueID lPersonID = new UniqueID(this.person2.getItemType(),
                this.person2.getID());
        when(this.dataService.retrieveItem(lPersonID)).thenReturn(
                new ItemAdapter(this.person2, this.image, this.context));

        lModel3.removeAssociations(new UniqueID[] { lPersonID });
        assertFalse(
                lModel1.equals(lModel3));
    }

    private List<IItemModel> getRelatedPersons(final IItem inItem)
            throws Exception {
        final ItemAdapter lItem = new ItemAdapter(inItem, this.image, this.context);
        return getRelatedItems(RelatedItemHelper.getRelatedPersons(lItem));
    }

    private List<IItemModel> getRelatedTerms(final IItem inItem)
            throws Exception {
        final ItemAdapter lItem = new ItemAdapter(inItem, this.image, this.context);
        return getRelatedItems(RelatedItemHelper.getRelatedTerms(lItem));
    }

    private List<IItemModel> getRelatedItems(
            final Collection<ItemWithIcon> inRelated) {
        final List<IItemModel> out = new ArrayList<IItemModel>();
        for (final ItemWithIcon lRelated : inRelated) {
            out.add(new ItemAdapter(lRelated.getItem(), this.image, this.context));
        }
        return out;
    }

}
