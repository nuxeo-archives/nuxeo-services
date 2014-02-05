package org.nuxeo.ecm.directory;

import java.util.List;

import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.CoreScope;
import org.nuxeo.ecm.core.test.CoreScope.CleanupHook;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.SimpleFeature;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.name.Names;

@Features( CoreFeature.class)
@Deploy({ "org.nuxeo.ecm.directory", "org.nuxeo.ecm.directory.types.contrib" })
public class DirectoryFeature extends SimpleFeature {

    protected Binder binder;

    protected static class SessionProvider implements Provider<Session>,
            CleanupHook<Session> {

        protected final String name;

        protected final Directory dir;

        protected SessionProvider(Directory dir) throws DirectoryException {
            this.dir = dir;
            name = dir.getName();
        }

        @Override
        public Session get() {
            try {
                return dir.getSession();
            } catch (DirectoryException cause) {
                throw new AssertionError("Cannot open session on " + name, cause);
            }
        }

        @Override
        public void handleCleanup(Session session) {
            try {
                session.close();
            } catch (DirectoryException cause) {
                throw new AssertionError("Cannot cleanup session of " + name,
                        cause);
            }
        }

    }

    @Override
    public void configure(FeaturesRunner runner, Binder binder) {
        List<Directory> directories;
        try {
            directories = Framework.getLocalService(DirectoryService.class).getDirectories();
        } catch (DirectoryException cause) {
            throw new AssertionError("Cannot load directories from runtime",
                    cause);
        }
        AssertionError errors = new AssertionError("Cannot get directory names");
        for (Directory each : directories) {
            try {
                String eachName = each.getName();
                binder.bind(Directory.class).annotatedWith(
                        Names.named(eachName)).toInstance(each);
                binder.bind(Session.class).annotatedWith(Names.named(eachName)).toProvider(
                        new SessionProvider(each)).in(CoreScope.INSTANCE);
            } catch (DirectoryException cause) {
                errors.addSuppressed(cause);
            }
        }
        if (errors.getSuppressed().length > 0) {
            throw errors;
        }
        this.binder = binder;
    }

}
