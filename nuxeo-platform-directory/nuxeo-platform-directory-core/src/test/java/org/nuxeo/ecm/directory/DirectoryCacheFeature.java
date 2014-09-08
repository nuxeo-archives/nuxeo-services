package org.nuxeo.ecm.directory;

import org.nuxeo.ecm.core.cache.CacheFeature;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.SimpleFeature;

@Features(CacheFeature.class)
@LocalDeploy("org.nuxeo.ecm.core.cache:directory-cache-config.xml")
public class DirectoryCacheFeature extends SimpleFeature {

    public static final String ENTRY_CACHE_NAME = "directory-entry-cache";

    public static final String ENTRY_CACHE_WITHOUT_REFERENCES_NAME = "directory-entry-cache-without-references";

    @Override
    public void initialize(FeaturesRunner runner) throws Exception {
        runner.getFeature(CacheFeature.class).enable();
    }
    
    


}
