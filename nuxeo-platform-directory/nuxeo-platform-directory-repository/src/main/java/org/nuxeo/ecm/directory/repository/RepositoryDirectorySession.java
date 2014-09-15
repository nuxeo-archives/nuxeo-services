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

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.DirectoryCache;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.api.DirectoryService;

/**
 * Session class for directory on repository
 * 
 *
 * @since 5.9.6
 */
public class RepositoryDirectorySession extends BaseSession {

    final protected DirectoryService directoryService;

    final protected RepositoryDirectory directory;

    final protected String schemaName;

    final protected String schemaIdField;

    final protected String schemaPasswordField;

    final protected CoreSession coreSession;

    final protected String createPath;

    private final static Log log = LogFactory.getLog(RepositoryDirectorySession.class);

    public RepositoryDirectorySession(RepositoryDirectory repositoryDirectory) {
        directoryService = RepositoryDirectoryFactory.getDirectoryService();
        this.directory = repositoryDirectory;
        schemaName = directory.getSchema();
        coreSession = CoreInstance.openCoreSession(directory.getDescriptor().repositoryName);
        schemaIdField = directory.getFieldMapper().getBackendField(
                directory.getIdField());
        schemaPasswordField = directory.getFieldMapper().getBackendField(
                directory.getPasswordField());

        createPath = directory.getDescriptor().createPath;
    }

    @Override
    public DocumentModel getEntry(String id) throws DirectoryException {
        return getEntry(id, false);
    }

    @Override
    public DocumentModel getEntry(String id, boolean fetchReferences)
            throws DirectoryException {
        DocumentModelList listDoc = coreSession.query("SELECT * FROM "
                + directory.getDescriptor().docType + " WHERE "
                + directory.getField(schemaIdField).getName().getPrefixedName()
                + " = '" + id + "'" + " AND ecm:path STARTSWITH '" + createPath
                + "'");
        // TODO : deal with references
        // TODO: deal with createPath
        if (!listDoc.isEmpty()) {
            // Should have only one
            if (listDoc.size() > 1) {
                log.warn(String.format("Found more than one result in getEntry, the first result only will be returned"));
            }
            return listDoc.get(0);
        }
        return null;
    }

    @Override
    public DocumentModelList getEntries() throws ClientException,
            DirectoryException {
        throw new UnsupportedOperationException();
    }

    private String getPrefixedFieldName(String fieldName) {
        Field schemaField = directory.getField(fieldName);
        return schemaField.getName().getPrefixedName();
    }

    @Override
    public DocumentModel createEntry(Map<String, Object> fieldMap)
            throws ClientException, DirectoryException {

        if (isReadOnly()) {
            return null;
        }

        // TODO : deal with encrypted password
        Map<String, Object> properties = new HashMap<String, Object>();
        for (String fieldId : fieldMap.keySet()) {
            String backendFieldId = directory.getFieldMapper().getBackendField(
                    fieldId);

            Object value = fieldMap.get(fieldId);
            properties.put(getPrefixedFieldName(backendFieldId), value);
        }

        final String rawid = (String) properties.get(getPrefixedFieldName(schemaIdField));
        if (rawid == null) {
            throw new DirectoryException(String.format(
                    "Entry is missing id field '%s'", schemaIdField));
        }

        String docType = directory.getDescriptor().docType;
        String path = directory.getDescriptor().createPath;
        String schema = directory.getSchema();

        DocumentModel docModel = coreSession.createDocumentModel(path, rawid,
                docType);

        docModel.setProperties(schema, properties);
        return coreSession.createDocument(docModel);

    }

    @Override
    public void updateEntry(DocumentModel docModel) throws ClientException,
            DirectoryException {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntry(DocumentModel docModel) throws ClientException,
            DirectoryException {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntry(String id) throws ClientException,
            DirectoryException {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntry(String id, Map<String, String> map)
            throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter)
            throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext) throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext, Map<String, String> orderBy)
            throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext, Map<String, String> orderBy,
            boolean fetchReferences) throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws DirectoryException {
        // coreSession.close();
    }

    @Override
    public List<String> getProjection(Map<String, Serializable> filter,
            String columnName) throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getProjection(Map<String, Serializable> filter,
            Set<String> fulltext, String columnName) throws ClientException,
            DirectoryException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAuthenticating() throws ClientException,
            DirectoryException {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(String username, String password)
            throws ClientException, DirectoryException {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIdField() throws ClientException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPasswordField() throws ClientException {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() throws ClientException {
        return directory.getDescriptor().readOnly;
    }

    @Override
    public boolean hasEntry(String id) throws ClientException {
        // TODO Auto-generated method stub
        // return false;
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentModel createEntry(DocumentModel entry)
            throws ClientException {
        Map<String, Object> fieldMap = entry.getProperties(schemaName);
        return createEntry(fieldMap);
    }

}
