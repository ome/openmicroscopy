/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphSpec;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import omero.cmd.GraphModify;
import omero.cmd.GraphSpecList;
import omero.cmd.GraphSpecListRsp;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public class GraphSpecListI extends GraphSpecList implements IRequest {

    private static final long serialVersionUID = -363984593874598374L;

    private final AtomicReference<Response> rsp = new AtomicReference<Response>();

    private final OmeroContext ctx;

    private Helper helper;

    public GraphSpecListI(OmeroContext ctx) {
        this.ctx = ctx;
    }

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(1);
    }

    public Object step(int step) {
        helper.assertStep(0, step);

        final GraphSpecListRsp rsp = new GraphSpecListRsp();
        final ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[]{"classpath:ome/services/spec.xml"}, this.ctx);

        final String[] keys = ctx.getBeanNamesForType(GraphSpec.class);
        final List<GraphModify> cmds = new ArrayList<GraphModify>();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            GraphSpec spec = ctx.getBean(key, GraphSpec.class);
            Map<String, String> options = new HashMap<String, String>();
            for (GraphEntry entry : spec.entries()) {
                options.put(entry.getName(), entry.getOpString());
            }
            cmds.add(new GraphModify(key, -1l, options));
        }

        rsp.list = cmds;
        return rsp;
    }

    public void buildResponse(int i, Object object) {
        helper.assertStep(0, i);
        helper.setResponse((Response) object);
    }

    public Response getResponse() {
        return rsp.get();
    }
}
