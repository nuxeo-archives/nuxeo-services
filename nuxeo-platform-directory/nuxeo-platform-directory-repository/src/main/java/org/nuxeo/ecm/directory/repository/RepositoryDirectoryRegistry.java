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
package org.nuxeo.ecm.directory.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.runtime.model.ContributionFragmentRegistry;

/**
 * Registry of directory 
 *
 * @since 5.9.6
 */
public class RepositoryDirectoryRegistry extends
        ContributionFragmentRegistry<RepositoryDirectoryDescriptor> {

    private static final Log log = LogFactory.getLog(RepositoryDirectoryRegistry.class);

    protected Map<String, RepositoryDirectoryDescriptor> descriptors = new HashMap<String, RepositoryDirectoryDescriptor>();

    @Override
    public String getContributionId(RepositoryDirectoryDescriptor contrib) {
        return contrib.name;
    }

    @Override
    public void contributionUpdated(String id,
            RepositoryDirectoryDescriptor contrib,
            RepositoryDirectoryDescriptor newOrigContrib) {
        String name = contrib.name;
        if (contrib.remove) {
            contributionRemoved(id, contrib);
        } else {
            if (descriptors.containsKey(name)) {
                log.info("Directory registration updated: " + name);
            } else {
                log.info("Directory registered: " + name);
            }
            contrib.init();
            descriptors.put(id, contrib);
        }
    }

    @Override
    public void contributionRemoved(String id,
            RepositoryDirectoryDescriptor origContrib) {
        RepositoryDirectoryDescriptor desc = descriptors.get(id);
        if (desc != null) {
            try {
                descriptors.remove(id);
                desc.stop();
                log.info("Directory removed: " + id);
            } catch (DirectoryException e) {
                log.error(String.format(
                        "Error while shutting down directory '%s'", id), e);
            }
        } else {
            log.warn(String.format(
                    "Could not find repository directory descriptor '%s' to be removed",
                    id));
        }
    }

    @Override
    public RepositoryDirectoryDescriptor clone(
            RepositoryDirectoryDescriptor orig) {
        return orig.clone();
    }

    @Override
    public void merge(RepositoryDirectoryDescriptor src,
            RepositoryDirectoryDescriptor dst) {
        boolean remove = src.remove;
        // keep old remove info: if old contribution was removed, new one
        // should replace the old one completely
        if (remove) {
            dst.remove = remove;
            // don't bother merging
            return;
        }
    }

    public Directory getDirectory(String name) {
        if (descriptors.containsKey(name)) {
            return descriptors.get(name).repositoryDirectory;
        }
        return null;
    }
    
    public void startAll() {
        for (RepositoryDirectoryDescriptor desc : descriptors.values()) {
            desc.start();
        }
    }

    public Collection<? extends Directory> getDirectories() {
        List<Directory> res = new ArrayList<Directory>();
        for (RepositoryDirectoryDescriptor desc : descriptors.values()) {
            res.add(desc.repositoryDirectory);
        }
        return res;
    }

}
