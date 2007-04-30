/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

// Java imports
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.api.IPojos;
import ome.api.ModelBased;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.system.Roles;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.data.PlaneDef;
import omero.ApiUsageException;
import omero.RBool;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.Time;
import omero.romio.BlueBand;
import omero.romio.GreenBand;
import omero.romio.RedBand;
import omero.romio.XY;
import omero.romio.XZ;
import omero.romio.ZY;
import omero.sys.EventContext;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.sys.QueryParam;
import omero.sys.Type;

/**
 * Code-generated
 */
public class IceMapper extends ome.util.ModelMapper implements ReverseModelMapper {

    private static Log log = LogFactory.getLog(IceMapper.class);

    // Exception handling

    public static ServerError fillServerError(ServerError se, Throwable t) {
        se.message = t.getMessage();
        se.serverExceptionClass = t.getClass().getName();
        se.serverStackTrace = stackAsString(t);
        return se;
    }

    public static String stackAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        Throwable cause = t.getCause();
        while (cause != null && cause != t ) {
            cause.printStackTrace(pw);
            t = cause;
            cause = t.getCause();
        }
        pw.flush();
        pw.close();

        return sw.getBuffer().toString();
    }

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

        // If either of those attempts worked, map it with IceMap
        if (k != null) {
            k = IceMap.OMEROtoOME.get(k);
        }

        // For whatever reason, it's not valid. Log it.
        if (k == null) {
            if (log.isDebugEnabled()) {
                log.debug(className + " does not specify a valid class.");
            }
        }

        if (k == null && strict) {
            ApiUsageException aue =  new ApiUsageException();
            aue.message = className + " does not specify a valid class.";
            throw aue;
        }

        // Return, even null.
        return k;
    }

    public static Object convert(RType rt) {
        if (rt._null) return null;
            Field f;
            try {
                f = rt.getClass().getField("val");
                return f.get(rt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public static EventContext convert(ome.system.EventContext ctx) {
        EventContext ec = new EventContext();
        ec.eventId = ctx.getCurrentEventId();
        ec.eventType = ctx.getCurrentEventType();
        ec.groupId = ctx.getCurrentGroupId();
        ec.groupName = ctx.getCurrentGroupName();
        ec.userId = ctx.getCurrentUserId();
        ec.userName = ctx.getCurrentUserName();
        ec.leaderOfGroups = ctx.getLeaderOfGroupsList();
        ec.memberOfGroups = ctx.getMemberOfGroupsList();
        ec.isAdmin = ctx.isCurrentUserAdmin();
        ec.isReadOnly = ctx.isReadOnly();
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

    public static PlaneDef convert(omero.romio.PlaneDef def) {
        PlaneDef pd = new PlaneDef(def.slice, def.t);
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
                throw new IllegalArgumentException("Unknown slice for "+def);
        }

        return pd;
    }

    public static omero.sys.Roles convert(Roles roles) {
        omero.sys.Roles r = new omero.sys.Roles();
        r.rootId = roles.getRootId();
        r.rootName = roles.getRootName();
        r.systemGroupId = roles.getSystemGroupId();
        r.systemGroupName = roles.getSystemGroupName();
        r.userGroupId = roles.getUserGroupId();
        r.userGroupName = roles.getUserGroupName();
        return r;
    }

    public static Time convert(Date date) {
        Time t = new Time();
        t.val = date.getTime();
        return t;
    }

    public ome.parameters.Parameters convert(Parameters params)
    throws ApiUsageException {

        if (params == null) return null;

        ome.parameters.Parameters p = new ome.parameters.Parameters();
        if (params.map != null) {
            for (Object obj : params.map.values()) {
                QueryParam qp = (QueryParam) obj;
                p.add(convert(qp));
            }
        }
        if (params.filt != null) {
            p.setFilter(convert(params.filt));
        }
        return p;
    }

    public ome.parameters.QueryParameter convert(QueryParam qParam)
    throws ApiUsageException {

        if (qParam == null) return null;

        String name = qParam.name;
        Object value = null;
        Class klass = null;
        int t = qParam.paramType.value();
        switch (t) {
            case Type._boolType:
                value = Boolean.valueOf(qParam.boolVal);
                klass = Boolean.class;
                break;
            case Type._intType:
                value = Integer.valueOf(qParam.intVal);
                klass = Integer.class;
                break;
            case Type._longType:
                value = Long.valueOf(qParam.longVal);
                klass = Long.class;
                break;
            case Type._floatType:
                value = Float.valueOf(qParam.floatVal);
                value = Float.class;
                break;
            case Type._doubleType:
                value = Double.valueOf(qParam.doubleVal);
                value = Double.class;
                break;
            case Type._stringType:
                value = qParam.stringVal;
                klass = String.class;
                break;
            case Type._classType:
                value = IceMapper.omeroClass(qParam.classVal, true);
                klass = Class.class;
                break;
            case Type._timeType:
                omero.RTime rt = qParam.timeVal;
                value = (rt == null ? null : rt._null ? null : new Timestamp(rt.val.val));
                klass = Timestamp.class;
                break;
            case Type._objectType:
                omero.RObject ro = qParam.objectVal;
                value = (ro == null ? null : ro._null ? null : reverse(ro.val));
                klass = IObject.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown type code:" + t);
        }

        ome.parameters.QueryParameter qp = new ome.parameters.QueryParameter(
                name, klass, value);
        return qp;
    }

    public static ome.parameters.Filter convert(Filter f) {

        if (f==null) return null;

        ome.parameters.Filter filter = new ome.parameters.Filter();

        int offset = 0, limit = Integer.MAX_VALUE;
        if (f.offset != null && ! f.offset._null) {
            offset = f.offset.val;
        }
        if (f.limit != null && ! f.limit._null) {
            limit = f.limit.val;
        }
        filter.page(offset, limit);

        if (f.ownerId != null && ! f.ownerId._null ) {
            filter.owner(f.ownerId.val);
        }

        if (f.groupId != null && ! f.groupId._null ) {
            filter.group(f.groupId.val);
        }

        if (f.unique) {
            filter.unique();
        }

        return filter;
    }

    /** Overrides the findCollection logic of {@link ModelMapper}, since all
     * {@link Collection}s should be {@link List}s in Ice.
     *
     * Originally necessitated by the Map<Long, Set<IObject>> return value of
     * {@link IPojos#findAnnotations(Class, Set, Set, Map)}
     */
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

    // ~ For Reversing (omero->ome). Copied from ReverseModelMapper.
    // =========================================================================

    protected Map target2model = new IdentityHashMap();

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
        } else if (QueryParam.class.isAssignableFrom(source.getClass())) {
            return convert((QueryParam)source);
        } else {
            throw new IllegalArgumentException("Don't know how to reverse "+source);
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
    public Collection reverse(Collection source) {
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
    public Collection reverse(Collection source, Class targetType) {

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
                throw new RuntimeException("Unknown collection type: "
                        + targetType);
            }
            target2model.put(source, target);
            try {
                for (Object object : source) {
                    target.add(reverse(object));
                }
            } catch (ApiUsageException aue) { // FIXME reverse can't throw ServerErrors!
                convertAndThrow(aue);
            }
        }
        return target;
    }

    public Map reverse(Map map) {
        if (map == null) return null;
        Map<Object, Object> target = new HashMap<Object, Object>();
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
     * @return
     */
    public Filterable reverse(ModelBased source) {

        if (source == null) {

            return null;

        } else if (target2model.containsKey(source)) {

            return (ome.model.IObject) target2model.get(source);

        } else {

            Filterable object = source.fillObject(this);
            target2model.put(source, object);
            return object;

        }
    }

    public void store(Object source, Object target) {
        target2model.put(source, target);
    }

    // ~ For ome->omero parsing
    // =========================================================================
    protected Map c2c() {
        return IceMap.OMEtoOMERO;
    }

    private void fillTarget(Filterable source, ModelBased target) {
        if (source != null && target != null) {
            target.copyObject(source, this);
        }
    }

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

}
