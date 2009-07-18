/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.Arrays;
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
import omero.api.AMD_IRoi_findByAnyIntersection;
import omero.api.AMD_IRoi_findByImage;
import omero.api.AMD_IRoi_findByIntersection;
import omero.api.AMD_IRoi_findByPlane;
import omero.api.AMD_IRoi_findByRoi;
import omero.api.AMD_IRoi_getPoints;
import omero.api.AMD_IRoi_getRoiStats;
import omero.api.AMD_IRoi_getShapeStats;
import omero.api.AMD_IRoi_getShapeStatsList;
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
                "findByIntersection", imageId, shape) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                List<Long> roiIds = geomTool.findIntersectingRois(imageId,
                        shape);
                if (roiIds == null || roiIds.size() == 0) {
                    return null;
                } else {
                    Query q = session
                            .createQuery("select distinct r from Roi r "
                                    + "join fetch r.shapes where r.id in (:ids) "
                                    + "order by r.id");
                    q.setParameterList("ids", roiIds);
                    return q.list();
                }
            }
        }));
    }

    public void findByAnyIntersection_async(
            AMD_IRoi_findByAnyIntersection __cb, final long imageId,
            final List<Shape> shapes, RoiOptions opts, Current __current)
            throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByAnyIntersection", imageId, shapes) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                if (shapes == null || shapes.size() == 0) {
                    return null; // EARLY EXIT
                }

                List<Long> roiIds = geomTool.findIntersectingRois(imageId,
                        shapes.toArray(new Shape[shapes.size()]));
                if (roiIds == null || roiIds.size() == 0) {
                    return null;
                } else {
                    Query q = session
                            .createQuery("select distinct r from Roi r "
                                    + "join fetch r.shapes where r.id in (:ids) "
                                    + "order by r.id");
                    q.setParameterList("ids", roiIds);
                    return q.list();
                }
            }
        }));

    }

    public void findByImage_async(AMD_IRoi_findByImage __cb,
            final long imageId, RoiOptions opts, Current __current)
            throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByImage", imageId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Query q = session
                        .createQuery("select distinct r from Roi r join r.image i "
                                + "join fetch r.shapes where i.id = :id "
                                + "order by r.id");
                q.setParameter("id", imageId);
                return q.list();

            }
        }));

    }

    public void findByRoi_async(AMD_IRoi_findByRoi __cb, final long roiId,
            RoiOptions opts, Current __current) throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByRoi", roiId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Query q = session.createQuery("select distinct r from Roi r "
                        + "join fetch r.shapes where r.id = :id "
                        + "order by r.id");
                q.setParameter("id", roiId);
                return q.list();

            }
        }));
    }

    public void findByPlane_async(AMD_IRoi_findByPlane __cb,
            final long imageId, final int z, final int t, RoiOptions opts,
            Current __current) throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByPlane", imageId, z, t) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Query q = session
                        .createQuery("select distinct r from Roi r "
                                + "join fetch r.shapes s where r.id = :id "
                                + "and ( s.theZ is null or s.theZ = :z ) "
                                + "and ( s.theT is null or s.theT = :t ) "
                                + "order by r.id");
                q.setParameter("id", imageId);
                q.setParameter("z", z);
                q.setParameter("t", t);
                return q.list();

            }
        }));
    }

    public void getPoints_async(AMD_IRoi_getPoints __cb, final long shapeId,
            Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getPoints", shapeId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return geomTool.getPoints(shapeId, session);
            }
        }));
    }

    public void getShapeStats_async(AMD_IRoi_getShapeStats __cb,
            final long shapeId, Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getShapeStats", shapeId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return geomTool.getStats(Arrays.asList(shapeId)).perShape[0];
            }
        }));

    }

    public void getShapeStatsList_async(AMD_IRoi_getShapeStatsList __cb,
            final List<Long> shapeIdList, Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getShapeStatsList", shapeIdList) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return Arrays.asList(geomTool.getStats(shapeIdList).perShape);
            }
        }));
    }

    public void getRoiStats_async(AMD_IRoi_getRoiStats __cb, final long roiId,
            Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getRoiStats", roiId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                List<Long> shapesInRoi = geomTool.getShapeIds(roiId);
                return geomTool.getStats(shapesInRoi);
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
                    } else {
                        byT.put(-1, shape);
                    }
                    if (shape.getTheZ() != null) {
                        byZ.put(shape.getTheZ().getValue(), shape);
                    } else {
                        byZ.put(-1, shape);
                    }
                    if (shape.getG() != null) {
                        byG.put(shape.getG().getValue(), shape);
                    } else {
                        byG.put("", shape);
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
