package org.nuxeo.ecm.directory.repository;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Directory on top of repository descriptor
 * 
 *
 * @since 5.9.6
 */
@XObject(value = "directory")
public class RepositoryDirectoryDescriptor implements Cloneable {

    @XNode("@name")
    public String name;

    @XNode("schema")
    protected String schemaName;
    
    @XNode("docType")
    protected String docType;

    @XNode("idField")
    protected String idField;

    @XNode("passwordField")
    protected String passwordField;

    @XNode("readOnly")
    public Boolean readOnly;

    @XNode("querySizeLimit")
    public Integer querySizeLimit;

    @XNode("@remove")
    public boolean remove = false;

    @XNode("autoVersioning")
    public boolean autoVersioning = false;

    @XNode("repositoryName")
    protected String repositoryName = "default";
    
    @XNode("createPath")
    protected String createPath = "/";
    
    @XNodeMap(value = "fieldMapping", key = "@name", type = HashMap.class, componentType = String.class)
    public Map<String, String> fieldMapping = new HashMap<String, String>();

    
    @XNodeList(value = "acl", type = ACLDescriptor[].class, componentType = ACLDescriptor.class)
    protected ACLDescriptor[] acls;

    protected RepositoryDirectory repositoryDirectory;

    @Override
    public RepositoryDirectoryDescriptor clone() {
        RepositoryDirectoryDescriptor clone = new RepositoryDirectoryDescriptor();
        clone.name = name;
        clone.schemaName = schemaName;
        clone.idField = idField;
        clone.passwordField = passwordField;
        clone.readOnly = readOnly;
        clone.querySizeLimit = querySizeLimit;
        clone.remove = remove;
        clone.autoVersioning = autoVersioning;
        clone.repositoryName = repositoryName;
        clone.docType = docType;
        clone.createPath = createPath;
        clone.fieldMapping = fieldMapping;
        if (acls != null) {
            clone.acls = acls;
        }
        return clone;
    }

    public void merge(RepositoryDirectoryDescriptor other) {
        merge(other, false);
    }

    public void merge(RepositoryDirectoryDescriptor other, boolean overwrite) {
        if (other.schemaName != null || overwrite) {
            schemaName = other.schemaName;
        }
        if (other.docType != null || overwrite) {
            docType = other.docType;
        }
        if (other.idField != null || overwrite) {
            idField = other.idField;
        }
        if (other.passwordField != null || overwrite) {
            passwordField = other.passwordField;
        }
        if (other.readOnly != null || overwrite) {
            readOnly = other.readOnly;
        }
        if (other.querySizeLimit != null || overwrite) {
            querySizeLimit = other.querySizeLimit;
        }
        if (other.repositoryName != null || overwrite) {
            repositoryName = other.repositoryName;
        }
        if (other.createPath != null || overwrite) {
            createPath = other.createPath;
        }
        if (other.docType != null || overwrite) {
            docType = other.docType;
        }
        if (other.fieldMapping != null || overwrite) {
            fieldMapping = other.fieldMapping;
        }
        if (other.acls != null || overwrite) {
            if (acls == null) {
                acls = other.acls;
            } else {
                ACLDescriptor[] otherAcls = new ACLDescriptor[acls.length
                        + other.acls.length];
                System.arraycopy(acls, 0, otherAcls, 0, acls.length);
                System.arraycopy(other.acls, 0, otherAcls, acls.length,
                        other.acls.length);
                acls = otherAcls;
            }
        }
    }

    public void start() {
        repositoryDirectory = new RepositoryDirectory(this);
    }

    public void stop() {
        if (repositoryDirectory != null) {
            repositoryDirectory.shutdown();
            repositoryDirectory = null;
        }
    }

}
