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
import java.util.concurrent.atomic.AtomicReference;

import ome.system.OmeroContext;
import ome.util.SqlAction;
import omero.cmd.IRequest;
import omero.cmd.ListRequests;
import omero.cmd.ListRequestsRsp;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;
import omero.util.ObjectFactoryRegistry;
import omero.util.ObjectFactoryRegistry.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class ListRequestsI extends ListRequests implements IRequest {

    private final Log log = LogFactory.getLog(ListRequestsI.class);

    private static final long serialVersionUID = -3653081139095111039L;

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private final OmeroContext ctx;

    public ListRequestsI(OmeroContext ctx) {
        this.ctx = ctx;
    }

    public void init(Status status, SqlAction sql, Session session, ome.system.ServiceFactory sf) {
        status.steps = 1;
    }

    public void step(int i) {
        return;
    }

    public void finish() {
        final List<Request> requestTypes = new ArrayList<Request>();
        final Map<String, ObjectFactoryRegistry> registries = ctx
                .getBeansOfType(ObjectFactoryRegistry.class);
        final ListRequestsRsp rsp = new ListRequestsRsp();
        for (ObjectFactoryRegistry registry : registries.values()) {
            Map<String, ObjectFactory> factories = registry.createFactories();
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
        this.rsp.set(rsp);
    }

    public Response getResponse() {
        return rsp.get();
    }
}
