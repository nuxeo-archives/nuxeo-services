package org.nuxeo.ecm.directory.repository;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "acl")
public class ACLDescriptor implements Cloneable {

    @XNode("@userName")
    public String userName;
    
    @XNode("@read")
    public boolean read = false;
    
    @XNode("@write")
    public boolean write = false;

}
