/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Maxime Hilaire
 *
 */
package org.nuxeo.ecm.directory.repository.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelComparator;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.repository.RepositoryDirectorySession;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(FeaturesRunner.class)
@Features(RepositoryDirectoryFeature.class)
public class TestRepositoryDirectory {

    @Inject
    @Named(value = RepositoryDirectoryFeature.REPO_DIRECTORY_NAME)
    Directory repoDir;
    
    @Inject
    UserManager um;

    RepositoryDirectorySession dirSession;

    @Inject
    protected RuntimeHarness harness;

    protected final static String PREFIX_SCHEMA = "sch1";

    protected final static String USERNAME_FIELD = "username";

    protected final static String PASSWORD_FIELD = "password";

    protected final static String COMPANY_FIELD = "company";

    protected final static String BAR_FIELD = PREFIX_SCHEMA + ":" + "bar";

    @Before
    public void setUp() throws Exception {

        // the resilient directory
        dirSession = (RepositoryDirectorySession) repoDir.getSession();

        Map<String, Object> e = new HashMap<String, Object>();
        e.put(USERNAME_FIELD, "1");
        e.put(PASSWORD_FIELD, "foo1");
        e.put(COMPANY_FIELD, "bar1");
        dirSession.createEntry(e);
    }

    @After
    public void tearDown() throws Exception {
        dirSession.close();
    }

    @Test
    public void testCreateEntry() throws Exception {

        Map<String, Object> e;

        e = new HashMap<String, Object>();
        e.put(USERNAME_FIELD, "2");
        e.put(PASSWORD_FIELD, "foo3");
        e.put(COMPANY_FIELD, "bar3");
        DocumentModel doc = dirSession.createEntry(e);

        assertEquals("bar3", doc.getPropertyValue(BAR_FIELD));
    }

    @Test
    public void testUpdateEntry() throws Exception {
        Map<String, Object> e;

        e = new HashMap<String, Object>();
        e.put("uid", "1");
        e.put("foo", "foo3");
        e.put("bar", "bar3");

        DocumentModel docModel = dirSession.getEntry("1");
        docModel.setProperties("schema1", e);

        dirSession.updateEntry(docModel);

    }

    @Test
    public void testGetEntry() throws Exception {
        DocumentModel entry;
        entry = dirSession.getEntry("1");
        assertEquals("1", entry.getProperty("schema1", "uid"));
        assertEquals("foo1", entry.getProperty("schema1", "foo"));
        entry = dirSession.getEntry("no-such-entry");
        assertNull(entry);
    }

    @Test
    public void testAuthenticate() throws Exception {
        assertTrue(dirSession.authenticate("1", "foo1"));
        assertFalse(dirSession.authenticate("1", "haha"));
    }

    @Test
    public void testDeleteEntry() throws Exception {
        dirSession.deleteEntry("no-such-entry");
        dirSession.deleteEntry("1");
        assertNull(dirSession.getEntry("1"));
    }

    @Test
    public void testHasEntry() throws Exception {
        assertTrue(dirSession.hasEntry("1"));
        assertFalse(dirSession.hasEntry("foo"));
    }

    @Test
    public void testQuery() throws Exception {

    }

    @Test
    public void testCreateFromModel() throws Exception {
        String schema = "schema1";
        DocumentModel entry = BaseSession.createEntryModel(null, schema, null,
                null);
        entry.setProperty("schema1", "uid", "yo");

        assertNull(dirSession.getEntry("yo"));
        dirSession.createEntry(entry);
        assertNotNull(dirSession.getEntry("yo"));

        // create one with existing same id, must fail
        entry.setProperty("schema1", "uid", "1");
        try {
            entry = dirSession.createEntry(entry);
            fail("Should raise an error, entry already exists");
        } catch (DirectoryException e) {
        }
    }

    @Test
    public void testReadOnlyOnGetEntry() throws Exception {
        // by default no backing dir is readonly
        assertFalse(BaseSession.isReadOnlyEntry(dirSession.getEntry("1")));

        // Set the master memory directory to read-only
        // dirSession.setReadOnly(true);

        // The resilient dir should be read-only now
        assertTrue(dirSession.isReadOnly());

        // all should be readonly
        assertTrue(BaseSession.isReadOnlyEntry(dirSession.getEntry("1")));

    }

    @Test
    public void testReadOnlyEntryInQueryResults() throws Exception {
        Map<String, String> orderBy = new HashMap<String, String>();
        orderBy.put("schema1:uid", "asc");
        DocumentModelComparator comp = new DocumentModelComparator(orderBy);

        Map<String, Serializable> filter = new HashMap<String, Serializable>();
        DocumentModelList results = dirSession.query(filter);
        Collections.sort(results, comp);

        // by default no backing dir is readonly
        assertFalse(BaseSession.isReadOnlyEntry(results.get(0)));
        assertFalse(BaseSession.isReadOnlyEntry(results.get(1)));

        // dirSession.setReadOnly(true);
        results = dirSession.query(filter);
        Collections.sort(results, comp);
        assertTrue(BaseSession.isReadOnlyEntry(results.get(0)));
        assertTrue(BaseSession.isReadOnlyEntry(results.get(1)));

    }

}
