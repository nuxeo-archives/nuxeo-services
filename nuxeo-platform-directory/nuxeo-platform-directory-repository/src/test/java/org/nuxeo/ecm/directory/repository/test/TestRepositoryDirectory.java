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

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.runtime.api.login.LoginService;
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
    protected Directory repoDir;

    @Inject
    protected LoginService loginService;

    @Inject
    protected RuntimeHarness harness;

    protected final static String SCHEMA_NAME = "schema1";

    protected final static String USER_SCHEMA_NAME = "user";

    protected final static String PREFIX_SCHEMA = "sch1";

    protected final static String USERNAME_FIELD = "username";

    protected final static String PASSWORD_FIELD = "password";

    protected final static String COMPANY_FIELD = "company";

    protected final static String UID_FIELD = PREFIX_SCHEMA + ":" + "uid";

    protected final static String BAR_FIELD = PREFIX_SCHEMA + ":" + "bar";

    protected final static String FOO_FIELD = PREFIX_SCHEMA + ":" + "foo";

    protected Session dirSession = null;

    @Before
    public void setUp() throws Exception {

        dirSession = repoDir.getSession();

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
    @Ignore
    public void testUpdateEntry() throws Exception {
        // TODO either fix mapping or remove mapping to fix this test
        // TODO test with different user's right

        Map<String, Object> e;
        e = new HashMap<String, Object>();
        e.put(USERNAME_FIELD, RepositoryDirectoryInit.DOC_ID_USER1);
        e.put(PASSWORD_FIELD, "foo3");
        e.put(COMPANY_FIELD, "bar3");

        DocumentModel docModel = dirSession.getEntry(RepositoryDirectoryInit.DOC_ID_USER1);
        docModel.setProperties(USER_SCHEMA_NAME, e);

        dirSession.updateEntry(docModel);

        docModel = dirSession.getEntry(RepositoryDirectoryInit.DOC_ID_USER1);
        Assert.assertEquals("foo3", docModel.getPropertyValue(FOO_FIELD));
    }

    @Test
    public void testAuthenticate() throws Exception {
        Assert.assertTrue(dirSession.authenticate(
                RepositoryDirectoryInit.DOC_ID_USER1,
                RepositoryDirectoryInit.DOC_PWD_USER1));
        Assert.assertFalse(dirSession.authenticate(
                RepositoryDirectoryInit.DOC_ID_USER1, "bad-pwd"));
        Assert.assertFalse(dirSession.authenticate("bad-id", "haha"));
    }

    @Test
    public void testDeleteEntry() throws Exception {
        dirSession.deleteEntry("no-such-entry");
        dirSession.deleteEntry("1");
        assertNull(dirSession.getEntry("1"));
    }

    @Test
    public void testHasEntry() throws Exception {
        assertTrue(dirSession.hasEntry(RepositoryDirectoryInit.DOC_ID_USER1));
        assertFalse(dirSession.hasEntry("bad-id"));
    }

    @Test
    public void testQuery() throws Exception {

    }

    // TODO to be tested :
    // create an entry that already exist but the user has not permission to see
    // it
    // See where it is stored (if ok)
    // try to getEntry id

    @Test
    @Ignore
    public void testCreateFromModel() throws Exception {
        DocumentModel entry = BaseSession.createEntryModel(null, SCHEMA_NAME,
                null, null);
        String id = "newId";
        entry.setPropertyValue(UID_FIELD, id);

        assertNull(dirSession.getEntry(id));
        DocumentModel newDoc = dirSession.createEntry(entry);
        dirSession.updateEntry(newDoc);
        assertNotNull(dirSession.getEntry(id));

        // create one with existing same id, must fail
        entry.setProperty(USER_SCHEMA_NAME, USERNAME_FIELD,
                RepositoryDirectoryInit.DOC_ID_USER1);
        try {
            entry = dirSession.createEntry(entry);
            fail("Should raise an error, entry already exists");
        } catch (DirectoryException e) {
        }
    }

    @Test
    public void queryWithFilter() {
        // TODO Auto-generated method stub
        //
    }

    @Test
    public void getReferences() {
        // TODO Auto-generated method stub
        //
    }

}
