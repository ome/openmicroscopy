/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.cmd;

import java.util.HashMap;
import java.util.Map;

import ome.services.chgrp.ChgrpStepFactory;
import ome.system.OmeroContext;
import omero.cmd.basic.ListRequestsI;
import omero.cmd.graphs.ChgrpI;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SPI type picked up from the Spring configuration and given a chance to
 * register all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicator}.
 *
 * @see ticket:6340
 */
public class RequestObjectFactoryRegistry extends
        omero.util.ObjectFactoryRegistry implements ApplicationContextAware {

    private/* final */OmeroContext ctx;

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    public Map<String, ObjectFactory> createFactories() {
        Map<String, ObjectFactory> factories = new HashMap<String, ObjectFactory>();
        factories.put(ListRequestsI.ice_staticId(), new ObjectFactory(
                ListRequestsI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new ListRequestsI(ctx);
            }

        });
        factories.put(ChgrpI.ice_staticId(),
                new ObjectFactory(ChgrpI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        ClassPathXmlApplicationContext specs = new ClassPathXmlApplicationContext(
                                new String[] { "classpath:ome/services/spec.xml" },
                                ctx);
                        ChgrpStepFactory factory = new ChgrpStepFactory(ctx);
                        return new ChgrpI(factory, specs);
                    }

                });
        return factories;
    }

}
