/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ome.system.OmeroContext;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.ListRequests;
import omero.cmd.ListRequestsRsp;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;
import omero.util.ObjectFactoryRegistry;
import omero.util.ObjectFactoryRegistry.ObjectFactory;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ListRequestsI extends ListRequests implements IRequest {

    private static final long serialVersionUID = -3653081139095111039L;

    private final OmeroContext ctx;

    private Helper helper;

    public ListRequestsI(OmeroContext ctx) {
        this.ctx = ctx;
    }

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        helper.setSteps(1);
        this.helper = helper;
    }

    public Object step(int i) {
        helper.assertStep(0, i);

        final Ice.Communicator ic = ctx.getBean(Ice.Communicator.class);
        final List<Request> requestTypes = new ArrayList<Request>();
        final Map<String, ObjectFactoryRegistry> registries = ctx
                .getBeansOfType(ObjectFactoryRegistry.class);
        final ListRequestsRsp rsp = new ListRequestsRsp();
        for (ObjectFactoryRegistry registry : registries.values()) {
            Map<String, ObjectFactory> factories = registry.createFactories(ic);
            for (Map.Entry<String, ObjectFactory> entry : factories.entrySet()) {
                String key = entry.getKey();
                ObjectFactory factory = entry.getValue();
                Object obj = factory.create(key);
                if (obj instanceof Request) {
                    requestTypes.add((Request) obj);
                }
            }
        }

        rsp.list = requestTypes;
        return rsp;
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertStep(0, step);
        helper.setResponseIfNull((Response) object);
    }

    public Response getResponse() {
        return helper.getResponse();
    }
}
