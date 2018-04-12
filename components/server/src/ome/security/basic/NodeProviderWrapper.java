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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.BeanCreationException;

import ome.model.meta.Node;
import ome.security.NodeProvider;
import ome.services.util.ReadOnlyStatus;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

/**
 * A node provider that offers a unified view of multiple underlying node providers.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.6
 * @param <P> node providers that adjust according to read-only status
 */
public class NodeProviderWrapper<P extends NodeProvider & ReadOnlyStatus.IsAware> implements NodeProvider {

    private final List<P> read, write;

    /**
     * Construct a new node provider.
     * @param readOnly the read-only status
     * @param providers the node providers to wrap: the earlier providers are tried first and at least one provider must support
     * write operations according to {@link ome.services.util.ReadOnlyStatus.IsAware#isReadOnly(ReadOnlyStatus)}
     */
    public NodeProviderWrapper(ReadOnlyStatus readOnly, List<P> providers) {
        read = providers;
        write = new ArrayList<P>(read.size());
        for (final P provider : read) {
            if (!provider.isReadOnly(readOnly)) {
                write.add(provider);
            }
        }
        if (write.isEmpty()) {
            throw new BeanCreationException("must be given a read-write node provider");
        }
    }

    @Override
    public Set<String> getManagerList(boolean onlyActive) {
        final Set<String> rvs = new HashSet<>();
        for (final P provider : read) {
            final Set<String> rv = provider.getManagerList(onlyActive);
            if (rv != null) {
                rvs.addAll(rv);
            }
        }
        return rvs;
    }

    @Override
    public long getManagerIdByUuid(String managerUuid, SqlAction sql) {
        for (final P provider : read) {
            final long managerId = provider.getManagerIdByUuid(managerUuid, sql);
            if (managerId != 0) {
                return managerId;
            }
        }
        return 0;
    }

    @Override
    public Node getManagerByUuid(String managerUuid, ServiceFactory sf) {
        for (final P provider : read) {
            final Node manager = provider.getManagerByUuid(managerUuid, sf);
            if (manager != null) {
                return manager;
            }
        }
        return null;
    }

    @Override
    public int closeSessionsForManager(String managerUuid) {
        int rv = 0;
        for (final P provider : write) {
            rv += provider.closeSessionsForManager(managerUuid);
        }
        return rv;
    }

    @Override
    public void setManagerDown(String managerUuid) {
        for (final P provider : write) {
            provider.setManagerDown(managerUuid);
        }
    }

    @Override
    public Node addManager(String managerUuid, String proxyString) {
        return write.get(0).addManager(managerUuid, proxyString);
    }

    @Override
    public Principal principal() {
        return write.get(0).principal();
    }
}
