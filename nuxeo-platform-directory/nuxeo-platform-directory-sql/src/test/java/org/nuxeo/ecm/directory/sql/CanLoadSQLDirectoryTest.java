package org.nuxeo.ecm.directory.sql;


import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.management.jtajca.DatabaseConnectionMonitor;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.DirectoryFeature;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.runtime.jtajca.management.JtajcaManagementFeature;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(FeaturesRunner.class)
@Features( { SQLDirectoryFeature.class, JtajcaManagementFeature.class })
public class CanLoadSQLDirectoryTest {

    @Inject DatabaseConnectionMonitor monitor;

    @Named("users")
    @Inject Directory directory;

    @Named("users")
    @Inject Session users;

    @Inject DirectoryFeature directories;

    @Before public void isInjected() {
        Assert.assertThat(users, Matchers.notNullValue());
    }

    @Test public void hasAdministrator() throws DirectoryException {
       checkAdministrator(users);
    }

    protected void checkAdministrator(Session users) throws DirectoryException {
        DocumentModel admin = users.getEntry("Administrator");
           Assert.assertThat(admin, Matchers.notNullValue());
    }

    @Test public void doesNotLeak() throws DirectoryException, ClientException {
       int initialCount = monitor.getNumActive();
       Session users = directory.getSession();
        try {
            checkAdministrator(users);
            Assert.assertThat(monitor.getNumActive(),
                    Matchers.is(initialCount));
        } finally {
            users.close();
        }
    }
}
