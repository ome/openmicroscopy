/*
 *   $Id$
 *
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rfloat;
import static omero.rtypes.rint;
import static omero.rtypes.rinternal;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;
import static omero.rtypes.rmap;
import static omero.rtypes.robject;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IContainer;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.ModelBased;
import ome.services.blitz.util.ConvertToBlitzExceptionMessage;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.util.Utils;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.data.RegionDef;
import omero.ApiUsageException;
import omero.RString;
import omero.RTime;
import omero.RType;
import omero.ServerError;
import omero.rtypes.Conversion;
import omero.model.NamedValue;
import omero.model.PermissionsI;
import omero.romio.BlueBand;
import omero.romio.GreenBand;
import omero.romio.PlaneDefWithMasks;
import omero.romio.RedBand;
import omero.romio.XY;
import omero.romio.XZ;
import omero.romio.ZY;
import omero.sys.EventContext;
import omero.sys.Filter;
import omero.sys.Options;
import omero.sys.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.UserException;

/**
 * Responsible for the mapping of ome.* types to omero.* types and back again.
 * Not all types are bidirectional, rather only those mappings are needed that
 * actually appear in the blitz API.
 * 
 * As of Beta3.1, an {@link IceMapper} instance can also be configured to handle
 * return value mapping, though by default an exception will be thrown if
 * {@link #mapReturnValue(Object)} is called.
 */
