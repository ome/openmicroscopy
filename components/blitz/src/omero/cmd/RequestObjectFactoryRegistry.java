/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.cmd;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ome.io.nio.AbstractFileSystemService;
import ome.security.ChmodStrategy;
import ome.services.chgrp.ChgrpStepFactory;
import ome.services.chown.ChownStepFactory;
import ome.services.delete.DeleteStepFactory;
import ome.services.delete.Deletion;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;

import omero.cmd.basic.DoAllI;
import omero.cmd.basic.ListRequestsI;
import omero.cmd.basic.TimingI;
import omero.cmd.graphs.ChgrpI;
import omero.cmd.graphs.ChmodI;
import omero.cmd.graphs.ChownI;
import omero.cmd.graphs.DeleteI;
import omero.cmd.graphs.GraphSpecListI;

/**
 * SPI type picked up from the Spring configuration and given a chance to
 * register all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicator}.
 *
 * @see ticket:6340
 */
public class RequestObjectFactoryRegistry extends
        omero.util.ObjectFactoryRegistry implements ApplicationContextAware {

    private final ExtendedMetadata em;

    private final Roles roles;

    private/* final */OmeroContext ctx;

    public RequestObjectFactoryRegistry(ExtendedMetadata em, Roles roles) {
        this.em = em;
        this.roles = roles;
    }

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    public Map<String, ObjectFactory> createFactories() {
        Map<String, ObjectFactory> factories = new HashMap<String, ObjectFactory>();
        factories.put(TimingI.ice_staticId(), new ObjectFactory(
                TimingI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new TimingI();
            }

        });
        factories.put(DoAllI.ice_staticId(), new ObjectFactory(
                DoAllI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new DoAllI(ctx);
            }

        });
        factories.put(ListRequestsI.ice_staticId(), new ObjectFactory(
                ListRequestsI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new ListRequestsI(ctx);
            }

        });
        factories.put(GraphSpecListI.ice_staticId(), new ObjectFactory(
                GraphSpecListI.ice_staticId()) {
            @Override
            public Ice.Object create(String name) {
                return new GraphSpecListI(ctx);
            }

        });
        factories.put(ChgrpI.ice_staticId(),
                new ObjectFactory(ChgrpI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        ClassPathXmlApplicationContext specs = new ClassPathXmlApplicationContext(
                                new String[] { "classpath:ome/services/spec.xml" },
                                ctx);
                        ChgrpStepFactory factory = new ChgrpStepFactory(ctx, em, roles);
                        return new ChgrpI(factory, specs);
                    }

                });
        factories.put(ChmodI.ice_staticId(),
                new ObjectFactory(ChmodI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new ChmodI(
                                ctx.getBean("chmodStrategy", ChmodStrategy.class));
                    }

                });
        factories.put(ChownI.ice_staticId(),
                new ObjectFactory(ChownI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        ClassPathXmlApplicationContext specs = new ClassPathXmlApplicationContext(
                                new String[] { "classpath:ome/services/spec.xml" },
                                ctx);
                        ChownStepFactory factory = new ChownStepFactory(ctx, em, roles);
                        return new ChownI(factory, specs);
                    }

                });
        factories.put(DeleteI.ice_staticId(),
                new ObjectFactory(DeleteI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        ClassPathXmlApplicationContext specs = new ClassPathXmlApplicationContext(
                            new String[]{"classpath:ome/services/spec.xml"}, ctx);
                        DeleteStepFactory dsf = new DeleteStepFactory(ctx);
                        AbstractFileSystemService afs =
                            ctx.getBean("/OMERO/Files", AbstractFileSystemService.class);
                        Deletion d = new Deletion(specs, dsf, afs);
                        return new DeleteI(d);
                    }
                });
        return factories;
    }

}
