/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.platform.types.localconfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.platform.types.localconfiguration.UITypesConfigurationConstants.UI_TYPES_CONFIGURATION_ALLOWED_TYPES_PROPERTY;
import static org.nuxeo.ecm.platform.types.localconfiguration.UITypesConfigurationConstants.UI_TYPES_CONFIGURATION_DENIED_TYPES_PROPERTY;
import static org.nuxeo.ecm.platform.types.localconfiguration.UITypesConfigurationConstants.UI_TYPES_CONFIGURATION_DENY_ALL_TYPES_PROPERTY;
import static org.nuxeo.ecm.platform.types.localconfiguration.UITypesConfigurationConstants.UI_TYPES_CONFIGURATION_FACET;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.localconfiguration.LocalConfigurationService;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.RepositorySettings;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = LocalConfigurationRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.types.api",
        "org.nuxeo.ecm.platform.types.core" })
@LocalDeploy("org.nuxeo.ecm.platform.types.core:test-ui-types-local-configuration.xml")
public class TestLocalConfiguration {

    public static final DocumentRef PARENT_WORKSPACE_REF = new PathRef(
            "/default-domain/workspaces/workspace");

    public static final DocumentRef CHILD_WORKSPACE_REF = new PathRef(
            "/default-domain/workspaces/workspace/workspace2");

    public static final String WORKSPACE_TYPE = "Workspace";

    public static final String FOLDER_TYPE = "Folder";

    public static final String SECTION_TYPE = "Section";

    public static final String FILE_TYPE = "File";

    public static final String NOTE_TYPE = "Note";

    public static final String SIMPLE_DOCUMENT_CATEGORY = "SimpleDocument";

    public static final String COLLABORATIVE_CATEGORY = "Collaborative";

    @Inject
    protected RepositorySettings settings;

    @Inject
    protected CoreSession session;

    @Inject
    protected TypeManager typeManager;

    @Inject
    protected LocalConfigurationService localConfigurationService;

    @Test
    public void shouldNotTakeIntoAccountConfigurationIfNoCurrentDocumentIsGiven()
            throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDeniedTypes(workspace, FOLDER_TYPE, FILE_TYPE);

