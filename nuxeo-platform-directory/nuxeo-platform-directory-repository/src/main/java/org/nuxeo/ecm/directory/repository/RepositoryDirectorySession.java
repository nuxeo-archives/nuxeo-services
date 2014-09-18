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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.directory.BaseSession;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.PasswordHelper;
import org.nuxeo.ecm.directory.Reference;
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

    final protected String docType;

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
        docType = directory.getDescriptor().docType;
        createPath = directory.getDescriptor().createPath;
    }

    @Override
    public DocumentModel getEntry(String id) throws DirectoryException {
        return getEntry(id, false);
    }

    @Override
    public DocumentModel getEntry(String id, boolean fetchReferences)
            throws DirectoryException {

        StringBuilder sbQuery = new StringBuilder("SELECT * FROM ");
        sbQuery.append(docType);
        sbQuery.append(" WHERE ");
        sbQuery.append(directory.getField(schemaIdField).getName().getPrefixedName());
        sbQuery.append(" = '");
        sbQuery.append(id);
        sbQuery.append("' AND ecm:path STARTSWITH '");
        sbQuery.append(createPath);
        sbQuery.append("'");

        DocumentModelList listDoc = coreSession.query(sbQuery.toString());
        // TODO : deal with references
        if (!listDoc.isEmpty()) {
            // Should have only one
            if (listDoc.size() > 1) {
                log.warn(String.format("Found more than one result in getEntry, the first result only will be returned"));
            }
            DocumentModel docResult = listDoc.get(0);
            if (isReadOnly()) {
                BaseSession.setReadOnlyEntry(docResult);
            }
            return docResult;
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
    @SuppressWarnings("unchecked")
    public DocumentModel createEntry(Map<String, Object> fieldMap)
            throws ClientException, DirectoryException {

        if (isReadOnly()) {
            log.warn(String.format(
                    "The directory '%s' is in read-only mode, could not create entry.",
                    directory.name));
            return null;
        }
        // TODO : deal with auto-versionning 
        // TODO : deal with encrypted password
        // TODO : deal with references 
        Map<String, Object> properties = new HashMap<String, Object>();
        List<String> createdRefs = new LinkedList<String>();
        for (String fieldId : fieldMap.keySet()) {
            if (directory.isReference(fieldId)) {
                createdRefs.add(fieldId);
            }
            Object value = fieldMap.get(fieldId);
            properties.put(getMappedPrefixedFieldName(fieldId), value);
        }

        final String rawid = (String) properties.get(getPrefixedFieldName(schemaIdField));
        if (rawid == null) {
            throw new DirectoryException(String.format(
                    "Entry is missing id field '%s'", schemaIdField));
        }

        DocumentModel docModel = coreSession.createDocumentModel(createPath,
                rawid, docType);

        docModel.setProperties(schemaName, properties);
        DocumentModel createdDoc = coreSession.createDocument(docModel);

        for (String referenceFieldName : createdRefs) {
            Reference reference = directory.getReference(referenceFieldName);
            List<String> targetIds = (List<String>) createdDoc.getProperty(
                    schemaName, referenceFieldName);
            reference.setTargetIdsForSource(docModel.getId(), targetIds);
        }
        return docModel;

    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateEntry(DocumentModel docModel) throws ClientException,
            DirectoryException {
        if (isReadOnly()) {
            log.warn(String.format(
                    "The directory '%s' is in read-only mode, could not update entry.",
                    directory.name));
        } else {

            if (!isReadOnlyEntry(docModel)) {

                String id = (String) docModel.getProperty(schemaName,
                        getIdField());
                if (id == null) {
                    throw new DirectoryException(
                            "Can not update entry with a null id for document ref "
                                    + docModel.getRef());
                } else {
                    if (getEntry(id) == null) {
                        throw new DirectoryException(
                                String.format(
                                        "Update entry failed : Entry with id '%s' not found !",
                                        id));
                    } else {

                        DataModel dataModel = docModel.getDataModel(schemaName);
                        Map<String, Object> updatedProps = new HashMap<String, Object>();
                        List<String> updatedRefs = new LinkedList<String>();

                        for (String field : docModel.getProperties(schemaName).keySet()) {
                            String schemaField = getMappedPrefixedFieldName(field);
                            if (!dataModel.isDirty(schemaField)) {
                                if (directory.isReference(field)) {
                                    updatedRefs.add(field);
                                } else {
                                    updatedProps.put(
                                            schemaField,
                                            docModel.getProperties(schemaName).get(
                                                    field));
                                }
                            }

                        }

                        docModel.setProperties(schemaName, updatedProps);

                        // update reference fields
                        for (String referenceFieldName : updatedRefs) {
                            Reference reference = directory.getReference(referenceFieldName);
                            List<String> targetIds = (List<String>) docModel.getProperty(
                                    schemaName, referenceFieldName);
                            reference.setTargetIdsForSource(docModel.getId(),
                                    targetIds);
                        }

                        coreSession.saveDocument(docModel);
                    }

                }
            }
        }
    }

    @Override
    public void deleteEntry(DocumentModel docModel) throws ClientException,
            DirectoryException {
        String id = (String) docModel.getProperty(schemaName, schemaIdField);
        deleteEntry(id);
    }

    @Override
    public void deleteEntry(String id) throws ClientException,
            DirectoryException {
        if (isReadOnly()) {
            log.warn(String.format(
                    "The directory '%s' is in read-only mode, could not delete entry.",
                    directory.name));
        } else {
            if (id == null) {
                throw new DirectoryException(
                        "Can not update entry with a null id ");
            } else {
                DocumentModel docModel = getEntry(id);
                if (docModel != null)
                    coreSession.removeDocument(docModel.getRef());
            }

        }
    }

    @Override
    public void deleteEntry(String id, Map<String, String> map)
            throws ClientException, DirectoryException {
        if (isReadOnly()) {
            log.warn(String.format(
                    "The directory '%s' is in read-only mode, could not delete entry.",
                    directory.name));
        }

        Map<String, Serializable> props = new HashMap<String, Serializable>(map);
        props.put(schemaIdField, id);

        DocumentModelList docList = query(props);
        if (!docList.isEmpty()) {
            if (docList.size() > 1) {
                log.warn(String.format("Found more than one result in getEntry, the first result only will be deleted"));
            }
            deleteEntry(docList.get(0));
        } else {
            throw new DirectoryException(String.format(
                    "Delete entry failed : Entry with id '%s' not found !", id));
        }

    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter)
            throws ClientException {
        Set<String> emptySet = Collections.emptySet();
        return query(filter, emptySet);
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext, Map<String, String> orderBy)
            throws ClientException {
        // XXX not fetch references by default: breaks current behavior
        return query(filter, fulltext, orderBy, false);
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext, Map<String, String> orderBy,
            boolean fetchReferences) throws ClientException {
        return query(filter, fulltext, orderBy, fetchReferences, 0, 0);
    }

    protected String getMappedPrefixedFieldName(String fieldName) {
        String backendFieldId = directory.getFieldMapper().getBackendField(
                fieldName);
        return getPrefixedFieldName(backendFieldId);
    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext, Map<String, String> orderBy,
            boolean fetchReferences, int limit, int offset)
            throws ClientException, DirectoryException {
        StringBuilder sbQuery = new StringBuilder("SELECT * FROM ");
        sbQuery.append(docType);
        // TODO deal with fetch ref
        if (!filter.isEmpty() || !fulltext.isEmpty()) {
            sbQuery.append(" WHERE ");
        }
        int i = 1;
        for (String filterKey : filter.keySet()) {

            sbQuery.append(getMappedPrefixedFieldName(filterKey));
            sbQuery.append(" = ");
            sbQuery.append("'");
            sbQuery.append(filter.get(filterKey));
            sbQuery.append("'");
            if (i < filter.size()) {
                sbQuery.append(" AND ");
                i++;
            }

        }
        if (filter.size() > 0 && fulltext.size() > 0) {
            sbQuery.append(" AND ");
        }
        if (fulltext.size() > 0) {
            sbQuery.append("ecm:fulltext");
            sbQuery.append(" = ");
            sbQuery.append("'");
            for (String fullTextKey : fulltext) {
                sbQuery.append(fullTextKey);
                sbQuery.append(" ");
            }
            sbQuery.append("'");
        }

        // Filter facetFilter = new FacetFilter(FacetNames.VERSIONABLE, true);
        
        DocumentModelList resultsDoc = coreSession.query(sbQuery.toString(), null, new Long(limit),
                new Long(offset), false);
        
        if(isReadOnly())
        {
            for (DocumentModel documentModel : resultsDoc) {
                BaseSession.setReadOnlyEntry(documentModel);
            }
        }
        
        return resultsDoc;

    }

    @Override
    public DocumentModelList query(Map<String, Serializable> filter,
            Set<String> fulltext) throws ClientException, DirectoryException {
        return query(filter, fulltext, new HashMap<String, String>());
    }

    @Override
    public void close() throws DirectoryException {
        coreSession.close();
        directory.removeSession(this);
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
    public boolean authenticate(String username, String password)
            throws ClientException {
        DocumentModel entry = getEntry(username);
        if (entry == null) {
            return false;
        }
        String storedPassword = (String) entry.getProperty(schemaName,
                schemaPasswordField);
        return PasswordHelper.verifyPassword(password, storedPassword);
    }

    @Override
    public boolean isAuthenticating() throws ClientException {
        return schemaPasswordField != null;
    }

    @Override
    public String getIdField() throws ClientException {
        return directory.getDescriptor().idField;
    }

    @Override
    public String getPasswordField() throws ClientException {
        return directory.getDescriptor().passwordField;
    }

    @Override
    public boolean isReadOnly() throws ClientException {
        return directory.getDescriptor().readOnly;
    }

    @Override
    public boolean hasEntry(String id) throws ClientException {
        return getEntry(id) != null;
    }

    @Override
    public DocumentModel createEntry(DocumentModel entry)
            throws ClientException {
        Map<String, Object> fieldMap = entry.getProperties(schemaName);
        return createEntry(fieldMap);
    }

}
