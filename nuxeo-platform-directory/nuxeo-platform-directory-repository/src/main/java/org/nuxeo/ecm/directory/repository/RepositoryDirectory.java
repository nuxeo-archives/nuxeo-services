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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
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

    private static final Log log = LogFactory.getLog(RepositoryDirectory.class);

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

    public void start() {
        CoreSession coreSession = CoreInstance.openCoreSession(descriptor.repositoryName);
        String createPath = descriptor.createPath;

        DocumentModel rootFolder = null;
        try {
            rootFolder = coreSession.getDocument(new PathRef(createPath));
        } catch (ClientException e) {
            // Normal case
        }

        if (rootFolder == null) {

            String parentFolder = descriptor.createPath.substring(0,
                    createPath.lastIndexOf("/"));
            if (createPath.lastIndexOf("/") == 0) {
                parentFolder = "/";
            }
            String createFolder = descriptor.createPath.substring(
                    createPath.lastIndexOf("/") + 1, createPath.length());

            log.info(String.format(
                    "Root folder '%s' has not been found for the directory '%s' on the repository '%s', will create it with given ACL",
                    createPath, name, descriptor.repositoryName));
            if (descriptor.canCreateRootFolder) {
                try {
                    DocumentModel doc = coreSession.createDocumentModel(
                            parentFolder, createFolder, "Folder");
                    doc.setProperty("dublincore", "title", createFolder);
                    coreSession.createDocument(doc);
                    // Set ACL from descriptor
                    for (int i = 0; i < descriptor.acls.length; i++) {
                        String userOrGroupName = descriptor.acls[i].userOrGroupName;
                        String privilege = descriptor.acls[i].privilege;
                        boolean granted = descriptor.acls[i].granted;
                        setACL(doc, userOrGroupName, privilege, granted);
                    }

                } catch (ClientException e) {
                    throw new DirectoryException(
                            String.format(
                                    "The root folder '%s' can not be created under '%s' for the directory '%s' on the repository '%s',"
                                            + " please make sure you have set the right path or that the path exist",
                                    createFolder, parentFolder, name,
                                    descriptor.repositoryName), e);
                } finally {
                    coreSession.close();
                }
            } else
            {
                coreSession.close();
            }

        } else {
            log.info(String.format(
                    "Root folder '%s' has been found for the directory '%s' on the repository '%s', ACL will not be set",
                    createPath, name, descriptor.repositoryName));
        }
    }

    protected DocumentModel setACL(DocumentModel rootFolder,
            String userOrGroupName, String privilege, boolean granted) {
        ACP acp = rootFolder.getACP();
        ACL localACL = acp.getOrCreateACL();
        localACL.add(new ACE(userOrGroupName, privilege, granted));
        rootFolder.setACP(acp, true);

        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Set ACL on root folder '%s' : userOrGroupName = '%s', privilege = '%s' , granted = '%s' ",
                    rootFolder.getPathAsString(), userOrGroupName, privilege,
                    granted));
        }

        return rootFolder.getCoreSession().saveDocument(rootFolder);
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

    public Field getField(String name) throws DirectoryException {
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


}