        assertTrue(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                workspace.getType()));
        assertTrue(typeManager.isAllowedSubType(FOLDER_TYPE,
                workspace.getType()));
        assertTrue(typeManager.isAllowedSubType(FILE_TYPE, workspace.getType()));
        assertTrue(typeManager.isAllowedSubType(NOTE_TYPE, workspace.getType()));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                workspace.getType(), workspace));

        assertTrue(typeManager.canCreate(WORKSPACE_TYPE, workspace.getType()));
        assertTrue(typeManager.canCreate(FOLDER_TYPE, workspace.getType()));
        assertTrue(typeManager.canCreate(FILE_TYPE, workspace.getType()));
        assertTrue(typeManager.canCreate(NOTE_TYPE, workspace.getType()));
        assertFalse(typeManager.canCreate(SECTION_TYPE, workspace.getType(),
                workspace));
    }

    @Test
    public void shouldNotAllowAnyType() throws Exception {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDenyAllTypes(workspace, true);

        LocalConfigurationService localConfigurationService = Framework.getService(LocalConfigurationService.class);
        UITypesConfiguration configuration = localConfigurationService.getConfiguration(
                UITypesConfiguration.class, UI_TYPES_CONFIGURATION_FACET,
                workspace);
        assertTrue(configuration.denyAllTypes());

        assertFalse(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                workspace.getType(), workspace));
        assertFalse(typeManager.isAllowedSubType(FOLDER_TYPE,
                workspace.getType(), workspace));
        assertFalse(typeManager.isAllowedSubType(FILE_TYPE,
                workspace.getType(), workspace));
        assertFalse(typeManager.isAllowedSubType(NOTE_TYPE,
                workspace.getType(), workspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                workspace.getType(), workspace));

        assertFalse(typeManager.canCreate(WORKSPACE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(FOLDER_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(FILE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(NOTE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE, workspace.getType(),
                workspace));
    }

    protected void setDenyAllTypes(DocumentModel doc, boolean denyAllTypes)
            throws ClientException {
        doc.setPropertyValue(UI_TYPES_CONFIGURATION_DENY_ALL_TYPES_PROPERTY,
                Boolean.valueOf(denyAllTypes));
        session.saveDocument(doc);
        session.save();
    }

    protected void setAllowedTypes(DocumentModel doc, String... allowedTypes)
            throws ClientException {
        doc.setPropertyValue(UI_TYPES_CONFIGURATION_ALLOWED_TYPES_PROPERTY,
                (Serializable) Arrays.asList(allowedTypes));
        session.saveDocument(doc);
        session.save();
    }

    protected void setDeniedTypes(DocumentModel doc, String... deniedTypes)
            throws ClientException {
        doc.setPropertyValue(UI_TYPES_CONFIGURATION_DENIED_TYPES_PROPERTY,
                (Serializable) Arrays.asList(deniedTypes));
        session.saveDocument(doc);
        session.save();
    }

    @Test
    public void shouldNotAllowFolderType() throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDeniedTypes(workspace, FOLDER_TYPE);

        assertFalse(typeManager.isAllowedSubType(FOLDER_TYPE,
                workspace.getType(), workspace));
        assertTrue(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                workspace.getType(), workspace));
        assertTrue(typeManager.isAllowedSubType(FILE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.isAllowedSubType(NOTE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                workspace.getType(), workspace));

        assertFalse(typeManager.canCreate(FOLDER_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(WORKSPACE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(FILE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(NOTE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE, workspace.getType(),
                workspace));
    }

    @Test
    public void deniedTypesShouldPassFirst() throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        setAllowedTypes(workspace, FOLDER_TYPE);
        setDeniedTypes(workspace, FOLDER_TYPE);

        assertFalse(typeManager.isAllowedSubType(FOLDER_TYPE,
                workspace.getType(), workspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                workspace.getType(), workspace));

        assertFalse(typeManager.canCreate(FOLDER_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE, workspace.getType(),
                workspace));
    }

    @Test
    public void noConfigurationShouldAllowEveryRegisteredSubTypes()
            throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);

        assertTrue(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                workspace.getType(), workspace));
        assertTrue(typeManager.isAllowedSubType(FOLDER_TYPE,
                workspace.getType(), workspace));
        assertTrue(typeManager.isAllowedSubType(FILE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.isAllowedSubType(NOTE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                workspace.getType(), workspace));

        assertTrue(typeManager.canCreate(WORKSPACE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(FOLDER_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(FILE_TYPE, workspace.getType(),
                workspace));
        assertTrue(typeManager.canCreate(NOTE_TYPE, workspace.getType(),
                workspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE, workspace.getType(),
                workspace));
    }

    @Test
    public void shouldNotAllowAnyTypeIfParentConfigurationDoNotAllowAnyType()
            throws ClientException {
        DocumentModel parentWorkspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDenyAllTypes(parentWorkspace, true);
        DocumentModel childWorkspace = session.getDocument(CHILD_WORKSPACE_REF);
        setAllowedTypes(childWorkspace, FOLDER_TYPE, FILE_TYPE);

        assertFalse(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(FOLDER_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(FILE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(NOTE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));

        assertFalse(typeManager.canCreate(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.canCreate(FOLDER_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.canCreate(FILE_TYPE, childWorkspace.getType(),
                childWorkspace));
        assertFalse(typeManager.canCreate(NOTE_TYPE, childWorkspace.getType(),
                childWorkspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));
    }

    @Test
    public void shouldInheritDeniedTypes() throws ClientException {
        DocumentModel parentWorkspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDeniedTypes(parentWorkspace, FILE_TYPE);
        DocumentModel childWorkspace = session.getDocument(CHILD_WORKSPACE_REF);
        setDeniedTypes(childWorkspace, FOLDER_TYPE);

        assertFalse(typeManager.isAllowedSubType(FOLDER_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(FILE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertTrue(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertTrue(typeManager.isAllowedSubType(NOTE_TYPE,
                childWorkspace.getType(), childWorkspace));

        assertFalse(typeManager.canCreate(FOLDER_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.canCreate(FILE_TYPE, childWorkspace.getType(),
                childWorkspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertTrue(typeManager.canCreate(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertTrue(typeManager.canCreate(NOTE_TYPE, childWorkspace.getType(),
                childWorkspace));
    }

    @Test
    public void shouldNotInheritAllowedTypes() throws ClientException {
        DocumentModel parentWorkspace = session.getDocument(PARENT_WORKSPACE_REF);
        setAllowedTypes(parentWorkspace, FILE_TYPE);
        DocumentModel childWorkspace = session.getDocument(CHILD_WORKSPACE_REF);
        setAllowedTypes(childWorkspace, FOLDER_TYPE);

        assertTrue(typeManager.isAllowedSubType(FOLDER_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(FILE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.isAllowedSubType(NOTE_TYPE,
                childWorkspace.getType(), childWorkspace));

        assertTrue(typeManager.canCreate(FOLDER_TYPE, childWorkspace.getType(),
                childWorkspace));
        assertFalse(typeManager.canCreate(FILE_TYPE, childWorkspace.getType(),
                childWorkspace));
        assertFalse(typeManager.canCreate(SECTION_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.canCreate(WORKSPACE_TYPE,
                childWorkspace.getType(), childWorkspace));
        assertFalse(typeManager.canCreate(NOTE_TYPE, childWorkspace.getType(),
                childWorkspace));
    }

    @Test
    public void validateTypeMapForWorkspace() {
        Map<String, List<Type>> typeMapForDocumentType = typeManager.getTypeMapForDocumentType(
                WORKSPACE_TYPE, null);
        assertNotNull(typeMapForDocumentType);
        assertFalse(typeMapForDocumentType.isEmpty());

        assertEquals(2, typeMapForDocumentType.size());
        assertTrue(typeMapForDocumentType.containsKey(SIMPLE_DOCUMENT_CATEGORY));
        assertTrue(typeMapForDocumentType.containsKey(COLLABORATIVE_CATEGORY));

        List<Type> types = typeMapForDocumentType.get(SIMPLE_DOCUMENT_CATEGORY);
        List<String> typesNames = convertToTypesNames(types);
        assertEquals(2, types.size());
        assertTrue(typesNames.contains(FILE_TYPE));
        assertTrue(typesNames.contains(NOTE_TYPE));

        types = typeMapForDocumentType.get(COLLABORATIVE_CATEGORY);
        typesNames = convertToTypesNames(types);
        assertEquals(2, types.size());
        assertTrue(typesNames.contains(WORKSPACE_TYPE));
        assertTrue(typesNames.contains(FOLDER_TYPE));
    }

    protected List<String> convertToTypesNames(Collection<Type> types) {
        List<String> typesList = new ArrayList<String>();
        for (Type type : types) {
            typesList.add(type.getId());
        }
        return typesList;
    }

    @Test
    public void shouldFindAllAllowedSubtypesWithoutConfiguration()
            throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        Collection<Type> allowedSubTypes = typeManager.findAllAllowedSubTypesFrom(
                WORKSPACE_TYPE, workspace);

        assertNotNull(allowedSubTypes);
        assertFalse(allowedSubTypes.isEmpty());
        assertEquals(4, allowedSubTypes.size());

        List<String> typesNames = convertToTypesNames(allowedSubTypes);
        assertTrue(typesNames.contains(WORKSPACE_TYPE));
        assertTrue(typesNames.contains(FOLDER_TYPE));
        assertTrue(typesNames.contains(FILE_TYPE));
        assertTrue(typesNames.contains(NOTE_TYPE));
        assertFalse(typesNames.contains(SECTION_TYPE));
    }

    @Test
    public void userWithoutReadRightOnWorkspaceShouldRetrieveConfiguration()
            throws ClientException {
        DocumentModel workspace = session.getDocument(PARENT_WORKSPACE_REF);
        setDeniedTypes(workspace, FILE_TYPE);

        addReadForEveryone(CHILD_WORKSPACE_REF);

        try (CoreSession newSession = openSessionAs("user1")) {
            DocumentModel childWorkspace = newSession.getDocument(CHILD_WORKSPACE_REF);
            assertTrue(typeManager.isAllowedSubType(FOLDER_TYPE,
                    childWorkspace.getType(), childWorkspace));
            assertTrue(typeManager.isAllowedSubType(WORKSPACE_TYPE,
                    childWorkspace.getType(), childWorkspace));
            assertTrue(typeManager.isAllowedSubType(NOTE_TYPE,
                    childWorkspace.getType(), childWorkspace));
            assertFalse(typeManager.isAllowedSubType(FILE_TYPE,
                    childWorkspace.getType(), childWorkspace));
        }
    }

    protected void addReadForEveryone(DocumentRef ref) throws ClientException {
        DocumentModel childWorkspace = session.getDocument(ref);
        ACP acp = childWorkspace.getACP();
        ACL acl = acp.getOrCreateACL();
        acl.clear();
        acl.add(new ACE(SecurityConstants.EVERYONE, SecurityConstants.READ,
                true));
        childWorkspace.setACP(acp, true);
        session.saveDocument(childWorkspace);
        session.save();
    }

    protected CoreSession openSessionAs(String username) throws ClientException {
        return settings.openSessionAs(username);
    }

}
