/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.basic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;

import ome.model.meta.Event;
import ome.security.EventProvider;
import ome.services.util.ReadOnlyStatus;

/**
 * An event provider that offers a unified view of multiple underlying event providers.
 * @author m.t.b.carroll@dundee.ac.uk
 * @param <P> event providers that adjust according to read-only status
 */
public class EventProviderWrapper<P extends EventProvider & ReadOnlyStatus.IsAware> implements EventProvider {

    private final List<P> write;

    /**
     * Construct a new event provider.
     * @param readOnly the read-only status
     * @param providers the event providers to wrap: the earlier providers are tried first and at least one provider must support
     * write operations according to {@link ome.services.util.ReadOnlyStatus.IsAware#isReadOnly(ReadOnlyStatus)}
     */
    public EventProviderWrapper(ReadOnlyStatus readOnly, List<P> providers) {
        write = new ArrayList<P>(providers.size());
        for (final P provider : providers) {
            if (!provider.isReadOnly(readOnly)) {
                write.add(provider);
            }
        }
        if (write.isEmpty()) {
            throw new BeanCreationException("must be given a read-write event provider");
        }
    }

    @Override
    public Event updateEvent(Event event) {
        return write.get(0).updateEvent(event);
    }
}
