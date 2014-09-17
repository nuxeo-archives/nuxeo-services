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

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModelComparator;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.repository.RepositoryDirectory;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(FeaturesRunner.class)
@Features(RepositoryDirectoryFeature.class)
public class TestReadOnlyRepositoryDirectory {

    @Inject
    @Named(value = RepositoryDirectoryFeature.REPO_DIRECTORY_NAME)
    protected Directory repoDir;

    protected Session dirSystemReadOnlySession = null;

    @Before
    public void setUp() throws Exception {
        ((RepositoryDirectory) repoDir).getDescriptor().readOnly = true;
        dirSystemReadOnlySession = repoDir.getSession();
    }

    @After
    public void tearDown() throws Exception {
        dirSystemReadOnlySession.close();
    }

    @Test
    public void testReadOnlyOnGetEntry() throws Exception {
        assertTrue(dirSystemReadOnlySession.isReadOnly());

        assertTrue(BaseSession.isReadOnlyEntry(dirSystemReadOnlySession.getEntry(RepositoryDirectoryInit.DOC_ID_USER1)));

    }

    @Test
    public void testReadOnlyEntryInQueryResults() throws Exception {
        Map<String, String> orderBy = new HashMap<String, String>();
        orderBy.put(TestRepositoryDirectory.UID_FIELD, "asc");
        DocumentModelComparator comp = new DocumentModelComparator(orderBy);

        Map<String, Serializable> filter = new HashMap<String, Serializable>();

        DocumentModelList results = dirSystemReadOnlySession.query(filter);
        Collections.sort(results, comp);
        assertTrue(BaseSession.isReadOnlyEntry(results.get(0)));
        assertTrue(BaseSession.isReadOnlyEntry(results.get(1)));

    }

}
