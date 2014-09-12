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

import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;

import javax.inject.Inject;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.test.annotations.RepositoryInit;
import org.nuxeo.ecm.platform.usermanager.UserManager;

/**
 * Default repository initializer that create the default DM doc hierarchy.
 */
public class RepositoryDirectoryInit implements RepositoryInit {

    @Inject
    UserManager um;

    @Override
    public void populate(CoreSession session) throws ClientException {
        // TODO: bootstrap user, groups, folder, setup acl etc.

        createDomain(session, "default-domain", "Default domain");

        DocumentModel docDomain = createDomain(session, "restricted-domain",
                "Resricted domain");
        removeAllPermission(docDomain);
        applyPermission(docDomain, WRITE, true, "user_1");

        DocumentModel doc = createDocument(session,
                docDomain.getPathAsString(), "users", "WorkspaceRoot");
        createDocument(session, doc.getPathAsString(), "User1", "RepoDirDoc");

        docDomain = createDomain(session, "unrestricted-domain",
                "Unrestricted domain");
        doc = createDocument(session, docDomain.getPathAsString(), "users",
                "WorkspaceRoot");
        createDocument(session, doc.getPathAsString(), "User2", "RepoDirDoc");

    }

    public DocumentModel createDocument(CoreSession session, String parentPath,
            String docName, String docType) {
        DocumentModel doc = session.createDocumentModel(parentPath, docName,
                docType);
        doc.setProperty("dublincore", "title", docType);
        return session.createDocument(doc);
    }

    public DocumentModel createDomain(CoreSession session, String domainName,
            String domainTitle) {
        DocumentModel doc = session.createDocumentModel("/", domainName,
                "Domain");
        doc.setProperty("dublincore", "title", domainTitle);
        doc = session.createDocument(doc);
        DocumentModel docDomain = doc;

        doc = session.createDocumentModel("/" + domainName + "/", "workspaces",
                "WorkspaceRoot");
        doc.setProperty("dublincore", "title", "Workspaces");
        doc = session.createDocument(doc);

        doc = session.createDocumentModel("/" + domainName + "/", "sections",
                "SectionRoot");
        doc.setProperty("dublincore", "title", "Workspaces");
        doc = session.createDocument(doc);

        doc = session.createDocumentModel("/" + domainName + "/", "templates",
                "TemplateRoot");
        doc.setProperty("dublincore", "title", "Templates");
        doc.setProperty("dublincore", "description",
                "Root of workspaces templates");
        doc = session.createDocument(doc);

        doc = session.createDocumentModel("/" + domainName + "/workspaces",
                "test", "Workspace");
        doc.setProperty("dublincore", "title", "workspace");
        doc = session.createDocument(doc);

        return docDomain;
    }

    protected void removeAllPermission(DocumentModel docModel) {
        ACP acp = docModel.getACP();

        ACL[] ACLs = acp.getACLs();
        for (int i = 0; i < ACLs.length; i++) {
            acp.removeACL(ACLs[i].getName());
        }
        // which is dynamically computed
        docModel.setACP(acp, true);
    }

    protected void applyPermission(DocumentModel docModel, String privilege,
            boolean granted, String userOrGroupName) {
        // if (!isAdministrator()) {
        // throw new DocumentSecurityException(
        // "You need to be an Administrator to do this.");
        // }

        // ACP acp = new ACPImpl();
        // UserEntry userEntry = new UserEntryImpl(userOrGroupName);
        // userEntry.addPrivilege(privilege);
        // acp.setRules(new UserEntry[] { userEntry });
        //
        // docModel.setACP(acp, true);

        ACP acp = docModel.getACP();
        ACL localACL = acp.getOrCreateACL();
        localACL.add(new ACE(userOrGroupName, privilege, granted));
        docModel.setACP(acp, true);

        docModel.getCoreSession().save();

        // doc = session.getDocument(new PathRef("/testACPInheritance/folder"));
        // acp = doc.getACP();
        // ACL acl = acp.getACL(ACL.INHERITED_ACL);
        //
        // assertEquals("joe_reader", acl.getACEs()[0].getUsername());
        //
        // // block inheritance
        // acp.getOrCreateACL().add(
        // new ACE(SecurityConstants.EVERYONE,
        // SecurityConstants.EVERYTHING, false));
        // doc.setACP(acp, true);
        // session.save();
        //
        // acl = acp.getACL(ACL.INHERITED_ACL);
    }

}
