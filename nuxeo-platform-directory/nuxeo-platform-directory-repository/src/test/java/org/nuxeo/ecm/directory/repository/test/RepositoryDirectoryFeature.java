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
 *     mhilaire
 *
 */

package org.nuxeo.ecm.directory.repository.test;

import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.SimpleFeature;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.name.Names;

@Features({ TransactionalFeature.class, CoreFeature.class })
@RepositoryConfig(init = RepositoryDirectoryInit.class)
@Deploy({ "org.nuxeo.ecm.directory.api", "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql", "org.nuxeo.ecm.core.schema",
        "org.nuxeo.ecm.directory.types.contrib",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.directory.repository" })
@LocalDeploy({
        "org.nuxeo.ecm.directory.types.contrib:schemas-config.xml",
        "org.nuxeo.ecm.directory.repository.config.tests:login-config.xml",
        "org.nuxeo.ecm.directory.repository.config.tests:test-sql-directories-config.xml",
        "org.nuxeo.ecm.directory.repository.config.tests:test-contrib-usermanager-config.xml",
        "org.nuxeo.ecm.directory.repository.config.tests:repository-directory-config.xml" })
public class RepositoryDirectoryFeature extends SimpleFeature {
    public static final String REPO_DIRECTORY_NAME = "repositoryDirectory";


    @Override
    public void configure(final FeaturesRunner runner, Binder binder) {
        bindDirectory(binder, REPO_DIRECTORY_NAME);
    }

    protected void bindDirectory(Binder binder, final String name) {
        binder.bind(Directory.class).annotatedWith(Names.named(name)).toProvider(
                new Provider<Directory>() {

                    @Override
                    public Directory get() {
                        return Framework.getService(DirectoryService.class).getDirectory(
                                name);
                    }

                });
    }

    @Override
    public void stop(FeaturesRunner runner) throws Exception {
        Framework.getService(DirectoryService.class).getDirectory(
                REPO_DIRECTORY_NAME).getSession().close();
    }
    
    

}
