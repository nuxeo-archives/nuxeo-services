/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *
 * Contributors:
 *     matic
 */
package org.nuxeo.ecm.platform.management.core.probes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.management.api.ProbeInfo;
import org.nuxeo.ecm.core.management.api.ProbeManager;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.platform.management.statuses.ProbeScheduler;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.management.ResourcePublisher;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author Stephane Lacoin (Nuxeo EP Software Engineer)
 */
@RunWith(FeaturesRunner.class)
@Features({TransactionalFeature.class, CoreFeature.class})
@Deploy({ "org.nuxeo.runtime.management", "org.nuxeo.ecm.core.management", "org.nuxeo.ecm.platform.management"})
public class TestProbes {

    @Test
    public void testScheduling() throws MalformedObjectNameException {
        ProbeScheduler scheduler = Framework.getLocalService(ProbeScheduler.class);
        assertFalse(scheduler.isEnabled());

        scheduler.enable();
        assertTrue(scheduler.isEnabled());

        scheduler.disable();
        assertFalse(scheduler.isEnabled());

        ResourcePublisher publisher = Framework.getLocalService(ResourcePublisher.class);
        assertTrue(publisher.getResourcesName().contains(new ObjectName("org.nuxeo:name=probeScheduler,type=service")));
    }

    @Test
    public void testPopulateRepository() throws Exception {
       ProbeInfo info = getProbeRunner().getProbeInfo("populateRepository");
       assertNotNull(info);
       info = getProbeRunner().runProbe(info);
       assertFalse(info.isInError());
       String result = info.getStatus().getAsString();
       System.out.print("populateRepository Probe result : " + result);
    }

    @Test
    public void testQueryRepository() throws Exception {
        ProbeInfo info = getProbeRunner().getProbeInfo("queryRepository");
        assertNotNull(info);
        info = getProbeRunner().runProbe(info);
        assertFalse(info.isInError());
        System.out.print(info.getStatus().getAsString());
    }

   ProbeManager getProbeRunner() throws Exception {
       return Framework.getService(ProbeManager.class);
   }

}
