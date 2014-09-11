/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.directory.AbstractDirectory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.DirectoryFieldMapper;
import org.nuxeo.ecm.directory.Reference;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.runtime.api.Framework;

/**
 * Implementation class of directory on top of a repository
 * 
 *
 * @since 5.9.6
 */
public class RepositoryDirectory extends AbstractDirectory {

    private final RepositoryDirectoryDescriptor descriptor;

    protected final Schema schema;

    public RepositoryDirectory(RepositoryDirectoryDescriptor descriptor)
            throws ClientException {
        super(descriptor.name);
        this.descriptor = descriptor;
        SchemaManager sm = Framework.getLocalService(SchemaManager.class);
        schema = sm.getSchema(descriptor.schemaName);
        fieldMapper = new DirectoryFieldMapper(descriptor.fieldMapping);
        if (schema == null) {
            throw new DirectoryException(String.format(
                    "Unknown schema '%s' for directory '%s' ",
                    descriptor.schemaName, name));
        }
    }

    public RepositoryDirectoryDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getName() {
        return descriptor.name;
    }

    @Override
    public String getSchema() {
        return descriptor.schemaName;
    }

    @Override
    public String getParentDirectory() {
        return null;
    }

    public Field getField(String name) throws DirectoryException{
        Field field = schema.getField(name);
        if (field == null) {
            throw new DirectoryException(String.format(
                    "Field '%s' does not exist in the schema '%s'", name,
                    schema.getName()));
        }
        return field;
    }

    @Override
    public String getIdField() {
        return descriptor.idField;
    }

    @Override
    public String getPasswordField() {
        return descriptor.passwordField;
    }

    @Override
    public Session getSession() throws DirectoryException {
        RepositoryDirectorySession session = new RepositoryDirectorySession(
                this);
        addSession(session);
        return session;
    }

    protected void addSession(RepositoryDirectorySession session) {
        sessions.add(session);
    }

    @Override
    public Reference getReference(String referenceFieldName) {
        return new RepositoryDirectoryReference(this, referenceFieldName);
    }

    @Override
    public void invalidateDirectoryCache() throws DirectoryException {
        getCache().invalidateAll();
    }

}
