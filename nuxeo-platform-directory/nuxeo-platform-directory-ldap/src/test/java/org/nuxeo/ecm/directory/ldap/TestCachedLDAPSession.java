/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.directory.ldap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.nuxeo.ecm.directory.DirectoryCacheFeature;
import org.nuxeo.runtime.test.runner.ContributableFeaturesRunner;
import org.nuxeo.runtime.test.runner.Features;

/**
 * Test class for LDAP directory that use cache
 */
@RunWith(ContributableFeaturesRunner.class)
@Features(DirectoryCacheFeature.class)
@SuiteClasses(TestLDAPSession.class)
public class TestCachedLDAPSession extends LDAPDirectoryTestCase{

   
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//        
//        List<String> directories = Arrays.asList("userDirectory",
//                "groupDirectory");
//        for (String directoryName : directories) {
//            Directory dir = directoryService.getDirectory(directoryName);
//            DirectoryCache dirCache = dir.getCache();
//            dirCache.setEntryCacheName(ENTRY_CACHE_NAME);
//            dirCache.setEntryCacheWithoutReferencesName(ENTRY_CACHE_WITHOUT_REFERENCES_NAME);

//    }

}
