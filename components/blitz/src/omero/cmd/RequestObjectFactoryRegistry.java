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

import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.security.ACLVoter;
import ome.security.SecuritySystem;
import ome.security.auth.PasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.services.mail.MailUtil;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import omero.cmd.admin.ResetPasswordRequestI;
import omero.cmd.basic.DoAllI;
import omero.cmd.basic.ListRequestsI;
import omero.cmd.basic.TimingI;
import omero.cmd.fs.FindPyramidsI;
import omero.cmd.fs.ManageImageBinariesI;
import omero.cmd.fs.OriginalMetadataRequestI;
import omero.cmd.fs.UsedFilesRequestI;
import omero.cmd.graphs.Chgrp2I;
import omero.cmd.graphs.ChildOptionI;
import omero.cmd.graphs.Chmod2I;
import omero.cmd.graphs.Chown2I;
import omero.cmd.graphs.Delete2I;
import omero.cmd.graphs.DiskUsage2I;
import omero.cmd.graphs.DiskUsageI;
import omero.cmd.graphs.DuplicateI;
import omero.cmd.graphs.FindChildrenI;
import omero.cmd.graphs.FindParentsI;
import omero.cmd.graphs.GraphRequestFactory;
import omero.cmd.graphs.LegalGraphTargetsI;
import omero.cmd.graphs.SkipHeadI;
import omero.cmd.mail.SendEmailRequestI;

/**
 * SPI type picked up from the Spring configuration and given a chance to
 * register all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicator}.
 *
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/6340">Trac ticket #6340</a>
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
    
    private final GraphRequestFactory graphRequestFactory;

    private/* final */OmeroContext ctx;

    public RequestObjectFactoryRegistry(ExtendedMetadata em,
            ACLVoter voter,
            Roles roles,
            PixelsService pixelsService,
            ThumbnailService thumbnailService,
            MailUtil mailUtil,
            PasswordUtil passwordUtil,
            SecuritySystem sec,
            PasswordProvider passwordProvider,
            GraphRequestFactory graphRequestFactory) {

        this.em = em;
        this.voter = voter;
        this.roles = roles;
        this.pixelsService = pixelsService;
        this.thumbnailService = thumbnailService;
        this.mailUtil = mailUtil;
        this.passwordUtil = passwordUtil;
        this.sec = sec;
        this.passwordProvider = passwordProvider;
        this.graphRequestFactory = graphRequestFactory;
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
        factories.put(Chgrp2I.ice_staticId(),
                new ObjectFactory(Chgrp2I.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(Chgrp2I.class);
                    }

                });
        factories.put(Chmod2I.ice_staticId(),
                new ObjectFactory(Chmod2I.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(Chmod2I.class);
                    }

                });
        factories.put(Chown2I.ice_staticId(),
                new ObjectFactory(Chown2I.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(Chown2I.class);
                    }

                });
        factories.put(Delete2I.ice_staticId(),
                new ObjectFactory(Delete2I.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(Delete2I.class);
                    }

                });
        factories.put(SkipHeadI.ice_staticId(),
                new ObjectFactory(SkipHeadI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(SkipHeadI.class);
                    }

                });
        factories.put(LegalGraphTargetsI.ice_staticId(),
                new ObjectFactory(LegalGraphTargetsI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new LegalGraphTargetsI(graphRequestFactory);
                    }
                });
        factories.put(OriginalMetadataRequestI.ice_staticId(),
                new ObjectFactory(OriginalMetadataRequestI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new OriginalMetadataRequestI(pixelsService);
                    }
                });
        factories.put(UsedFilesRequestI.ice_staticId(),
                new ObjectFactory(UsedFilesRequestI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new UsedFilesRequestI(pixelsService);
                    }
                });
        factories.put(ManageImageBinariesI.ice_staticId(),
                new ObjectFactory(ManageImageBinariesI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new ManageImageBinariesI(pixelsService, voter);
                    }
                });
        factories.put(FindPyramidsI.ice_staticId(),
                new ObjectFactory(FindPyramidsI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new FindPyramidsI(pixelsService);
                    }
                });
        factories.put(DiskUsageI.ice_staticId(),
                new ObjectFactory(DiskUsageI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return new DiskUsageI(pixelsService, thumbnailService, graphRequestFactory.getGraphPathBean());
                    }
                });
        factories.put(DiskUsage2I.ice_staticId(),
                new ObjectFactory(DiskUsage2I.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        final DiskUsage2I request = graphRequestFactory.getRequest(DiskUsage2I.class);
                        request.setPixelsService(pixelsService);
                        request.setThumbnailService(thumbnailService);
                        return request;
                    }
                });
        factories.put(DuplicateI.ice_staticId(),
                new ObjectFactory(DuplicateI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(DuplicateI.class);
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
        factories.put(FindParentsI.ice_staticId(),
                new ObjectFactory(FindParentsI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(FindParentsI.class);
                    }
                });
        factories.put(FindChildrenI.ice_staticId(),
                new ObjectFactory(FindChildrenI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.getRequest(FindChildrenI.class);
                    }
                });
        /* request parameters */
        factories.put(ChildOptionI.ice_staticId(),
                new ObjectFactory(ChildOptionI.ice_staticId()) {
                    @Override
                    public Ice.Object create(String name) {
                        return graphRequestFactory.createChildOption();
                    }
                });
        return factories;
    }

}
