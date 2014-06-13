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
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;
import ome.system.Login;
import omero.cmd.Chgrp2;
import omero.cmd.Chgrp2Response;
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
public class Chgrp2I extends Chgrp2 implements IRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Chgrp2I.class);
    private static final ImmutableMap<String, String> ALL_GROUPS_CONTEXT = ImmutableMap.of(Login.OMERO_GROUP, "-1");

    private final IceMapper iceMapper = new IceMapper();
    private final GraphPathBean graphPathBean;
    private final GraphPolicy graphPolicy;
 
    private Helper helper;
    private GraphTraversal graphTraversal;

    /**
     * Construct a new <q>chgrp</q> request; called from {@link GraphRequestFactory#getRequest(Class)}.
     * @param graphPathBean the graph path bean to use
     * @param graphPolicy the graph policy to apply for chgrp
     */
    public Chgrp2I(GraphPathBean graphPathBean, GraphPolicy graphPolicy) {
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
                return graphTraversal.planOperation(helper.getSession(), iceMapper.reverse(targetObjects));
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
            final ImmutableList<omero.model.IObject> movedObjects = ImmutableList.copyOf(iceMapper.map(result.getKey()));
            final ImmutableList<omero.model.IObject> deletedObjects = ImmutableList.copyOf(iceMapper.map(result.getValue()));
            final Chgrp2Response response = new Chgrp2Response(movedObjects, deletedObjects);
            helper.setResponseIfNull(response);
            LOGGER.info("in chgrp to " + groupId + " of " + targetObjects.size() +
                    ", moved " + response.includedObjects.size() + " and deleted " + response.deletedObjects.size() + " in total");
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /**
     * A <q>chgrp</q> processor that updates model objects' group.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    private final class InternalProcessor extends BaseGraphTraversalProcessor {

        public InternalProcessor() {
            super(helper.getSession());
        }

        @Override
        public void processInstances(String className, Collection<Long> ids) {
            final ExperimenterGroup group = (ExperimenterGroup) session.load(ExperimenterGroup.class, groupId);
            final String update = "UPDATE " + className + " SET details.group = :group WHERE id IN (:ids)";
            session.createQuery(update).setParameter("group", group).setParameterList("ids", ids).executeUpdate();
        }
    }
}
