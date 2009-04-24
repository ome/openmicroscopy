/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.roi.GeomTool;
import ome.services.throttling.Adapter;
import ome.services.util.Executor.SimpleWork;
import ome.system.ServiceFactory;
import omero.ServerError;
import omero.api.AMD_IRoi_findByIntersection;
import omero.api.AMD_IRoi_getPoints;
import omero.api.AMD_IRoi_getStats;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.api.ShapePoints;
import omero.api._IRoiOperations;
import omero.model.Roi;
import omero.model.Shape;
import omero.util.IceMapper;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * implementation of the IRoi service interface.
 * 
 * @since Beta4.1
 */
public class RoiI extends AbstractAmdServant implements _IRoiOperations,
        ServiceFactoryAware, BlitzOnly {

    protected ServiceFactoryI factory;

    protected final GeomTool geomTool;

    public RoiI(BlitzExecutor be, GeomTool geomTool) {
        super(null, be);
        this.geomTool = geomTool;
    }

    public void setServiceFactory(ServiceFactoryI sf) {
        this.factory = sf;
    }

    // ~ Service methods
    // =========================================================================

    public void findByIntersection_async(AMD_IRoi_findByIntersection __cb,
            final long imageId, final Shape shape, final RoiOptions opts,
            Current __current) throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByIntersection") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                List<Long> roiIds = geomTool.findIntersectingRois(imageId,
                        shape);
                if (roiIds == null || roiIds.size() == 0) {
                    return null;
                } else {
                    Query q = session
                            .createQuery("select distinct r from Roi r "
                                    + "join fetch r.shapes where r.id in (:ids)");
                    q.setParameterList("ids", roiIds);
                    return q.list();
                }
            }
        }));
    }

    public void getPoints_async(AMD_IRoi_getPoints __cb, final long shapeId,
            Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getPoints") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                Shape shape = geomTool.shapeById(shapeId, session);
                return geomTool.getPoints(shape);
            }
        }));
    }


    public void getStats_async(AMD_IRoi_getStats __cb, final long shapeId,
            Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getPoints") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                Shape shape = geomTool.shapeById(shapeId, session);
                ShapePoints points = geomTool.getPoints(shape);
                return geomTool.getStats(shape, points);
            }
        }));
        
    }

    // Helpers
    // =========================================================================

    private static class RoiResultMapper extends IceMapper {
        public RoiResultMapper(RoiOptions opts) {
            super(new RoiResultReturnMapper(opts));
        }
    }

    private static class RoiResultReturnMapper implements
            IceMapper.ReturnMapping {

        private final RoiOptions opts;

        public RoiResultReturnMapper(RoiOptions opts) {
            this.opts = opts;
        }

        @SuppressWarnings("unchecked")
        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            RoiResult result = new RoiResult();
            result.opts = opts;

            if (value == null) {
                result.rois = Collections.emptyList();
                result.byZ = Collections.emptyMap();
                result.byT = Collections.emptyMap();
                result.byG = Collections.emptyMap();
                result.groups = Collections.emptyMap();
                return result; // EARLY EXIT
            }

            List<Roi> rois = (List<Roi>) IceMapper.FILTERABLE_COLLECTION
                    .mapReturnValue(mapper, value);
            result.rois = rois;
            MultiMap byZ = new MultiValueMap();
            MultiMap byT = new MultiValueMap();
            MultiMap byG = new MultiValueMap();
            for (Roi roi : rois) {
                omero.model.RoiI roii = (omero.model.RoiI) roi;
                Iterator<Shape> it = roii.iterateShapes();
                while (it.hasNext()) {
                    Shape shape = it.next();
                    if (shape.getTheT() != null) {
                        byT.put(shape.getTheT().getValue(), shape);
                    }
                    if (shape.getTheZ() != null) {
                        byZ.put(shape.getTheZ().getValue(), shape);
                    }
                    if (shape.getG() != null) {
                        byG.put(shape.getG().getValue(), shape);
                    }
                }
                result.byG = byG;
                result.byZ = byZ;
                result.byT = byT;
            }
            return result;
        }
    }

}
