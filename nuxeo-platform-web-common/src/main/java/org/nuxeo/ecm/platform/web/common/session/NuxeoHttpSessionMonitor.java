/*
 *
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.web.common.session;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.management.counters.CounterHelper;

/**
 * Singleton used to keep track of all HttpSessions. This Singleton is
 * populated/updated either via the HttpSessionListener or via directedly via
 * the Authentication filter
 *
 * @author Tiry (tdelprat@nuxeo.com)
 * @since 5.4.2
 */
public class NuxeoHttpSessionMonitor implements NuxeoSessionMonitor {

    public static final String REQUEST_COUNTER = "org.nuxeo.web.requests";

    public static final String SESSION_COUNTER = "org.nuxeo.web.sessions";

    public static final long REQUEST_COUNTER_STEP = 5;

    protected static final Log log = LogFactory.getLog(NuxeoHttpSessionMonitor.class);

    protected static NuxeoHttpSessionMonitor instance = new NuxeoHttpSessionMonitor();

    static {
        try {
            instance.registerSelf();
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException | MalformedObjectNameException cause) {
            log.error(
                    "Cannot register http session monitor in platform mbean server",
                    cause);
        }
    }

    public static NuxeoHttpSessionMonitor instance() {
        return instance;
    }

    protected long globalRequestCounter;

    protected MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    protected Map<String, SessionInfoImpl> sessionTracker = new ConcurrentHashMap<String, SessionInfoImpl>();

    protected long totalSessionSizeAtLogout;

    public SessionInfoImpl addEntry(HttpSession session) {
        if (session == null || session.getId() == null) {
            return null;
        }
        SessionInfoImpl si = new SessionInfoImpl(session);
        sessionTracker.put(session.getId(), si);
        return si;
    }

    public SessionInfo associatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getId() != null) {
            SessionInfoImpl si = sessionTracker.get(session.getId());
            if (si == null) {
                si = addEntry(session);
            }
            if (request.getUserPrincipal() != null && si.getLoginName() == null) {
                si.setLoginName(request.getUserPrincipal().getName());
                CounterHelper.increaseCounter(SESSION_COUNTER);
            }
            si.setLastAccessUrl(request.getRequestURI());
            increaseRequestCounter();
            return si;
        }
        return null;
    }

    public SessionInfo associatedUser(HttpSession session, String userName) {
        if (session == null || session.getId() == null) {
            return null;
        }
        SessionInfoImpl si = sessionTracker.get(session.getId());
        if (si == null) {
            si = addEntry(session);
        }
        if (si.getLoginName() == null) {
            si.setLoginName(userName);
            CounterHelper.increaseCounter(SESSION_COUNTER);
        }
        return si;
    }

    @Override
    public long getAllSessionSize() {
        long result = 0;
        try {
            if (sessionTracker != null) {
                for (SessionInfoImpl si : sessionTracker.values()) {
                    result += si.getSessionSize();
                }
            }
        } catch (Exception e) {
            log.error("An error occured when computing getAllSessionSize", e);
        }
        return result;
    }

    @Override
    public long getAllSessionStateSize() {
        long result = 0;
        try {
            if (sessionTracker != null) {
                for (SessionInfoImpl si : sessionTracker.values()) {
                    result += si.getStateSize();
                }
            }
        } catch (Exception e) {
            log.error("An error occured when computing getAllSessionStateSize",
                    e);
        }
        return result;
    }

    @Override
    public long getGlobalRequestCounter() {
        return globalRequestCounter;
    }

    public List<SessionInfoImpl> getSortedSessions() {

        List<SessionInfoImpl> sortedSessions = new ArrayList<SessionInfoImpl>();
        for (SessionInfoImpl si : getTrackedSessions()) {
            if (si.getLoginName() != null) {
                sortedSessions.add(si);
            }
        }
        Collections.sort(sortedSessions);
        return sortedSessions;
    }

    public List<SessionInfoImpl> getSortedSessions(long maxInactivity) {
        List<SessionInfoImpl> sortedSessions = new ArrayList<SessionInfoImpl>();
        for (SessionInfoImpl si : getTrackedSessions()) {
            if (si.getLoginName() != null
                    && si.getInactivityInS() < maxInactivity) {
                sortedSessions.add(si);
            }
        }
        Collections.sort(sortedSessions);
        return sortedSessions;
    }

    @Override
    public long getTotalSessionSizeAtLogout() {
        return totalSessionSizeAtLogout;
    }

    public Collection<SessionInfoImpl> getTrackedSessions() {
        return sessionTracker.values();
    }

    protected void increaseRequestCounter() {
        globalRequestCounter += 1;
        if (globalRequestCounter == 1
                || globalRequestCounter % REQUEST_COUNTER_STEP == 0) {
            CounterHelper.setCounterValue(REQUEST_COUNTER, globalRequestCounter);
        }
    }

    protected void registerSelf() throws InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException,
            MalformedObjectNameException {
        mbs.registerMBean(this, new ObjectName(
                "org.nuxeo:type=http-session-monitor"));
    }

    public void removeEntry(String sid) {
        SessionInfo si = sessionTracker.remove(sid);
        if (si != null && si.getLoginName() != null) {
            totalSessionSizeAtLogout += si.getStateSize();
            double totalSessionSizeAtLogoutInMB = totalSessionSizeAtLogout;
            totalSessionSizeAtLogoutInMB = totalSessionSizeAtLogoutInMB / 8 / 1024 / 1024;
            log.warn("totalSessionSizeAtLogout = "
                    + totalSessionSizeAtLogoutInMB + " MB");
            CounterHelper.decreaseCounter(SESSION_COUNTER);
        }
    }

    @Override
    public void resetTotalSessionSizeAtLogout() {
        setTotalSessionSizeAtLogout(0);
    }

    public void setTotalSessionSizeAtLogout(
            long totalSessionStateSizeAtLogout) {
        this.totalSessionSizeAtLogout = totalSessionStateSizeAtLogout;
    }

    public SessionInfo updateEntry(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getId() != null) {
            SessionInfoImpl si = sessionTracker.get(session.getId());
            if (si != null) {
                si.updateLastAccessTime();
                si.setLastAccessUrl(request.getRequestURI());
                increaseRequestCounter();
                return si;
            } else {
                return addEntry(session);
            }
        }
        return null;
    }

}
