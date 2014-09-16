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
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelComparator;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.local.ClientLoginModule;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
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
    Directory repoDir;

    @Inject
    LoginService loginService;

    @Inject
    UserManager um;

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

    protected Principal loginAs(String username, String password)
            throws Exception {
        Principal user = um.authenticate(username, password);
        LoginContext logContext = Framework.login(username, password);
        ClientLoginModule.getThreadLocalLogin().push(user,
                password.toCharArray(), logContext.getSubject());
        return user;
    }

    @After
    public void tearDown() throws Exception {
        // dirSession.close();
    }

    @Test
    public void testCreateEntry() throws Exception {
        Map<String, Object> e;

        e = new HashMap<String, Object>();
        e.put(USERNAME_FIELD, "2");
        e.put(PASSWORD_FIELD, "foo3");
        e.put(COMPANY_FIELD, "bar3");
        DocumentModel doc = repoDir.getSession().createEntry(e);

        assertEquals("bar3", doc.getPropertyValue(BAR_FIELD));
        
    }

    @Test
    public void testUpdateEntry() throws Exception {
        //TODO test with different user's right
        
        Map<String, Object> e;
        e = new HashMap<String, Object>();
        e.put(USERNAME_FIELD, RepositoryDirectoryInit.DOC_ID_USER1);
        e.put(PASSWORD_FIELD, "foo3");
        e.put(COMPANY_FIELD, "bar3");

        DocumentModel docModel = repoDir.getSession().getEntry(RepositoryDirectoryInit.DOC_ID_USER1);
        docModel.setProperties(USER_SCHEMA_NAME, e);

        repoDir.getSession().updateEntry(docModel);
        
        docModel = repoDir.getSession().getEntry(RepositoryDirectoryInit.DOC_ID_USER1);
        Assert.assertEquals("foo3", docModel.getPropertyValue(FOO_FIELD));
    }
    
    

    @Test
    public void testGetEntry() throws Exception {
//        loginAs("user_2", "user_2");

        DocumentModel entry;
        entry = repoDir.getSession().getEntry(
                RepositoryDirectoryInit.DOC_ID_USER1);
        assertEquals("foo1", entry.getPropertyValue(FOO_FIELD));
        entry = repoDir.getSession().getEntry("no-such-entry");
        assertNull(entry);

        // TODO : test with different user's rights

    }

    @Test
    public void testAuthenticate() throws Exception {
        Assert.assertTrue(repoDir.getSession().authenticate(RepositoryDirectoryInit.DOC_ID_USER1, RepositoryDirectoryInit.DOC_PWD_USER1));
        Assert.assertFalse(repoDir.getSession().authenticate(RepositoryDirectoryInit.DOC_ID_USER1, "bad-pwd"));
        Assert.assertFalse(repoDir.getSession().authenticate("bad-id", "haha"));
    }

    @Test
    public void testDeleteEntry() throws Exception {
        repoDir.getSession().deleteEntry("no-such-entry");
        repoDir.getSession().deleteEntry("1");
        assertNull(repoDir.getSession().getEntry("1"));
    }

    @Test
    public void testHasEntry() throws Exception {
        assertTrue(repoDir.getSession().hasEntry(RepositoryDirectoryInit.DOC_ID_USER1));
        assertFalse(repoDir.getSession().hasEntry("bad-id"));
    }

    @Test
    public void testQuery() throws Exception {

    }

    //TO be tested :
//    create an entry that already exist but the user has not permission to see it
//    See where it is stored (if ok)
//    try to getEntry id
    
    @Test
    public void testCreateFromModel() throws Exception {
        DocumentModel entry = BaseSession.createEntryModel(null, SCHEMA_NAME, null,
                null);
        String id = "newId";
        entry.setPropertyValue(UID_FIELD, id);

        assertNull(repoDir.getSession().getEntry(id));
        DocumentModel newDoc = repoDir.getSession().createEntry(entry);
        repoDir.getSession().updateEntry(newDoc);
        assertNotNull(repoDir.getSession().getEntry(id));

        // create one with existing same id, must fail
        entry.setProperty(USER_SCHEMA_NAME, USERNAME_FIELD, RepositoryDirectoryInit.DOC_ID_USER1);
        try {
            entry = repoDir.getSession().createEntry(entry);
            fail("Should raise an error, entry already exists");
        } catch (DirectoryException e) {
        }
    }

    @Test
    public void testReadOnlyOnGetEntry() throws Exception {
        // by default no backing dir is readonly
        assertFalse(BaseSession.isReadOnlyEntry(repoDir.getSession().getEntry(
                "1")));

        // Set the master memory directory to read-only
        // dirSession.setReadOnly(true);

        // The resilient dir should be read-only now
        assertTrue(repoDir.getSession().isReadOnly());

        // all should be readonly
        assertTrue(BaseSession.isReadOnlyEntry(repoDir.getSession().getEntry(
                "1")));

    }

    @Test
    public void testReadOnlyEntryInQueryResults() throws Exception {
        Map<String, String> orderBy = new HashMap<String, String>();
        orderBy.put("schema1:uid", "asc");
        DocumentModelComparator comp = new DocumentModelComparator(orderBy);

        Map<String, Serializable> filter = new HashMap<String, Serializable>();
        DocumentModelList results = repoDir.getSession().query(filter);
        Collections.sort(results, comp);

        // by default no backing dir is readonly
        assertFalse(BaseSession.isReadOnlyEntry(results.get(0)));
        assertFalse(BaseSession.isReadOnlyEntry(results.get(1)));

        // dirSession.setReadOnly(true);
        results = repoDir.getSession().query(filter);
        Collections.sort(results, comp);
        assertTrue(BaseSession.isReadOnlyEntry(results.get(0)));
        assertTrue(BaseSession.isReadOnlyEntry(results.get(1)));

    }

}
