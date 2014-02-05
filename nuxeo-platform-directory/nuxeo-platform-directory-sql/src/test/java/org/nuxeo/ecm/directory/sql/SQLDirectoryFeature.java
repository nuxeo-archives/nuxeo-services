package org.nuxeo.ecm.directory.sql;

import org.nuxeo.ecm.core.storage.sql.ra.PoolingRepositoryFactory;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.directory.DirectoryFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.SimpleFeature;

@Features({ TransactionalFeature.class, DirectoryFeature.class })
@Deploy("org.nuxeo.ecm.directory.sql")
@LocalDeploy({ "org.nuxeo.ecm.directory:test-sql-directories-schema-override.xml", "org.nuxeo.ecm.directory:test-schema.xml", "org.nuxeo.ecm.directory:test-directories.xml" })
@RepositoryConfig(repositoryFactoryClass = PoolingRepositoryFactory.class)
public class SQLDirectoryFeature extends SimpleFeature{

}
