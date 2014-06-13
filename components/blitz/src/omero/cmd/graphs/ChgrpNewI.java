/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package omero.cmd.graphs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.system.Login;
import omero.cmd.ChgrpNew;
import omero.cmd.ChgrpNewResponse;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.util.IceMapper;

/**
 * An experimental Chgrp for exercising the {@link ome.services.graphs.GraphPathBean} from clients.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public class ChgrpNewI extends ChgrpNew implements IRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChgrpNewI.class);
    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");
    private static final IceMapper ICE_MAPPER = new IceMapper();

    private final GraphPathBean graphPathBean;
    private final GraphPolicy graphPolicy;
 
    private Helper helper;
    private GraphTraversal graphTraversal;

    /**
     * Construct a new <q>chgrp</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphPathBean the graph path bean to use
     * @param graphPolicy the graph policy to apply for chgrp
     */
    public ChgrpNewI(GraphPathBean graphPathBean, GraphPolicy graphPolicy) {
        this.graphPathBean = graphPathBean;
        this.graphPolicy = graphPolicy;
    }

    @Override
    public ImmutableMap<String, String> getCallContext() {
       return ALL_GROUPS_CONTEXT;
    }

    @Override
    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(3);
        graphTraversal = new GraphTraversal(graphPathBean, graphPolicy, new InternalProcessor());
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        try {
            switch (step) {
            case 0:
                return graphTraversal.planOperation(helper.getSession(), ICE_MAPPER.reverse(targetObjects));
            case 1:
                graphTraversal.unlinkTargets();
                return null;
            case 2:
                graphTraversal.processTargets();
                return null;
            default:
                throw helper.cancel(new ERR(), new IllegalArgumentException(), "model object graph operation has no step " + step);
            }
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "model object graph operation failed");
        }
    }

    @Override
    public void finish() {
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            final Entry<Collection<IObject>, Collection<IObject>> result = (Entry<Collection<IObject>, Collection<IObject>>) object;
            final ImmutableList<omero.model.IObject> movedObjects = ImmutableList.copyOf(ICE_MAPPER.map(result.getKey()));
            final ImmutableList<omero.model.IObject> deletedObjects = ImmutableList.copyOf(ICE_MAPPER.map(result.getValue()));
            final ChgrpNewResponse response = new ChgrpNewResponse(movedObjects, deletedObjects);
            helper.setResponseIfNull(response);
            LOGGER.info("in chgrp to " + groupId + " of " + targetObjects.size() +
                    ", moved " + response.includedObjects.size() + " and deleted " + response.deletedObjects.size() + " in total");
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    private final class InternalProcessor implements GraphTraversal.Processor {
        private final Session session;

        public InternalProcessor() {
            this.session = helper.getSession();
        }

        @Override
        public void nullProperties(String className, String propertyName,
                Collection<Long> ids) {
            final String update = "UPDATE " + className + " SET " + propertyName + " = NULL WHERE id IN (:ids)";
            session.createQuery(update).setParameterList("ids", ids).executeUpdate();
        }

        @Override
        public void filterProperties(String className, String propertyName,
                Collection<Entry<Long, Collection<Entry<String, Long>>>> ids) {
            final Map<Long, Collection<Entry<String, Long>>> idMap = new HashMap<Long, Collection<Entry<String, Long>>>(2 * ids.size());
            for (final Entry<Long, Collection<Entry<String, Long>>> idEntry : ids) {
                idMap.put(idEntry.getKey(), idEntry.getValue());
            }
            final String query = "FROM " + className + " root LEFT JOIN FETCH root." + propertyName + " WHERE root.id IN (:ids)";
            final List<IObject> retrieved = session.createQuery(query).setParameterList("ids", idMap.keySet()).list();
            for (final IObject proxy : retrieved) {
                final Set<Entry<String, Long>> toRemove = new HashSet<Entry<String, Long>>(idMap.get(proxy.getId()));
                final Collection<IObject> items;
                try {
                    items = (Collection<IObject>) PropertyUtils.getNestedProperty(proxy, propertyName);
                } catch (/* TODO Java SE 7 ReflectiveOperation*/Exception e) {
                    throw new RuntimeException(Hibernate.getClass(proxy).getName() + "[" + proxy.getId() +
                            "] has no accessible object collection property " + propertyName, e);
                }
                final Iterator<IObject> itemIterator = items.iterator();
                while (itemIterator.hasNext()) {
                    final IObject item = itemIterator.next();
                    if (toRemove.contains(Maps.immutableEntry(Hibernate.getClass(item), item.getId()))) {
                        itemIterator.remove();
                    }
                }
            }
        }

        @Override
        public void deleteInstances(String className, Collection<Long> ids) {
            final String update = "DELETE FROM " + className + " WHERE id IN (:ids)";
            session.createQuery(update).setParameterList("ids", ids).executeUpdate();
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) {
            final ExperimenterGroup group = (ExperimenterGroup) session.load(ExperimenterGroup.class, groupId);
            final String update = "UPDATE " + className + " SET details.group = :group WHERE id IN (:ids)";
            session.createQuery(update).setParameter("group", group).setParameterList("ids", ids).executeUpdate();
        }
    }
}
