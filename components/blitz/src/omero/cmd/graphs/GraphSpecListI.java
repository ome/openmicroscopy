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

import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphSpec;
import ome.system.OmeroContext;
import omero.cmd.GraphModify;
import omero.cmd.GraphSpecList;
import omero.cmd.GraphSpecListRsp;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.HandleI.Cancel;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphSpecListI extends GraphSpecList implements IRequest {

    private static final long serialVersionUID = -363984593874598374L;

    private final OmeroContext ctx;

    private Helper helper;

    public GraphSpecListI(OmeroContext ctx) {
        this.ctx = ctx;
    }

    //
    // IRequest
    //

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

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int i, Object object) {
        helper.assertStep(0, i);
        helper.setResponseIfNull((Response) object);
    }

    public Response getResponse() {
        return helper.getResponse();
    }
}