public class IceMapper extends ome.util.ModelMapper implements
        ReverseModelMapper {

    private static Logger log = LoggerFactory.getLogger(IceMapper.class);

    // Return value mapping
    // =========================================================================

    private final ReturnMapping mapping;

    public IceMapper() {
        this.mapping = null;
    }

    public IceMapper(ReturnMapping mapping) {
        this.mapping = mapping;
    }

    public interface ReturnMapping {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException;

    }

    public final static ReturnMapping VOID = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws UserException {
            if (value != null) {
                throw new IllegalArgumentException("Method is void");
            }
            return null;
        }

    };

    public final static ReturnMapping FILTERABLE = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws UserException {
            return mapper.map((Filterable) value);
        }

    };

    public final static ReturnMapping FILTERABLE_ARRAY = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws UserException {
            Filterable[] array = (Filterable[]) value;
            if (array == null) {
                return null;
            } else {
                List rv = new ArrayList(array.length);
                for (int i = 0; i < array.length; i++) {
                    rv.add(mapper.map(array[i]));
                }
                return rv;
            }
        }

    };

    public final static ReturnMapping FILTERABLE_COLLECTION = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws UserException {
            Collection<Filterable> coll = (Collection<Filterable>) value;
            if (coll == null) {
                return null;
            } else {
                List rv = new ArrayList();
                for (Filterable f : coll) {
                    rv.add(mapper.map(f));
                }
                return rv;
            }
        }

    };

    public final static ReturnMapping OBJECTARRAY_TO_RTYPESEQ = new ReturnMapping() {
        public Object mapReturnValue(IceMapper mapper, Object value)
        throws Ice.UserException {

            if (value == null) {
                return null;
            }

            Object[] objArr = (Object[]) value;
            List<RType> rv = new ArrayList<RType>();
            for (Object obj : objArr) {
                rv.add((RType) OBJECT_TO_RTYPE.mapReturnValue(mapper, obj));
            }

            return rv;
        }
    };

    @SuppressWarnings("unchecked")
    public final static ReturnMapping LISTOBJECTARRAY_TO_RTYPESEQSEQ = new ReturnMapping() {
        public Object mapReturnValue(IceMapper mapper, Object value)
        throws Ice.UserException {

            if (value == null) {
                return null;
            }

            List<Object[]> listObjArr = (List<Object[]>) value;
            List<List<RType>> rv = new ArrayList<List<RType>>();
            for (Object[] objs : listObjArr) {
                rv.add((List<RType>)OBJECTARRAY_TO_RTYPESEQ.mapReturnValue(mapper, objs));
            }

            return rv;
        }
    };

    public final static ReturnMapping OBJECT_TO_RTYPE = new ReturnMapping() {
        public Object mapReturnValue(IceMapper mapper, Object value)
        throws Ice.UserException {
            return mapper.toRType(value);
        }
    };

    public final static ReturnMapping STRING_TO_RSTRING = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            String str = (String) value;
            return omero.rtypes.rstring(str);
        }
    };

    /**
     * Specifies a return type which should not be parsed. This should
     * only be used for objects unknown to the Mapper, and should <em>not</em>
     * be used for any types which contain by transitivity any ome.model.* types!
     */
    public final static ReturnMapping UNMAPPED = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            return value;
        }
    };

    public final static ReturnMapping PRIMITIVE = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            if (value == null) {
                return null;
            } else {
                if (!IceMapper.isNullablePrimitive(value.getClass())) {
                    throw new RuntimeException(
                            "Object not nullable primitive: " + value);
                }
                return value;
            }
        }
    };

    public final static ReturnMapping PRIMITIVE_MAP = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            if (value == null) {
                return null;
            } else {
                Map map = (Map) value;
                Map rv = new HashMap();
                for (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object kr = PRIMITIVE.mapReturnValue(mapper, k);
                    Object vr = PRIMITIVE.mapReturnValue(mapper, v);
                    rv.put(kr, vr);
                }
                return rv;
            }
        }
    };

    public final static ReturnMapping FILTERABLE_PRIMITIVE_MAP = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            if (value == null) {
                return null;
            } else {
                Map map = (Map) value;
                Map rv = new HashMap();
                for (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object kr = FILTERABLE.mapReturnValue(mapper, k);
                    Object vr = PRIMITIVE.mapReturnValue(mapper, v);
                    rv.put(kr, vr);
                }
                return rv;
            }
        }
    };

    public final static ReturnMapping PRIMITIVE_FILTERABLE_COLLECTION_MAP = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            if (value == null) {
                return null;
            } else {
                Map map = (Map) value;
                Map rv = new HashMap();
                for (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object kr = PRIMITIVE.mapReturnValue(mapper, k);
                    Object vr = FILTERABLE_COLLECTION.mapReturnValue(mapper, v);
                    rv.put(kr, vr);
                }
                return rv;
            }
        }
    };

    public final static ReturnMapping RTYPEDICT = new ReturnMapping() {

        public Object mapReturnValue(IceMapper mapper, Object value)
                throws Ice.UserException {
            if (value == null) {
                return null;
            } else {
                Map map = (Map) value;
                Map rv = new HashMap();
                for (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object kr = PRIMITIVE.mapReturnValue(mapper, k);
                    Object vr = OBJECT_TO_RTYPE.mapReturnValue(mapper, v);
                    rv.put(kr, vr);
                }
                return rv;
            }
        }
    };

    /**
     * Returns true only if the current mapping is the {@link #VOID} mapping.
     */
    public boolean isVoid() {
        return canMapReturnValue() && mapping == VOID;
    }

    /**
     * True if this instance has a {@link ReturnMapping}
     */
    public boolean canMapReturnValue() {
        return mapping != null;
    }

    /**
     * Convert the given Object via the set {@link ReturnMapping}. Throws a
     * {@link NullPointException} if no mapping is set.
     */
    public Object mapReturnValue(Object value) throws Ice.UserException {
        return mapping.mapReturnValue(this, value);
    }

    // Exception handling
    // =========================================================================

    public static ServerError fillServerError(ServerError se, Throwable t) {
        se.message = t.getMessage();
        se.serverExceptionClass = t.getClass().getName();
        se.serverStackTrace = stackAsString(t);
        return se;
    }

    // Classes
    // =========================================================================

    private static Class<? extends IObject> _class(String className) {
        Class k = null;
        try {
            k = Class.forName(className);
        } catch (Exception e) {
            // ok
        }
        return k;
    }

    public static Class<? extends IObject> omeroClass(String className,
            boolean strict) throws ApiUsageException {

        Class k = _class(className);

        // If that didn't work, try to prefix with "omero.model"
        if (k == null) {
            k = _class("omero.model." + className);
        }

        // If either of those attempts worked, map it with IceMap unless
        // it's already in the key of OMEtoOMERO
        if (k != null) {
            if (IceMap.OMEtoOMERO.containsKey(k)) {
                // good
            } else {
                k = IceMap.OMEROtoOME.get(k);
            }
        }

        // For whatever reason, it's not valid. Log it.
        if (k == null) {
            if (log.isDebugEnabled()) {
                log.debug(className + " does not specify a valid class.");
            }
        }

        if (k == null && strict) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = className + " does not specify a valid class.";
            throw aue;
        }

        // Return, even null.
        return k;
    }

    // Conversions
    // =========================================================================

    public RType toRType(Object o) throws omero.ApiUsageException {
        if (o == null) {
            return null;
        } else if (o instanceof RType) {
            return (RType) o;
        } else if (o instanceof Boolean) {
            Boolean b = (Boolean) o;
            omero.RBool bool = rbool(b.booleanValue());
            return bool;
        } else if (o instanceof Date) {
            Date date = (Date) o;
            omero.RTime time = rtime(date.getTime());
            return time;
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            omero.RInt rint = rint(i);
            return rint;
        } else if (o instanceof Long) {
            Long lng = (Long) o;
            omero.RLong rlng = rlong(lng.longValue());
            return rlng;
        } else if (o instanceof Float) {
            Float flt = (Float) o;
            omero.RFloat rflt = rfloat(flt);
            return rflt;
        } else if (o instanceof Double) {
            Double dbl = (Double) o;
            omero.RDouble rdbl = rdouble(dbl.doubleValue());
            return rdbl;
        } else if (o instanceof String) {
            String str = (String) o;
            omero.RString rstr = rstring(str);
            return rstr;
        } else if (o instanceof IObject) {
            IObject obj = (IObject) o;
            omero.model.IObject om = (omero.model.IObject) map(obj);
            omero.RObject robj = robject(om);
            return robj;
        } else if (o instanceof Collection) {
            List<RType> l = new ArrayList<RType>();
            for (Object i : (Collection) o) {
                l.add(toRType(i));
            }
            return rlist(l);
        } else if (o instanceof Map) {
            Map<?, ?> mIn = (Map) o;
            Map<String, RType> mOut = new HashMap<String, RType>();
            for (Object k : mIn.keySet()) {
                if (!(k instanceof String)) {
                    throw new omero.ValidationException(
                            null, null, "Map key not a string");
                }
                mOut.put((String) k, toRType(mIn.get(k)));
            }
            return rmap(mOut);
        } else if (o instanceof omero.Internal) {
            return rinternal((omero.Internal) o);
        } else if (o instanceof ome.model.units.Unit) {
            ome.model.units.Unit u = (ome.model.units.Unit) o;
            Map<String, RType> rv = new HashMap<String, RType>();
            rv.put("string", rstring(u.toString()));
            rv.put("value", rdouble(u.getValue()));
            rv.put("unit", rstring(u.getUnit().toString()));
            rv.put("symbol", rstring(u.getUnit().getSymbol()));
            return rmap(rv);
        } else if (o instanceof ome.model.units.UnitEnum) {
            return rstring(((ome.model.units.UnitEnum) o).toString());
        } else {
            throw new ApiUsageException(null, null,
                    "Unsupported conversion to rtype from " + o.getClass().getName() + ":" + o);
        }
    }

    /**
     * Uses the omero.rtypes hierarchy to properly convert any {@link RType} to
     * its internal representation. This requires that the instance properly
     * implement {@link omero.rtypes.Conversion} otherwise ApiUsageException
     * will be thrown.
     * 
     * @param rt
     * @return
     * @throws omero.ApiUsageException
     */
    public Object fromRType(RType rt) throws omero.ApiUsageException {

        if (rt == null) {
            return null;
        }

        if (rt instanceof omero.rtypes.Conversion) {
            omero.rtypes.Conversion conv = (omero.rtypes.Conversion) rt;
            return conv.convert(this);
        } else {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = rt.getClass() + " is not a conversion type";
            throw aue;
        }

    }

    public static EventContext convert(ome.system.EventContext ctx) {
        if (ctx == null) {
            return null;
        }
        EventContext ec = new EventContext();
        Long event = ctx.getCurrentEventId();
        ec.eventId = event == null ? -1 : event;

        Long shareId = ctx.getCurrentShareId();
        ec.shareId = shareId == null ? -1 : shareId;
        ec.sessionId = ctx.getCurrentSessionId();
        ec.sessionUuid = ctx.getCurrentSessionUuid();
        ec.eventType = ctx.getCurrentEventType();
        ec.groupId = ctx.getCurrentGroupId();
        ec.groupName = ctx.getCurrentGroupName();
        ec.userId = ctx.getCurrentUserId();
        ec.userName = ctx.getCurrentUserName();
        ec.leaderOfGroups = ctx.getLeaderOfGroupsList();
        ec.memberOfGroups = ctx.getMemberOfGroupsList();
        ec.isAdmin = ctx.isCurrentUserAdmin();
        // ticket:2265 Removing from public interface
        // ec.isReadOnly = ctx.isReadOnly();
        ec.groupPermissions = convert(ctx.getCurrentGroupPermissions());
        return ec;
    }

    public static omero.romio.RGBBuffer convert(RGBBuffer buffer) {
        omero.romio.RGBBuffer b = new omero.romio.RGBBuffer();
        b.bands = new byte[3][];
        b.bands[RedBand.value] = buffer.getRedBand();
        b.bands[GreenBand.value] = buffer.getGreenBand();
        b.bands[BlueBand.value] = buffer.getBlueBand();
        b.sizeX1 = buffer.getSizeX1();
        b.sizeX2 = buffer.getSizeX2();
        return b;
    }

    /**
     * Converts the passed Ice Object and returns the converted object.
     * 
     * @param def The object to convert
     * @return See above.
     * @throws omero.ApiUsageException Thrown if the slice is unknown.
     */
    public static PlaneDef convert(omero.romio.PlaneDef def)
            throws omero.ApiUsageException {
        PlaneDef pd = new PlaneDef(def.slice, def.t);
        pd.setStride(def.stride);
        omero.romio.RegionDef r = def.region;
        if (r != null) {
            pd.setRegion(new RegionDef(r.x, r.y, r.width, r.height));
        }
        if (def instanceof PlaneDefWithMasks) {
            pd.setRenderShapes(true);
            if (((PlaneDefWithMasks) def).shapeIds != null) {
                pd.setShapeIds(((PlaneDefWithMasks) def).shapeIds);
            }
        }
        switch (def.slice) {
        case XY.value:
            pd.setZ(def.z);
            break;
        case ZY.value:
            pd.setX(def.x);
            break;
        case XZ.value:
            pd.setY(def.y);
            break;
        default:
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Unknown slice for " + def;
            throw aue;
        }

        return pd;
    }

    public static Principal convert(omero.sys.Principal old) {
        if (old == null) {
            return null;
        }
        return new Principal(old.name, old.group, old.eventType);
    }

    public static omero.sys.Roles convert(Roles roles) {
        omero.sys.Roles r = new omero.sys.Roles();
        r.rootId = roles.getRootId();
        r.rootName = roles.getRootName();
        r.systemGroupId = roles.getSystemGroupId();
        r.systemGroupName = roles.getSystemGroupName();
        r.userGroupId = roles.getUserGroupId();
        r.userGroupName = roles.getUserGroupName();
        r.guestId = roles.getGuestId();
        r.guestName = roles.getGuestName();
        r.guestGroupId = roles.getGuestGroupId();
        r.guestGroupName = roles.getGuestGroupName();
        return r;
    }

    public static RTime convert(Date date) {
        return rtime(date);
    }

    public static Timestamp convert(RTime time) {
        if (time == null) {
            return null;
        }
        return new Timestamp(time.getValue());
    }

    public ome.parameters.Parameters convert(Parameters params)
            throws ApiUsageException {

        if (params == null) {
            return null;
        }

        ome.parameters.Parameters p = new ome.parameters.Parameters();
        if (params.map != null) {
            for (String name : params.map.keySet()) {
                Object obj = params.map.get(name);
                p.add(convert(name, obj));
            }
        }
        if (params.theFilter != null) {
            p.setFilter(convert(params.theFilter));
        }

        if (params.theOptions != null) {
            p.setOptions(convert(params.theOptions));
        }
        return p;
    }

    public ome.parameters.QueryParameter convert(String key, Object o)
            throws ApiUsageException {

        if (o == null) {
            return null;
        }

        String name = key;
        Class klass = o.getClass();
        Object value = null;
        if (RType.class.isAssignableFrom(klass)) {
            value = fromRType((RType) o);
            // If fromRType passes correctly, then we're sure that we
            // can convert to rtypes.Conversion
            klass = ((Conversion) o).type();
        } else {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Query parameter must be a subclass of RType " + o;
            throw aue;
        }

        ome.parameters.QueryParameter qp = new ome.parameters.QueryParameter(
                name, klass, value);
        return qp;
    }

    public static ome.parameters.Options convert(Options o) {

        if (o == null) {
            return null;
        }

        ome.parameters.Options options = new ome.parameters.Options();

        if (o.orphan != null) {
            options.orphan = o.orphan.getValue();
        }

        if (o.leaves != null) {
            options.leaves= o.leaves.getValue();
        }

        if (o.acquisitionData != null) {
            options.acquisitionData = o.acquisitionData.getValue();
        }

        return options;
}

    public static ome.parameters.Filter convert(Filter f) {

        if (f == null) {
            return null;
        }

        ome.parameters.Filter filter = new ome.parameters.Filter();

        if (f.offset != null) {
            filter.offset = f.offset.getValue();
        }
        if (f.limit != null) {
            filter.limit = f.limit.getValue();
        }

        if (f.ownerId != null) {
            filter.owner(f.ownerId.getValue());
        }

        if (f.groupId != null) {
            filter.group(f.groupId.getValue());
        }

        if (f.startTime != null) {
            filter.startTime = convert(f.startTime);
        }

        if (f.endTime != null) {
            filter.endTime = convert(f.endTime);
        }

        if (f.unique != null && f.unique.getValue()) {
            filter.unique();
        }

        return filter;
    }

    public static List<NamedValue> convertNamedValueList(List<ome.model.internal.NamedValue> map) {
        if (map == null) {
            return null;
        }
        final List<NamedValue> nvl = new ArrayList<NamedValue>(map.size());
        for (final ome.model.internal.NamedValue nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final String name = nv.getName();
                final String value = nv.getValue();
                nvl.add(new NamedValue(name, value));
            }
        }
        return nvl;
    }

    public static List<NamedValue> convertMapPairs(List<ome.xml.model.MapPair> map) {
        if (map == null) {
            return null;
        }
        final List<NamedValue> nvl = new ArrayList<NamedValue>(map.size());
        for (final ome.xml.model.MapPair nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final String name = nv.getName();
                final String value = nv.getValue();
                nvl.add(new NamedValue(name, value));
            }
        }
        return nvl;
    }

    /**
     * Convert a String&rarr;String map's values to {@link RString}s.
     * <code>null</code> values are dropped completely.
     * @param map a map
     * @return the converted map, or <code>null</code> if <code>map == null</code>
     */
    public static Map<String, RString> convertStringStringMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        final Map<String, RString> rMap = new HashMap<String, RString>(map.size());
        for (final Map.Entry<String, String> mapEntry : map.entrySet()) {
            final String key = mapEntry.getKey();
            final String value = mapEntry.getValue();
            if (value != null) {
                rMap.put(key, rstring(value));
            }
        }
        return rMap;
    }

    /**
     * Overrides the findCollection logic of {@link ModelMapper}, since all
     * {@link Collection}s should be {@link List}s in Ice.
     * 
     * Originally necessitated by the Map<Long, Set<IObject>> return value of
     * {@link IContainer#findAnnotations(Class, Set, Set, Map)}
     */
    @Override
    public Collection findCollection(Collection source) {
        if (source == null) {
            return null;
        }

        Collection target = (Collection) model2target.get(source);
        if (null == target) {
            target = new ArrayList();
            model2target.put(source, target);
        }
        return target;
    }

    public List map(Filterable[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new ArrayList();
        } else {
            List l = new ArrayList(array.length);
            for (int i = 0; i < array.length; i++) {
                l.add(map(array[i]));
            }
            return l;
        }
    }

    // ~ For Reversing (omero->ome). Copied from ReverseModelMapper.
    // =========================================================================

    protected Map<Object, Object> target2model = new IdentityHashMap<Object, Object>();

    public static omero.model.Permissions convert(ome.model.internal.Permissions p) {
        if (p == null) {
            return null;
        }
        return new PermissionsI(p.toString());
    }

    public static ome.model.internal.Permissions convert(omero.model.Permissions p) {
        if (p == null) {
            return null;
        }
        return Utils.toPermissions(p.getPerm1());
    }

    // TODO copied with ModelMapper
    public boolean isImmutable(Object obj) {
        if (null == obj || obj instanceof Number || obj instanceof Number[]
                || obj instanceof String || obj instanceof String[]
                || obj instanceof Boolean || obj instanceof Boolean[]) {
            return true;
        }
        return false;
    }

    public Object reverse(Object source) throws ApiUsageException {
        if (source == null) {
            return null;
        } else if (Collection.class.isAssignableFrom(source.getClass())) {
            return reverse((Collection) source);
        } else if (ModelBased.class.isAssignableFrom(source.getClass())) {
            return reverse((ModelBased) source);
        } else if (isImmutable(source)) {
            return source;
        } else if (RType.class.isAssignableFrom(source.getClass())) {
            return fromRType((RType) source);
        } else {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Don't know how to reverse " + source;
            throw aue;
        }

    }

    /**
     * Copied from {@link ModelMapper#findCollection(Collection)} This could be
     * unified in that a method findCollection(Collection, Map) was added with
     * {@link ModelMapper} calling findCollection(source,model2target) and
     * {@link #reverseCollection(Collection)} calling
     * findCollection(source,target2model).
     * 
     * @param collection
     * @return
     */
    public Collection reverse(Collection source) { // FIXME throws
        // omero.ApiUsageException {
        return reverse(source, source == null ? null : source.getClass());
    }

    /**
     * Creates a collection assignable to the given type. Currently only
     * {@link Set} and {@link List} are supported, and {@link HashSet}s and
     * {@link ArrayList}s will be returned. The need for this arose from the
     * decision to have no {@link Set}s in the Ice Java mapping.
     * 
     * @param source
     * @param targetType
     * @return
     * @see ticket:684
     */
    public Collection reverse(Collection source, Class targetType) { // FIXME
        // throws
        // omero.ApiUsageException
        // {

        if (source == null) {
            return null;
        }

        Collection target = (Collection) target2model.get(source);
        if (null == target) {
            if (Set.class.isAssignableFrom(targetType)) {
                target = new HashSet();
            } else if (List.class.isAssignableFrom(targetType)) {
                target = new ArrayList();
            } else {
                // omero.ApiUsageException aue = new omero.ApiUsageException();
                // aue.message = "Unknown collection type "+targetType;
                // throw aue;
                throw new InternalException("Unknown collection type "
                        + targetType);
            }
            target2model.put(source, target);
            try {
                for (Object object : source) {
                    target.add(reverse(object));
                }
            } catch (ApiUsageException aue) { // FIXME reverse can't throw
                // ServerErrors!
                convertAndThrow(aue);
            }
        }
        return target;
    }

    /**
     * Supports the separate case of reversing for arrays. See
     * {@link #reverse(Collection, Class)} and {@link #map(Filterable[])}.
     * 
     * @param list
     * @param type
     * @return
     * @throws omero.ServerError
     */
    public Object[] reverseArray(List list, Class type)
            throws omero.ServerError {

        if (list == null) {
            return null;
        }

        Class component = type.getComponentType();
        Object[] array = null;
        try {

            array = (Object[]) Array.newInstance(component, list.size());
            for (int i = 0; i < array.length; i++) {
                array[i] = this.handleInput(component, list.get(i));
            }
        } catch (Exception e) {
            String msg = "Cannot create array from type " + type;
            if (log.isErrorEnabled()) {
                log.error(msg, e);
            }
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = msg;
            aue.serverExceptionClass = e.getClass().getName();
            throw aue;
        }

        return array;
    }

    public Map reverse(Map map) {
        if (map == null) {
            return null;
        }

        if (target2model.containsKey(map)) {
            return (Map) target2model.get(map);
        }

        Map<Object, Object> target = new HashMap<Object, Object>();
        target2model.put(map, target);

        try {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                Object targetKey = reverse(key);
                Object targetValue = reverse(value);
                target.put(targetKey, targetValue);
            }
        } catch (ApiUsageException aue) {
            convertAndThrow(aue);
        }
        return target;
    }

    /**
     * Copied from {@link ReverseModelMapper#map(ModelBased)}
     * 
     * @param source
     */
    public Filterable reverse(ModelBased source) {

        if (source == null) {

            return null;

        } else if (target2model.containsKey(source)) {

            return (Filterable) target2model.get(source);

        } else {
            Filterable object = source.fillObject(this);
            target2model.put(source, object);
            return object;

        }
    }

    /**
     * Reverse a String&rarr;String map's values from {@link RString}s.
     * <code>null</code> values are dropped completely.
     * @param rMap a map
     * @return the reversed map, or <code>null</code> if <code>rMap == null</code>
     */
    public static Map<String, String> reverseStringStringMap(Map<String, RString> rMap) {
        if (rMap == null) {
            return null;
        }
        final Map<String, String> map = new HashMap<String, String>(rMap.size());
        for (final Map.Entry<String, RString> rMapEntry : rMap.entrySet()) {
            final String key = rMapEntry.getKey();
            final RString rValue = rMapEntry.getValue();
            final String value = rValue == null ? null : rValue.getValue();
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static List<ome.model.internal.NamedValue> reverseNamedList(List<NamedValue> map) {
        if (map == null) {
            return null;
        }
        final List<ome.model.internal.NamedValue> nvl = new ArrayList<ome.model.internal.NamedValue>(map.size());
        for (final NamedValue nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final String name = nv.name;
                final String value = nv.value;
                nvl.add(new ome.model.internal.NamedValue(name, value));
            }
        }
        return nvl;
    }

    public void store(Object source, Object target) {
        target2model.put(source, target);
    }

    // ~ For ome->omero parsing
    // =========================================================================
    @Override
    protected Map c2c() {
        return IceMap.OMEtoOMERO;
    }

    private void fillTarget(Filterable source, ModelBased target) {
        if (source != null && target != null) {
            target.copyObject(source, this);
        }
    }

    @Override
    public Filterable filter(String fieldId, Filterable source) {
        // Filterable o = super.filter(fieldId,source);
        // Can't call super here!!
        if (hasntSeen(source)) {
            // log.info("Haven't seen. Stepping into "+f);
            enter(source);
            addSeen(source);
            source.acceptFilter(this);
            exit(source);
        }

        Object target = findTarget(source);
        fillTarget(source, (ModelBased) target); // FIXME cast
        return source;
    }

    @Override
    protected boolean hasntSeen(Object o) {
        return o == null ? false : super.hasntSeen(o);
    }

    private void convertAndThrow(ApiUsageException aue) {
        InternalException ie = new InternalException(aue.getMessage());
        ie.setStackTrace(aue.getStackTrace());
    }

    // ~ Methods from IceMethodInvoker
    // =========================================================================

    protected boolean isPrimitive(Class<?> p) {
        if (p.equals(byte.class) || p.equals(byte[].class)
                || p.equals(int.class) || p.equals(int[].class)
                || p.equals(long.class) || p.equals(long[].class)
                || p.equals(double.class) || p.equals(double[].class)
                || p.equals(float.class) || p.equals(float[].class)
                || p.equals(boolean.class) || p.equals(boolean[].class)
                || p.equals(String.class)) {
            return true;
        }
        return false;
    }

    protected static boolean isNullablePrimitive(Class<?> p) {
        if (p.equals(String.class) || p.equals(Integer.class)
                || p.equals(Integer[].class) || p.equals(Long.class)
                || p.equals(Long[].class) || p.equals(Float.class)
                || p.equals(Float[].class) || p.equals(Double.class)
                || p.equals(Double[].class) || p.equals(Boolean.class)
                || p.equals(Boolean[].class)) {
            return true;
        }
        return false;
    }

    protected static boolean isWrapperArray(Class<?> p) {
        if (p.equals(Integer[].class) || p.equals(Long[].class)
                || p.equals(Double[].class) || p.equals(Float[].class)
                || p.equals(String[].class)) {
            return true;
        }
        return false;
    }

    public Object handleInput(Class<?> p, Object arg) throws ServerError {
        if (arg instanceof RType) {
            RType rt = (RType) arg;
            return fromRType(rt);
        } else if (isPrimitive(p) || isNullablePrimitive(p)) {
            // FIXME use findTarget for Immutable.
            return arg;
        } else if (isWrapperArray(p)) {
            return reverseArray((List) arg, p);
        } else if (p.equals(Class.class)) {
            return omeroClass((String) arg, true);
        } else if (ome.model.internal.Details.class.isAssignableFrom(p)) {
            return reverse((ModelBased) arg);
        } else if (ome.model.IObject.class.isAssignableFrom(p)) {
            return reverse((ModelBased) arg);
        } else if (p.equals(ome.parameters.Filter.class)) {
            return convert((omero.sys.Filter) arg);
        } else if (p.equals(ome.system.Principal.class)) {
            return convert((omero.sys.Principal) arg);
        } else if (p.equals(ome.parameters.Parameters.class)) {
            return convert((omero.sys.Parameters) arg);
        } else if (List.class.isAssignableFrom(p)) {
            return reverse((Collection) arg);
        } else if (Set.class.isAssignableFrom(p)) {
            return reverse(new HashSet((Collection) arg)); // Necessary
            // since Ice
            // doesn't
            // support
            // Sets.
        } else if (Collection.class.isAssignableFrom(p)) {
            return reverse((Collection) arg);
        } else if (Timestamp.class.isAssignableFrom(p)) {
            if (arg != null) {
                throw new RuntimeException("This must be null here");
            }
            return null;
        } else if (Map.class.isAssignableFrom(p)) {
            return reverse((Map) arg);
        } else if (PlaneDef.class.isAssignableFrom(p)) {
            return convert((omero.romio.PlaneDef) arg);
        } else if (Object[].class.isAssignableFrom(p)) {
            return reverseArray((List) arg, p);
        } else {
            throw new ApiUsageException(null, null, "Can't handle input " + p);
        }
    }

    public Object handleOutput(Class type, Object o) throws ServerError {
        if (o == null) {
            return null;
        } else if (RType.class.isAssignableFrom(type)) {
            return o;
        } else if (omero.Internal.class.isAssignableFrom(type)) {
            return rinternal((omero.Internal) o);
        } else if (void.class.isAssignableFrom(type)) {
            assert o == null;
            return null;
        } else if (isPrimitive(type)) {
            return o;
        } else if (isNullablePrimitive(type)) {
            return toRType(o);
        } else if (RGBBuffer.class.isAssignableFrom(type)) {
            return convert((RGBBuffer) o);
        } else if (Roles.class.isAssignableFrom(type)) {
            return convert((Roles) o);
        } else if (Date.class.isAssignableFrom(type)) {
            return convert((Date) o);
        } else if (ome.system.EventContext.class.isAssignableFrom(type)) {
            return convert((ome.system.EventContext) o);
        } else if (Set.class.isAssignableFrom(type)) {
            return map(new ArrayList((Set) o)); // Necessary since Ice
            // doesn't support Sets.
        } else if (Collection.class.isAssignableFrom(type)) {
            return map((Collection) o);
        } else if (IObject.class.isAssignableFrom(type)) {
            return map((Filterable) o);
        } else if (Map.class.isAssignableFrom(type)) {
            return map((Map) o);
        } else if (Filterable[].class.isAssignableFrom(type)) {
            return map((Filterable[]) o);
        } else {
            throw new ApiUsageException(null, null, "Can't handle output "
                    + type);
        }
    }

    /**
     * wraps any non-ServerError returned by
     * {@link #handleException(Throwable, OmeroContext)} in an
     * {@link InternalException}.
     *
     * @param t
     * @param ctx
     * @return
     */
    public ServerError handleServerError(Throwable t, OmeroContext ctx) {
        Ice.UserException ue = handleException(t, ctx);
        if (ue instanceof ServerError) {
            return (ServerError) ue;
        }
        omero.InternalException ie = new omero.InternalException();
        IceMapper.fillServerError(ie, ue);
        return ie;

    }

    public Ice.UserException handleException(Throwable t, OmeroContext ctx) {

        // Getting rid of the reflection wrapper.
        if (InvocationTargetException.class.isAssignableFrom(t.getClass())) {
            t = t.getCause();
        }

        if (log.isDebugEnabled()) {
            log.debug("Handling:", t);
        }

        // First we give registered handlers a chance to convert the message,
        // if that doesn't succeed, then we try either manually, or just
        // wrap the exception in an InternalException
        if (ctx != null) {
            try {
                ConvertToBlitzExceptionMessage ctbem =
                    new ConvertToBlitzExceptionMessage(this, t);
                ctx.publishMessage(ctbem);
                if (ctbem.to != null) {
                    t = ctbem.to;
                }
            } catch (Throwable handlerT) {
                // Logging the output, but we shouldn't worry the user
                // with a failing handler
                log.error("Exception handler failure", handlerT);
            }
        }

        Class c = t.getClass();

        if (Ice.UserException.class.isAssignableFrom(c)) {
            return (Ice.UserException) t;
        }

        // API USAGE

        else if (ome.conditions.OptimisticLockException.class
                .isAssignableFrom(c)) {
            omero.OptimisticLockException ole = new omero.OptimisticLockException();
            return IceMapper.fillServerError(ole, t);

        }

        else if (ome.conditions.OverUsageException.class.isAssignableFrom(c)) {
            omero.OverUsageException oue = new omero.OverUsageException();
            return IceMapper.fillServerError(oue, t);

        }

        else if (ome.services.query.QueryException.class.isAssignableFrom(c)) {
            omero.QueryException qe = new omero.QueryException();
            return IceMapper.fillServerError(qe, t);

        }

        else if (ome.conditions.ValidationException.class.isAssignableFrom(c)) {
            omero.ValidationException ve = new omero.ValidationException();
            return IceMapper.fillServerError(ve, t);

        }

        else if (ome.conditions.ApiUsageException.class.isAssignableFrom(c)) {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            return IceMapper.fillServerError(aue, t);
        }

        // CONCURRENCY

        else if (ome.conditions.MissingPyramidException.class
                .isAssignableFrom(c)) {
            omero.MissingPyramidException mpe = new omero.MissingPyramidException();
            mpe.backOff = ((ome.conditions.MissingPyramidException) t).backOff;
            mpe.pixelsID = ((ome.conditions.MissingPyramidException) t).getPixelsId();
            return IceMapper.fillServerError(mpe, t);
        }

        else if (ome.conditions.TryAgain.class
                .isAssignableFrom(c)) {
            omero.TryAgain ta = new omero.TryAgain();
            ta.backOff = ((ome.conditions.TryAgain) t).backOff;
            return IceMapper.fillServerError(ta, t);
        }

        else if (ome.conditions.LockTimeout.class
                .isAssignableFrom(c)) {
            omero.LockTimeout lt = new omero.LockTimeout();
            lt.backOff = ((ome.conditions.LockTimeout) t).backOff;
            lt.seconds = ((ome.conditions.LockTimeout) t).seconds;
            return IceMapper.fillServerError(lt, t);
        }

        else if (ome.conditions.DatabaseBusyException.class.isAssignableFrom(c)) {
            omero.DatabaseBusyException dbe = new omero.DatabaseBusyException();
            return IceMapper.fillServerError(dbe, t);
        }

        else if (ome.conditions.ConcurrencyException.class.isAssignableFrom(c)) {
            omero.ConcurrencyException re = new omero.ConcurrencyException();
            return IceMapper.fillServerError(re, t);
        }

        // RESOURCE

        else if (ome.conditions.ResourceError.class.isAssignableFrom(c)) {
            omero.ResourceError re = new omero.ResourceError();
            return IceMapper.fillServerError(re, t);
        }

        // SECURITY

        else if (ome.conditions.ReadOnlyGroupSecurityViolation.class.isAssignableFrom(c)) {
            omero.ReadOnlyGroupSecurityViolation sv = new omero.ReadOnlyGroupSecurityViolation();
            return IceMapper.fillServerError(sv, t);
        }

        else if (ome.conditions.GroupSecurityViolation.class.isAssignableFrom(c)) {
            omero.GroupSecurityViolation sv = new omero.GroupSecurityViolation();
            return IceMapper.fillServerError(sv, t);
        }

        else if (ome.conditions.SecurityViolation.class.isAssignableFrom(c)) {
            omero.SecurityViolation sv = new omero.SecurityViolation();
            return IceMapper.fillServerError(sv, t);
        }

        // SESSIONS

        else if (ome.conditions.RemovedSessionException.class
                .isAssignableFrom(c)) {
            omero.RemovedSessionException rse = new omero.RemovedSessionException();
            return IceMapper.fillServerError(rse, t);
        }

        else if (ome.conditions.SessionTimeoutException.class
                .isAssignableFrom(c)) {
            omero.SessionTimeoutException ste = new omero.SessionTimeoutException();
            return IceMapper.fillServerError(ste, t);
        }

        else if (ome.conditions.AuthenticationException.class
                .isAssignableFrom(c)) {
            // not an omero.ServerError()
            omero.AuthenticationException ae = new omero.AuthenticationException(
                    t.getMessage());
            return ae;
        }

        else if (ome.conditions.ExpiredCredentialException.class
                .isAssignableFrom(c)) {
            // not an omero.ServerError()
            omero.ExpiredCredentialException ece = new omero.ExpiredCredentialException(
                    t.getMessage());
            return ece;
        }

        // INTERNAL etc.

        else if (ome.conditions.InternalException.class.isAssignableFrom(c)) {
            omero.InternalException ie = new omero.InternalException();
            return IceMapper.fillServerError(ie, t);
        }

        else if (ome.conditions.RootException.class.isAssignableFrom(c)) {
            // Not returning but logging error message.
            log
                    .error("RootException thrown which is an unknown subclasss.\n"
                            + "This most likely means that an exception was added to the\n"
                            + "ome.conditions hierarchy, without being accountd for in blitz:\n"
                            + c.getName());
        }

        // Catch all in case above did not return
        omero.InternalException ie = new omero.InternalException();
        return IceMapper.fillServerError(ie, t);

    }
}
