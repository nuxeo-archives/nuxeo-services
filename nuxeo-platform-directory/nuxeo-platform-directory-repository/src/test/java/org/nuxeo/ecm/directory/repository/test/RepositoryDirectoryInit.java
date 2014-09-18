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

import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.WRITE;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.annotations.RepositoryInit;

/**
 * Default repository initializer that create the default DM doc hierarchy.
 */
public class RepositoryDirectoryInit implements RepositoryInit {

    public static String ROOT_FOLDER_PATH = "/rootFolder";
    
    
    public static String DOC_ID_USER1 = "user1";
    public static String DOC_PWD_USER1 = "foo1";
    public static String DOC_ID_USER2 = "user2";
    public static String DOC_PWD_USER2 = "foo2";
    
    public static String USERS_RESTRICTED_FOLDER = "users-restricted";
    public static String USERS_UNRESTRICTED_FOLDER = "users-unrestricted";
    
    public static String USERS_RESTRICTED_PATH = ROOT_FOLDER_PATH+ "/test/"+USERS_RESTRICTED_FOLDER; 
    
    public static String USERS_UNRESTRICTED_PATH = ROOT_FOLDER_PATH+ "/test/"+USERS_UNRESTRICTED_FOLDER; 
    
    
    
    @Override
    public void populate(CoreSession session) throws ClientException {
        //RootFolder should have been bootstrapped by the directory on repository bundle
        DocumentModel doc = session.createDocumentModel(ROOT_FOLDER_PATH,
                "test", "Workspace");
        doc.setProperty("dublincore", "title", "test");
        doc = session.createDocument(doc);
        
        doc = createDocument(session,
                ROOT_FOLDER_PATH+"/test", USERS_RESTRICTED_FOLDER, "Folder");
        doc = removeAllPermission(doc);
        //user_2 has no permission on it
        doc = applyPermission(doc, WRITE, true, RepositoryDirectoryFeature.USER1_NAME);
        doc = applyPermission(doc, READ, false, RepositoryDirectoryFeature.USER2_NAME);
        doc = applyPermission(doc, WRITE, false, RepositoryDirectoryFeature.USER2_NAME);
        doc = applyPermission(doc, SecurityConstants.EVERYTHING, false, RepositoryDirectoryFeature.USER2_NAME);

        //Create a User1 doc for unit test
        DocumentModel user1 = createDocument(session, doc.getPathAsString(), "User1", "RepoDirDoc");
        user1.setProperty("schema1", "uid", DOC_ID_USER1);
        user1.setProperty("schema1", "foo", DOC_PWD_USER1);
        user1.setProperty("schema1", "bar", "bar1");
        session.saveDocument(user1);
        
        doc = createDocument(session,
                ROOT_FOLDER_PATH+"/test", USERS_UNRESTRICTED_FOLDER, "Folder");
        
        removeAllPermission(doc);
        //Both User1 & User2 have write access
        applyPermission(doc, WRITE, true, RepositoryDirectoryFeature.USER1_NAME);
        applyPermission(doc, WRITE, true, RepositoryDirectoryFeature.USER2_NAME);
        
        //Create a User2 doc for unit test
        DocumentModel user2 = createDocument(session, doc.getPathAsString(), "User2", "RepoDirDoc");
        user2.setProperty("schema1", "uid", DOC_ID_USER2);
        user2.setProperty("schema1", "foo", DOC_PWD_USER2);
        user2.setProperty("schema1", "bar", "bar2");
        session.saveDocument(user2);

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

        return docDomain;
    }

    protected DocumentModel removeAllPermission(DocumentModel docModel) {
        ACP acp = docModel.getACP();

        ACL[] ACLs = acp.getACLs();
        for (int i = 0; i < ACLs.length; i++) {
            acp.removeACL(ACLs[i].getName());
        }
        // which is dynamically computed
        docModel.setACP(acp, true);
        
        return docModel.getCoreSession().saveDocument(docModel);
    }

    protected DocumentModel applyPermission(DocumentModel docModel, String privilege,
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

        return docModel.getCoreSession().saveDocument(docModel);

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
