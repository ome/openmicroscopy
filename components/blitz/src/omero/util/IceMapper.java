/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

// Java imports
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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

import ome.api.IPojos;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.ModelBased;
import ome.system.Principal;
import ome.system.Roles;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.data.PlaneDef;
import omero.ApiUsageException;
import omero.JBool;
import omero.JClass;
import omero.JDouble;
import omero.JFloat;
import omero.JInt;
import omero.JList;
import omero.JLong;
import omero.JObject;
import omero.JSet;
import omero.JString;
import omero.JTime;
import omero.RArray;
import omero.RBool;
import omero.RClass;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RObject;
import omero.RSet;
import omero.RString;
import omero.RTime;
import omero.RType;
import omero.ServerError;
import omero.romio.BlueBand;
import omero.romio.GreenBand;
import omero.romio.RedBand;
import omero.romio.XY;
import omero.romio.XZ;
import omero.romio.ZY;
import omero.sys.EventContext;
import omero.sys.Filter;
import omero.sys.Parameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for the mapping of ome.* types to omero.* types and back again.
 * Not all types are bidirectional, rather only those mappings are needed that
 * actually appear in the blitz API.
 */
public class IceMapper extends ome.util.ModelMapper implements
        ReverseModelMapper {

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
        while (cause != null && cause != t) {
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
            ApiUsageException aue = new ApiUsageException();
            aue.message = className + " does not specify a valid class.";
            throw aue;
        }

        // Return, even null.
        return k;
    }

    public Object convert(RType rt) throws omero.ApiUsageException {

        if (rt == null) {
            return null;
        }

        Field f;
        Object rv;
        try {
            f = rt.getClass().getField("val");
            rv = f.get(rt);
        } catch (Exception e) {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            fillServerError(aue, e);
            aue.message = "Cannot get value for " + rt + ":" + aue.message;
            throw aue;
        }

        // Next round of conversions on the value itself.
        if (RTime.class.isAssignableFrom(rt.getClass())) {
            rv = new Timestamp((Long) rv);
        } else if (RClass.class.isAssignableFrom(rt.getClass())) {
            rv = omeroClass((String) rv, true);
        } else if (RArray.class.isAssignableFrom(rt.getClass())) {
            RArray arr = (RArray) rt;
            Collection reversed = reverse(arr.val);
            // Assuming all the same
            Class k = rtypeTypes.get(arr.val.get(0).getClass());
            rv = Array.newInstance(k, arr.val.size());
            rv = reversed.toArray((Object[]) rv);
        } else if (RSet.class.isAssignableFrom(rt.getClass())) {
            RSet set = (RSet) rt;
            rv = reverse(set.val, Set.class);
        } else if (RList.class.isAssignableFrom(rt.getClass())) {
            RList list = (RList) rt;
            rv = reverse(list.val, List.class);
        } else {
            rv = reverse(rv); // TODO Any optimizations to be had here?
        }
        return rv;
    }

    public static EventContext convert(ome.system.EventContext ctx) {
        EventContext ec = new EventContext();
        ec.sessionId = ctx.getCurrentSessionId();
        ec.sessionUuid = ctx.getCurrentSessionUuid();
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

    public static PlaneDef convert(omero.romio.PlaneDef def)
            throws omero.ApiUsageException {
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
        r.guestGroupName = roles.getGuestGroupName();
        return r;
    }

    public static RTime convert(Date date) {
        return new JTime(date);
    }

    public static Timestamp convert(RTime time) {
        if (time == null) {
            return null;
        }
        return new Timestamp(time.val);
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
        return p;
    }

    static final Map<Class, Class> rtypeTypes;
    static {
        Map<Class, Class> tmp = new HashMap<Class, Class>();
        tmp.put(RBool.class, Boolean.class);
        tmp.put(JBool.class, Boolean.class);
        tmp.put(RFloat.class, Float.class);
        tmp.put(JFloat.class, Float.class);
        tmp.put(RInt.class, Integer.class);
        tmp.put(JInt.class, Integer.class);
        tmp.put(RDouble.class, Double.class);
        tmp.put(JDouble.class, Double.class);
        tmp.put(RLong.class, Long.class);
        tmp.put(JLong.class, Long.class);
        tmp.put(RString.class, String.class);
        tmp.put(JString.class, String.class);
        tmp.put(RClass.class, Class.class);
        tmp.put(JClass.class, Class.class);
        tmp.put(RObject.class, IObject.class);
        tmp.put(JObject.class, IObject.class);
        tmp.put(RList.class, Collection.class);
        tmp.put(JList.class, Collection.class);
        // tmp.put(RArray.class,Collection.class);
        // tmp.put(JArray.class,Collection.class);
        tmp.put(RSet.class, Collection.class);
        tmp.put(JSet.class, Collection.class);
        tmp.put(RTime.class, Timestamp.class);
        tmp.put(JTime.class, Timestamp.class);
        rtypeTypes = tmp;
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
            value = convert((RType) o);
            klass = rtypeTypes.get(klass);
        } else {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Query parameter must be a subclass of RType " + o;
            throw aue;
        }

        ome.parameters.QueryParameter qp = new ome.parameters.QueryParameter(
                name, klass, value);
        return qp;
    }

    public static ome.parameters.Filter convert(Filter f) {

        if (f == null) {
            return null;
        }

        ome.parameters.Filter filter = new ome.parameters.Filter();

        int offset = 0, limit = Integer.MAX_VALUE;
        if (f.offset != null) {
            offset = f.offset.val;
        }
        if (f.limit != null) {
            limit = f.limit.val;
        }
        filter.page(offset, limit);

        if (f.ownerId != null) {
            filter.owner(f.ownerId.val);
        }

        if (f.groupId != null) {
            filter.group(f.groupId.val);
        }

        if (f.unique) {
            filter.unique();
        }

        return filter;
    }

    /**
     * Overrides the findCollection logic of {@link ModelMapper}, since all
     * {@link Collection}s should be {@link List}s in Ice.
     * 
     * Originally necessitated by the Map<Long, Set<IObject>> return value of
     * {@link IPojos#findAnnotations(Class, Set, Set, Map)}
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
        } else if (RType.class.isAssignableFrom(source.getClass())) {
            return convert((RType) source);
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
    public Filterable[] reverseArray(List list, Class type)
            throws omero.ServerError {

        if (list == null) {
            return null;
        }

        Class component = type.getComponentType();
        Filterable[] array = null;
        try {

            array = (Filterable[]) Array.newInstance(component, list.size());
            for (int i = 0; i < array.length; i++) {
                array[i] = (Filterable) reverse(list.get(i));
            }
        } catch (Exception e) {
            String msg = "Cannot create filterable array from type " + type;
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

}
