/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.platform.forms.layout.core.registries;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.platform.forms.layout.descriptors.WidgetConverterDescriptor;
import org.nuxeo.runtime.model.SimpleContributionRegistry;

/**
 * @since 5.5
 */
public class WidgetConverterRegistry extends
        SimpleContributionRegistry<WidgetConverterDescriptor> {

    protected final String category;

    public WidgetConverterRegistry(String category) {
        super();
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getLayoutNames() {
        List<String> res = new ArrayList<String>();
        res.addAll(currentContribs.keySet());
        return res;
    }

    @Override
    public String getContributionId(WidgetConverterDescriptor contrib) {
        return contrib.getName();
    }

    public List<WidgetConverterDescriptor> getConverters() {
        List<WidgetConverterDescriptor> res = new ArrayList<WidgetConverterDescriptor>();
        for (WidgetConverterDescriptor item : currentContribs.values()) {
            if (item != null) {
                res.add(item);
            }
        }
        return res;
    }

    public WidgetConverterDescriptor getConverter(String id) {
        return getCurrentContribution(id);
    }

}
