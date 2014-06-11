/*
 *   Copyright 2011-2014 Glencoe Software, Inc. All rights reserved.
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

import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.security.ACLVoter;
import ome.security.ChmodStrategy;
import ome.security.SecuritySystem;
import ome.security.auth.PasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.services.chgrp.ChgrpStepFactory;
import ome.services.chown.ChownStepFactory;
import ome.services.delete.Deletion;
import ome.services.graphs.GraphException;
import ome.services.util.MailUtil;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import omero.cmd.admin.ResetPasswordRequestI;
import omero.cmd.basic.DoAllI;
import omero.cmd.basic.ListRequestsI;
import omero.cmd.basic.TimingI;
import omero.cmd.fs.ManageImageBinariesI;
import omero.cmd.fs.OriginalMetadataRequestI;
import omero.cmd.graphs.ChgrpI;
import omero.cmd.graphs.ChgrpNewI;
import omero.cmd.graphs.ChmodI;
import omero.cmd.graphs.ChownI;
import omero.cmd.graphs.DeleteI;
import omero.cmd.graphs.DeleteNewI;
import omero.cmd.graphs.DiskUsageI;
import omero.cmd.graphs.GraphSpecListI;
import omero.cmd.mail.SendEmailRequestI;

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

    private final ACLVoter voter;

    private final Roles roles;

    private final PixelsService pixelsService;

    private final ThumbnailService thumbnailService;

    private final MailUtil mailUtil;

    private final PasswordUtil passwordUtil;
    
    private final SecuritySystem sec;
    
    private final PasswordProvider passwordProvider;
    
    private/* final */OmeroContext ctx;

    public RequestObjectFactoryRegistry(ExtendedMetadata em,
            ACLVoter voter,
            Roles roles,
            PixelsService pixelsService,
            ThumbnailService thumbnailService,
            MailUtil mailUtil,
            PasswordUtil passwordUtil,
            SecuritySystem sec,
            PasswordProvider passwordProvider) {

        this.em = em;
        this.voter = voter;
        this.roles = roles;
        this.pixelsService = pixelsService;
        this.thumbnailService = thumbnailService;
        this.mailUtil = mailUtil;
        this.passwordUtil = passwordUtil;
        this.sec = sec;
        this.passwordProvider = passwordProvider;
    }

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.ctx = (OmeroContext) ctx;
    }

    public Map<String, ObjectFactory> createFactories(final Ice.Communicator ic) {
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
                        return new ChgrpI(ic, factory, specs);
                    }

                });
        factories.put(ChgrpNewI.ice_staticId(),
                new ObjectFactory(ChgrpNewI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        try {
                            return new ChgrpNewI();
                        } catch (GraphException e) {
                            throw new RuntimeException("failed to construct chgrp operation", e);
                        }
                    }

                });
        factories.put(ChmodI.ice_staticId(),
                new ObjectFactory(ChmodI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new ChmodI(ic,
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
                        return new ChownI(ic, factory, specs);
                    }

                });
        factories.put(DeleteI.ice_staticId(),
                new ObjectFactory(DeleteI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        Deletion d = ctx.getBean(Deletion.class.getName(), Deletion.class);
                        return new DeleteI(ic, d);
                    }
                });
        factories.put(DeleteNewI.ice_staticId(),
                new ObjectFactory(DeleteNewI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        try {
                            return new DeleteNewI();
                        } catch (GraphException e) {
                            throw new RuntimeException("failed to construct delete operation", e);
                        }
                    }

                });
        factories.put(OriginalMetadataRequestI.ice_staticId(),
                new ObjectFactory(OriginalMetadataRequestI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new OriginalMetadataRequestI(pixelsService);
                    }
                });
        factories.put(ManageImageBinariesI.ice_staticId(),
                new ObjectFactory(ManageImageBinariesI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new ManageImageBinariesI(pixelsService, voter);
                    }
                });
        factories.put(DiskUsageI.ice_staticId(),
                new ObjectFactory(DiskUsageI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new DiskUsageI(pixelsService, thumbnailService);
                    }
                });
        factories.put(SendEmailRequestI.ice_staticId(),
                new ObjectFactory(SendEmailRequestI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                    	return new SendEmailRequestI(mailUtil);
                    }
                });
        factories.put(ResetPasswordRequestI.ice_staticId(),
                new ObjectFactory(ResetPasswordRequestI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                    	return new ResetPasswordRequestI(mailUtil, passwordUtil, sec, passwordProvider);
                    }
                });
        return factories;
    }

}
