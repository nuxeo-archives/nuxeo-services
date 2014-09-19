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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.storage.sql.jdbc.db.Table;
import org.nuxeo.ecm.core.storage.sql.jdbc.dialect.Dialect;
import org.nuxeo.ecm.directory.AbstractReference;
import org.nuxeo.ecm.directory.DirectoryEntryNotFoundException;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Reference;

/**
 * Class to handle reference
 *
 * @since 5.9.6
 */
@XObject(value = "repositoryDirectoryReference")
public class RepositoryDirectoryReference extends AbstractReference {

    @XNode("@field")
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    @XNode("@directory")
    public void setTargetDirectoryName(String targetDirectoryName) {
        this.targetDirectoryName = targetDirectoryName;
    }

    @XNode("@targetField")
    protected String targetField;

    private static final Log log = LogFactory.getLog(RepositoryDirectoryReference.class);


    String fieldName;


    public void addLinks(String sourceId, List<String> targetIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void addLinks(List<String> sourceIds, String targetId)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    protected interface Collector {
        List<String> collect(Reference dir) throws DirectoryException;
    }

    protected List<String> doCollect(Collector extractor)
            throws DirectoryException {
        Set<String> ids = new HashSet<String>();
        Reference ref = getSourceDirectory().getReference(fieldName);
        if (ref != null) {
            try {
                ids.addAll(extractor.collect(ref));
            } catch (DirectoryEntryNotFoundException e) {
                log.debug(e.getMessage());
            }
        }
        List<String> x = new ArrayList<String>(ids.size());
        x.addAll(ids);
        return x;
    }

    public List<String> getSourceIdsForTarget(final String targetId)
            throws DirectoryException {
        return doCollect(new Collector() {
            public List<String> collect(Reference ref)
                    throws DirectoryException {
                return ref.getSourceIdsForTarget(targetId);
            }
        });
    }

    public List<String> getTargetIdsForSource(final String sourceId)
            throws DirectoryException {
        return doCollect(new Collector() {
            public List<String> collect(Reference ref)
                    throws DirectoryException {
                return ref.getSourceIdsForTarget(sourceId);
            }
        });
    }

    public void removeLinksForSource(String sourceId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void removeLinksForTarget(String targetId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void setSourceIdsForTarget(String targetId, List<String> sourceIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void setTargetIdsForSource(String sourceId, List<String> targetIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected AbstractReference newInstance() {
        return new RepositoryDirectoryReference();
    }

    @Override
    public AbstractReference clone() {
        return super.clone();
    }

}
