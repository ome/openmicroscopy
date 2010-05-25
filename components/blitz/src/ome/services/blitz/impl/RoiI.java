/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.parameters.Filter;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.roi.GeomTool;
import ome.services.throttling.Adapter;
import ome.services.util.Executor.SimpleWork;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;
import ome.util.Filterable;
import omero.InternalException;
import omero.ServerError;
import omero.rtypes;
import omero.api.AMD_IRoi_findByAnyIntersection;
import omero.api.AMD_IRoi_findByImage;
import omero.api.AMD_IRoi_findByIntersection;
import omero.api.AMD_IRoi_findByPlane;
import omero.api.AMD_IRoi_findByRoi;
import omero.api.AMD_IRoi_getRoiMeasurements;
import omero.api.AMD_IRoi_getMeasuredRois;
import omero.api.AMD_IRoi_getMeasuredRoisMap;
import omero.api.AMD_IRoi_getPoints;
import omero.api.AMD_IRoi_getRoiStats;
import omero.api.AMD_IRoi_getShapeStats;
import omero.api.AMD_IRoi_getShapeStatsList;
import omero.api.AMD_IRoi_getTable;
import omero.api.AMD_IRoi_uploadMask;
import omero.api.IQueryPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.api._IRoiOperations;
import omero.constants.namespaces.NSMEASUREMENT;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.OriginalFileI;
import omero.model.Roi;
import omero.model.Shape;
import omero.util.IceMapper;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
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
    
    protected final SimpleJdbcOperations jdbc;

    public RoiI(BlitzExecutor be, GeomTool geomTool, SimpleJdbcOperations jdbc) {
    	super(null, be);
        this.geomTool = geomTool;
    	this.jdbc = jdbc;
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
                        opts, shape);
                if (roiIds == null || roiIds.size() == 0) {
                    return null;
                } else {
                    RoiQueryBuilder qb = new RoiQueryBuilder(roiIds);
                    return qb.query(session).list();
                }
            }
        }));
    }

    public void findByAnyIntersection_async(
            AMD_IRoi_findByAnyIntersection __cb, final long imageId,
            final List<Shape> shapes, final RoiOptions opts, Current __current)
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
                        opts, shapes.toArray(new Shape[shapes.size()]));
                if (roiIds == null || roiIds.size() == 0) {
                    return null;
                } else {
                    RoiQueryBuilder qb = new RoiQueryBuilder(roiIds);
                    return qb.query(session).list();
                }
            }
        }));

    }

    public void findByImage_async(AMD_IRoi_findByImage __cb,
            final long imageId, final RoiOptions opts, Current __current)
            throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByImage", imageId, opts) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
            	if(opts.namespace!=null)
            	{
                	Object[] parameters;	
                	String queryString;
                	parameters = new Object[] {new Long(imageId), 
            			new String(opts.namespace.getValue())};
                	queryString = "select id from roi where image = ? and ? = any (namespace)";
					return null;
            	}
            	else
            	{
        		    String queryString = "select distinct r from Roi r join r.image i "
                        + "join fetch r.shapes where i.id = :id";
        		    queryString = queryString + " order by r.id";
        		    Query q = session.createQuery(queryString);
        		    q.setParameter("id", imageId);
        		    return q.list();
            	}   	
            }
        }));

    }

    public void findByRoi_async(AMD_IRoi_findByRoi __cb, final long roiId,
            final RoiOptions opts, Current __current) throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "findByRoi", roiId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                RoiQueryBuilder qb = new RoiQueryBuilder(Arrays.asList(roiId));
                return qb.query(session).list();
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

                Query q = session.createQuery("select distinct r from Roi r "
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

    // Measurement results.
    // =========================================================================

    public void getRoiMeasurements_async(AMD_IRoi_getRoiMeasurements __cb,
            final long imageId, final RoiOptions opts, Current __current)
            throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_COLLECTION);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getRoiMeasurements", imageId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                QueryBuilder qb = new QueryBuilder();
                qb.select("distinct fa");
                qb.from("Image", "i");
                qb.append(", Roi roi ");
                qb.join("roi.annotationLinks", "rlinks", false, false);
                qb.join("rlinks.child", "rfa", false, false);
                qb.join("i.wellSamples", "ws", false, false);
                qb.join("ws.well", "well", false, false);
                qb.join("well.plate", "plate", false, false);
                qb.join("plate.annotationLinks", "links", false, false);
                qb.join("links.child", "fa", false, false);
                qb.where();
                qb.and("fa.ns = '" + NSMEASUREMENT.value + "'");
                qb.and("rfa.id = fa.id");
                qb.and("i.id = :id");
                qb.and("i.id = roi.image");
                qb.param("id", imageId);
                qb.filter("fa", filter(opts));
                return qb.query(session).list();
            }
        }));
    }

    protected List<ome.model.roi.Roi> loadMeasuredRois(Session session,
            long imageId, long annotationId) {
        Query q = session
                .createQuery("select distinct r from Roi r join r.image i "
                        + "join fetch r.shapes join i.wellSamples ws join ws.well well "
                        + "join well.plate plate join plate.annotationLinks links "
                        + "join links.child a where a.id = :aid and i.id = :iid "
                        + "order by r.id");
        q.setParameter("iid", imageId);
        q.setParameter("aid", annotationId);
        return q.list();
    }

    public void getMeasuredRoisMap_async(AMD_IRoi_getMeasuredRoisMap __cb,
            final long imageId, final List<Long> annotationIds,
            RoiOptions opts, Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(new RoiResultMapReturnMapper(
                opts));

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getMeasuredRoisMap", imageId, annotationIds) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                if (annotationIds == null) {
                    return null;
                }
                Map<Long, List<ome.model.roi.Roi>> rv = new HashMap<Long, List<ome.model.roi.Roi>>();
                for (Long annotationId : annotationIds) {
                    rv.put(annotationId, loadMeasuredRois(session, imageId,
                            annotationId));
                }
                return rv;
            }
        }));
    }

    public void getMeasuredRois_async(AMD_IRoi_getMeasuredRois __cb,
            final long imageId, final long annotationId, final RoiOptions opts,
            Current __current) throws ServerError {

        final IceMapper mapper = new RoiResultMapper(opts);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getMeasuredRois", imageId, annotationId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return loadMeasuredRois(session, imageId, annotationId);
            }
        }));
    }

    public void getTable_async(AMD_IRoi_getTable __cb, final long annotationId,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.UNMAPPED);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getTable", annotationId) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                QueryBuilder qb = new QueryBuilder();
                qb.select("f");
                qb.from("FileAnnotation", "fa");
                qb.join("fa.file", "f", false, false);
                qb.where();
                qb.and("fa.id = :id");
                qb.param("id", annotationId);
                OriginalFile file = (OriginalFile) qb.query(session)
                        .uniqueResult();

                if (file == null) {
                    throw new ome.conditions.ApiUsageException("No such file annotation: " + annotationId);
                }

                try {
                    return factory.sharedResources(__current).openTable(
                            new OriginalFileI(file.getId(), false));
                } catch (ServerError e) {
                    throw new RuntimeException(e);
                }

            }
        }));
    }

    class MaskClass
    {
    	Set<Point> points;
    	int colour;
    	Point min, max;
    	int width;
		int height;
	 
    	MaskClass(int value)
    	{
    		points = new HashSet<Point>();
    		colour = value;
    	}
    	
    	public Color getColour()
    	{
    		return new Color(colour);
    	}
    	
    	
    	public byte[] asBytes() throws IOException
    	{
    	   		
    		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    		DataOutputStream outputStream = new DataOutputStream(byteStream);
   			for(int x = min.x ; x < max.x + 1 ; x++)
			{
   				for(int y = min.y ; y < max.y + 1 ; y++)
   				{
     				if(points.contains(new Point(x,y)))
    					outputStream.writeInt(colour);
    				else
						outputStream.writeInt(0);
    			}
    		}
    		outputStream.close();
    		return byteStream.toByteArray();
    	}
    	
    	public void add(Point p)
    	{
    		if(points.size()==0)
    		{
    			min = new Point(p);
    			max = new Point(p);
    		}
    		else
    		{
    			min.x = Math.min(p.x, min.x);
    			min.y = Math.min(p.y, min.y);
    			max.x = Math.max(p.x, max.x);
    			max.y = Math.max(p.y, max.y);
    		}
   			width = max.x-min.x+1;
			height = max.y-min.y+1;
    		points.add(p);
    	}
    	
    	public ome.model.roi.Mask asMaskI(int z, int t) throws IOException
    	{
    		ome.model.roi.Mask mask = new ome.model.roi.Mask();
    		mask.setX((double)min.x);
    		mask.setY((double)min.y);
    		mask.setWidth((double)width);
    		mask.setHeight((double)height);
    		mask.setLocked(true);
    		mask.setTheT(t);
    		mask.setTheZ(z);
    		byte[] theseBytes;
    		theseBytes = this.asBytes();
    		mask.setBytes(theseBytes);
    		return mask;
    	}
    	
    }
    
    @SuppressWarnings("unchecked")
    private <T extends IObject> T safeReverse(Object o, IceMapper mapper) {
        try {
            return (T) mapper.reverse(o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to safely reverse: " + o);
        }
    }
    
	public void uploadMask_async(final AMD_IRoi_uploadMask __cb,
			final long imageId, final int z, final int t, final byte[] bytes,
			final Current __current) throws ServerError
	{

		final IceMapper mapper = new IceMapper(IceMapper.VOID);

		runnableCall(__current, new Adapter(__cb, __current, mapper, factory
				.getExecutor(), factory.principal, new SimpleWork(this,
				"uploadMask", bytes)
		{

			@Transactional(readOnly = false)
			public Object doWork(Session session, ServiceFactory sf)
			{
				IUpdate update = sf.getUpdateService();
				
				ome.model.core.Image image;
				ome.model.roi.Roi roi;
				ByteArrayInputStream s = new ByteArrayInputStream(bytes);
				IQuery query = sf.getQueryService();
				IObject o =  query.findByQuery("from Image as i left outer join " +
						"fetch i.pixels as p where i.id = "+imageId, null);
				
				try
				{
					image = (ome.model.core.Image) o;
					BufferedImage inputImage = ImageIO.read(s);
					Map<Integer, MaskClass> map = new HashMap<Integer, MaskClass>();
					MaskClass mask;
					int value;
					for (int x = 0; x < inputImage.getWidth(); x++)
						for (int y = 0; y < inputImage.getHeight(); y++)
						{
							value = inputImage.getRGB(x, y);
							if(value==Color.black.getRGB())
								continue;
							if (!map.containsKey(value))
							{
								mask = new MaskClass(value);
								map.put(value, mask);
							}
							else
								mask = map.get(value);
							mask.add(new Point(x, y));
						}
					Iterator<Integer> maskIterator = map.keySet().iterator();
					while (maskIterator.hasNext())
					{
						int colour = maskIterator.next();
						mask = map.get(colour);
						roi = new ome.model.roi.Roi();
						roi.setImage(image);
						ome.model.roi.Mask  toSaveMask = mask.asMaskI(z, t);
						roi.addShape(toSaveMask);
						ome.model.roi.Roi newROI  = update.saveAndReturnObject(roi);
					}
					return null;
				} catch (Exception e)
				{
					__cb.ice_exception(e);
				}
				return null;
			}
		}));
	}
    
    // Helpers
    // =========================================================================

    private Filter filter(RoiOptions opts) {
        Filter f = new Filter();
        if (opts != null) {
            if (opts.userId != null) {
                f.owner(opts.userId.getValue());
            }
            if (opts.groupId != null) {
                f.group(opts.groupId.getValue());
            }
            Integer offset = null;
            Integer limit = null;
            if (opts.offset != null) {
                offset = opts.offset.getValue();
            }
            if (opts.limit != null) {
                limit = opts.limit.getValue();
            }
            if (offset != null || limit != null) {
                f.page(offset, limit);
            }
        }
        return f;
    }

    private static class RoiQueryBuilder extends QueryBuilder {

        RoiQueryBuilder(List<Long> roiIds) {
            this.paramList("ids", roiIds);
            this.select("distinct r");
            this.from("Roi", "r");
            this.join("r.shapes", "s", false, true);
            this.where();
        }

        @Override
        public Query query(Session session) {
            this.and("r.id in (:ids)");
            this.append("order by r.id");
            return super.query(session);
        }

    }

    public static class RoiResultMapper extends IceMapper {
        public RoiResultMapper(RoiOptions opts) {
            super(new RoiResultReturnMapper(opts));
        }
    }

    public static class RoiResultReturnMapper implements
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

    public static class RoiResultMapReturnMapper implements
            IceMapper.ReturnMapping {

        private final RoiOptions opts;

        public RoiResultMapReturnMapper(RoiOptions opts) {
            this.opts = opts;
        }

        @SuppressWarnings("unchecked")
        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {

            Map<Long, RoiResult> rv = new HashMap<Long, RoiResult>();
            Map<Long, List<ome.model.roi.Roi>> iv = (Map<Long, List<ome.model.roi.Roi>>) value;

            RoiResultMapper m = new RoiResultMapper(opts);

            for (Map.Entry<Long, List<ome.model.roi.Roi>> entry : iv.entrySet()) {
                rv.put(entry.getKey(), (RoiResult) m.mapReturnValue(entry
                        .getValue()));
            }

            return rv;
        }

    }
}
