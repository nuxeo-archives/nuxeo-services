package org.nuxeo.ecm.directory.memory;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("directory")
public class MemoryDirectoryDescriptor {

    @XNode("@name")
    public final String name = "default";

    @XNode("@schema")
    public final String schema = "none";

    @XNode("@idField")
    public final String idField = "id";
}
