package org.nuxeo.ecm.platform.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.storage.sql.ra.PoolingRepositoryFactory;
import org.nuxeo.ecm.core.test.RepositorySettings;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.api.DataSourceHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.nuxeo.runtime.transaction.TransactionRuntimeException;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ TransactionalFeature.class, PlatformFeature.class })
@RepositoryConfig(repositoryFactoryClass = PoolingRepositoryFactory.class, cleanup = Granularity.METHOD)
public class CanTwoPhaseCommitTest {
    @Inject
    CoreSession session;

    @Inject
    RepositorySettings settings;

    public void rollbackRepositoryChange() throws ClientException {
        DocumentModel doc = session.createDocumentModel("/", "marker",
                "Document");
        doc = session.createDocument(doc);
        TransactionHelper.setTransactionRollbackOnly();
        TransactionHelper.commitOrRollbackTransaction();
        settings.releaseSession();
        session = settings.createSession();
        assertFalse(session.exists(new PathRef("/marker")));
    }

    public static class AnyXid extends ArgumentMatcher<Xid> {

        @Override
        public boolean matches(Object argument) {
            return argument instanceof Xid;
        }

    }

    public static Xid anyXid() {
        return Matchers.argThat(new AnyXid());
    }

    public static XAResource mockFailOnPrepare() throws XAException {
        XAResource mock = Mockito.mock(XAResource.class);
        Mockito.when(mock.prepare(anyXid())).thenThrow(new XAException(XAException.XA_RBOTHER));
        return mock;
    }

    @Test
    public void xaDatasourceDontAutocommit() throws NamingException, SQLException {
        DataSource ds = DataSourceHelper.getDataSource("nuxeo");
        Connection cx = ds.getConnection();
        try {
            assertFalse(cx.getAutoCommit());
        } finally {
            cx.close();
        }
    }

    @Test
    public void systemRollbackDirty() throws ClientException, SystemException, NamingException, IllegalStateException, RollbackException, XAException {
        DocumentModel doc = session.createDocumentModel("/", "marker",
                "Document");
        doc = session.createDocument(doc);
        rollbackPrepared();
    }

    @Test
    public void systemRollbackReadonly() throws ClientException, SystemException, NamingException, IllegalStateException, RollbackException, XAException {
        session.getRootDocument();
        rollbackPrepared();
    }

    protected void rollbackPrepared() throws SystemException, NamingException, IllegalStateException, RollbackException, XAException {
        Transaction tx = TransactionHelper.lookupTransactionManager().getTransaction();
        tx.enlistResource(mockFailOnPrepare());
        boolean caught = false;
        try {
            TransactionHelper.commitOrRollbackTransaction();
        } catch (TransactionRuntimeException e) {
            assertTrue(e.getCause() instanceof RollbackException);
            caught = true;
        }
        assertTrue(caught == true);
    }
}
