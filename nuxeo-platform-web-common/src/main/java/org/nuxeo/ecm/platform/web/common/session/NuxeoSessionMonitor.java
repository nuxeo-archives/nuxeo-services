/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     guillaume
 */
package org.nuxeo.ecm.platform.web.common.session;

import javax.management.MXBean;

/**
 *
 *
 * @since 5.9.2
 */
@MXBean
public interface NuxeoSessionMonitor {

    long getTotalSessionSizeAtLogout();

    void resetTotalSessionSizeAtLogout();

    long getGlobalRequestCounter();

    long getAllSessionStateSize();

    long getAllSessionSize();

}