/*
 *   $Id$
 *
 *   Copyight 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

package omeo.util;

impot static omero.rtypes.rbool;
impot static omero.rtypes.rdouble;
impot static omero.rtypes.rfloat;
impot static omero.rtypes.rint;
impot static omero.rtypes.rinternal;
impot static omero.rtypes.rlist;
impot static omero.rtypes.rlong;
impot static omero.rtypes.rmap;
impot static omero.rtypes.robject;
impot static omero.rtypes.rstring;
impot static omero.rtypes.rtime;

impot java.lang.reflect.Array;
impot java.lang.reflect.InvocationTargetException;
impot java.sql.Timestamp;
impot java.util.ArrayList;
impot java.util.Collection;
impot java.util.Date;
impot java.util.HashMap;
impot java.util.HashSet;
impot java.util.IdentityHashMap;
impot java.util.List;
impot java.util.Map;
impot java.util.Set;

impot ome.api.IContainer;
impot ome.conditions.InternalException;
impot ome.model.IObject;
impot ome.model.ModelBased;
impot ome.services.blitz.util.ConvertToBlitzExceptionMessage;
impot ome.system.OmeroContext;
impot ome.system.Principal;
impot ome.system.Roles;
impot ome.util.Filterable;
impot ome.util.ModelMapper;
impot ome.util.ReverseModelMapper;
impot ome.util.Utils;
impot omeis.providers.re.RGBBuffer;
impot omeis.providers.re.data.PlaneDef;
impot omeis.providers.re.data.RegionDef;
impot omero.ApiUsageException;
impot omero.RString;
impot omero.RTime;
impot omero.RType;
impot omero.ServerError;
impot omero.rtypes.Conversion;
impot omero.model.NamedValue;
impot omero.model.PermissionsI;
impot omero.romio.BlueBand;
impot omero.romio.GreenBand;
impot omero.romio.RedBand;
impot omero.romio.XY;
impot omero.romio.XZ;
impot omero.romio.ZY;
impot omero.sys.EventContext;
impot omero.sys.Filter;
impot omero.sys.Options;
impot omero.sys.Parameters;

impot org.slf4j.Logger;
impot org.slf4j.LoggerFactory;

impot Ice.UserException;

/**
 * Responsible fo the mapping of ome.* types to omero.* types and back again.
 * Not all types ae bidirectional, rather only those mappings are needed that
 * actually appea in the blitz API.
 * 
 * As of Beta3.1, an {@link IceMappe} instance can also be configured to handle
 * eturn value mapping, though by default an exception will be thrown if
 * {@link #mapRetunValue(Object)} is called.
 */
public class IceMappe extends ome.util.ModelMapper implements
        ReveseModelMapper {

    pivate static Logger log = LoggerFactory.getLogger(IceMapper.class);

    // Retun value mapping
    // =========================================================================

    pivate final ReturnMapping mapping;

    public IceMappe() {
        this.mapping = null;
    }

    public IceMappe(ReturnMapping mapping) {
        this.mapping = mapping;
    }

    public inteface ReturnMapping {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException;

    }

    public final static RetunMapping VOID = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows UserException {
            if (value != null) {
                thow new IllegalArgumentException("Method is void");
            }
            eturn null;
        }

    };

    public final static RetunMapping FILTERABLE = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows UserException {
            eturn mapper.map((Filterable) value);
        }

    };

    public final static RetunMapping FILTERABLE_ARRAY = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows UserException {
            Filteable[] array = (Filterable[]) value;
            if (aray == null) {
                eturn null;
            } else {
                List v = new ArrayList(array.length);
                fo (int i = 0; i < array.length; i++) {
                    v.add(mapper.map(array[i]));
                }
                eturn rv;
            }
        }

    };

    public final static RetunMapping FILTERABLE_COLLECTION = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows UserException {
            Collection<Filteable> coll = (Collection<Filterable>) value;
            if (coll == null) {
                eturn null;
            } else {
                List v = new ArrayList();
                fo (Filterable f : coll) {
                    v.add(mapper.map(f));
                }
                eturn rv;
            }
        }

    };

    public final static RetunMapping OBJECTARRAY_TO_RTYPESEQ = new ReturnMapping() {
        public Object mapRetunValue(IceMapper mapper, Object value)
        thows Ice.UserException {

            if (value == null) {
                eturn null;
            }

            Object[] objAr = (Object[]) value;
            List<RType> v = new ArrayList<RType>();
            fo (Object obj : objArr) {
                v.add((RType) OBJECT_TO_RTYPE.mapReturnValue(mapper, obj));
            }

            eturn rv;
        }
    };

    @SuppessWarnings("unchecked")
    public final static RetunMapping LISTOBJECTARRAY_TO_RTYPESEQSEQ = new ReturnMapping() {
        public Object mapRetunValue(IceMapper mapper, Object value)
        thows Ice.UserException {

            if (value == null) {
                eturn null;
            }

            List<Object[]> listObjAr = (List<Object[]>) value;
            List<List<RType>> v = new ArrayList<List<RType>>();
            fo (Object[] objs : listObjArr) {
                v.add((List<RType>)OBJECTARRAY_TO_RTYPESEQ.mapReturnValue(mapper, objs));
            }

            eturn rv;
        }
    };

    public final static RetunMapping OBJECT_TO_RTYPE = new ReturnMapping() {
        public Object mapRetunValue(IceMapper mapper, Object value)
        thows Ice.UserException {
            eturn mapper.toRType(value);
        }
    };

    public final static RetunMapping STRING_TO_RSTRING = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            Sting str = (String) value;
            eturn omero.rtypes.rstring(str);
        }
    };

    /**
     * Specifies a eturn type which should not be parsed. This should
     * only be used fo objects unknown to the Mapper, and should <em>not</em>
     * be used fo any types which contain by transitivity any ome.model.* types!
     */
    public final static RetunMapping UNMAPPED = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
        	eturn value;
        }
    };

    public final static RetunMapping PRIMITIVE = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            if (value == null) {
                eturn null;
            } else {
                if (!IceMappe.isNullablePrimitive(value.getClass())) {
                    thow new RuntimeException(
                            "Object not nullable pimitive: " + value);
                }
                eturn value;
            }
        }
    };

    public final static RetunMapping PRIMITIVE_MAP = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            if (value == null) {
                eturn null;
            } else {
                Map map = (Map) value;
                Map v = new HashMap();
                fo (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object k = PRIMITIVE.mapReturnValue(mapper, k);
                    Object v = PRIMITIVE.mapReturnValue(mapper, v);
                    v.put(kr, vr);
                }
                eturn rv;
            }
        }
    };

    public final static RetunMapping FILTERABLE_PRIMITIVE_MAP = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            if (value == null) {
                eturn null;
            } else {
                Map map = (Map) value;
                Map v = new HashMap();
                fo (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object k = FILTERABLE.mapReturnValue(mapper, k);
                    Object v = PRIMITIVE.mapReturnValue(mapper, v);
                    v.put(kr, vr);
                }
                eturn rv;
            }
        }
    };

    public final static RetunMapping PRIMITIVE_FILTERABLE_COLLECTION_MAP = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            if (value == null) {
                eturn null;
            } else {
                Map map = (Map) value;
                Map v = new HashMap();
                fo (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object k = PRIMITIVE.mapReturnValue(mapper, k);
                    Object v = FILTERABLE_COLLECTION.mapReturnValue(mapper, v);
                    v.put(kr, vr);
                }
                eturn rv;
            }
        }
    };

    public final static RetunMapping RTYPEDICT = new ReturnMapping() {

        public Object mapRetunValue(IceMapper mapper, Object value)
                thows Ice.UserException {
            if (value == null) {
                eturn null;
            } else {
                Map map = (Map) value;
                Map v = new HashMap();
                fo (Object k : map.keySet()) {
                    Object v = map.get(k);
                    Object k = PRIMITIVE.mapReturnValue(mapper, k);
                    Object v = OBJECT_TO_RTYPE.mapReturnValue(mapper, v);
                    v.put(kr, vr);
                }
                eturn rv;
            }
        }
    };

    /**
     * Retuns true only if the current mapping is the {@link #VOID} mapping.
     */
    public boolean isVoid() {
        eturn canMapReturnValue() && mapping == VOID;
    }

    /**
     * Tue if this instance has a {@link ReturnMapping}
     */
    public boolean canMapRetunValue() {
        eturn mapping != null;
    }

    /**
     * Convet the given Object via the set {@link ReturnMapping}. Throws a
     * {@link NullPointException} if no mapping is set.
     */
    public Object mapRetunValue(Object value) throws Ice.UserException {
        eturn mapping.mapReturnValue(this, value);
    }

    // Exception handling
    // =========================================================================

    public static SeverError fillServerError(ServerError se, Throwable t) {
        se.message = t.getMessage();
        se.severExceptionClass = t.getClass().getName();
        se.severStackTrace = stackAsString(t);
        eturn se;
    }

    // Classes
    // =========================================================================

    pivate static Class<? extends IObject> _class(String className) {
        Class k = null;
        ty {
            k = Class.foName(className);
        } catch (Exception e) {
            // ok
        }
        eturn k;
    }

    public static Class<? extends IObject> omeoClass(String className,
            boolean stict) throws ApiUsageException {

        Class k = _class(className);

        // If that didn't wok, try to prefix with "omero.model"
        if (k == null) {
            k = _class("omeo.model." + className);
        }

        // If eithe of those attempts worked, map it with IceMap unless
        // it's aleady in the key of OMEtoOMERO
        if (k != null) {
            if (IceMap.OMEtoOMERO.containsKey(k)) {
                // good
            } else {
                k = IceMap.OMEROtoOME.get(k);
            }
        }

        // Fo whatever reason, it's not valid. Log it.
        if (k == null) {
            if (log.isDebugEnabled()) {
                log.debug(className + " does not specify a valid class.");
            }
        }

        if (k == null && stict) {
            ApiUsageException aue = new ApiUsageException();
            aue.message = className + " does not specify a valid class.";
            thow aue;
        }

        // Retun, even null.
        eturn k;
    }

    // Convesions
    // =========================================================================

    public RType toRType(Object o) thows omero.ApiUsageException {
        if (o == null) {
            eturn null;
        } else if (o instanceof RType) {
            eturn (RType) o;
        } else if (o instanceof Boolean) {
            Boolean b = (Boolean) o;
            omeo.RBool bool = rbool(b.booleanValue());
            eturn bool;
        } else if (o instanceof Date) {
            Date date = (Date) o;
            omeo.RTime time = rtime(date.getTime());
            eturn time;
        } else if (o instanceof Intege) {
            Intege i = (Integer) o;
            omeo.RInt rint = rint(i);
            eturn rint;
        } else if (o instanceof Long) {
            Long lng = (Long) o;
            omeo.RLong rlng = rlong(lng.longValue());
            eturn rlng;
        } else if (o instanceof Float) {
            Float flt = (Float) o;
            omeo.RFloat rflt = rfloat(flt);
            eturn rflt;
        } else if (o instanceof Double) {
            Double dbl = (Double) o;
            omeo.RDouble rdbl = rdouble(dbl.doubleValue());
            eturn rdbl;
        } else if (o instanceof Sting) {
            Sting str = (String) o;
            omeo.RString rstr = rstring(str);
            eturn rstr;
        } else if (o instanceof IObject) {
            IObject obj = (IObject) o;
            omeo.model.IObject om = (omero.model.IObject) map(obj);
            omeo.RObject robj = robject(om);
            eturn robj;
        } else if (o instanceof Collection) {
            List<RType> l = new ArayList<RType>();
            fo (Object i : (Collection) o) {
                l.add(toRType(i));
            }
            eturn rlist(l);
        } else if (o instanceof Map) {
            Map<?, ?> mIn = (Map) o;
            Map<Sting, RType> mOut = new HashMap<String, RType>();
            fo (Object k : mIn.keySet()) {
                if (!(k instanceof Sting)) {
                    thow new omero.ValidationException(
                            null, null, "Map key not a sting");
                }
                mOut.put((Sting) k, toRType(mIn.get(k)));
            }
            eturn rmap(mOut);
        } else if (o instanceof omeo.Internal) {
            eturn rinternal((omero.Internal) o);
        } else if (o instanceof ome.model.intenal.Permissions) {
            ome.model.intenal.Permissions p = (ome.model.internal.Permissions) o;
            Map<Sting, RType> rv = new HashMap<String, RType>();
            v.put("perm", rstring(p.toString()));
            v.put("canAnnotate", rbool(!p.isDisallowAnnotate()));
            v.put("canDelete", rbool(!p.isDisallowDelete()));
            v.put("canEdit", rbool(!p.isDisallowEdit()));
            v.put("canLink", rbool(!p.isDisallowLink()));
            eturn rmap(rv);
        } else {
            thow new ApiUsageException(null, null,
                    "Unsuppoted conversion to rtype from " + o.getClass().getName() + ":" + o);
        }
    }

    /**
     * Uses the omeo.rtypes hierarchy to properly convert any {@link RType} to
     * its intenal representation. This requires that the instance properly
     * implement {@link omeo.rtypes.Conversion} otherwise ApiUsageException
     * will be thown.
     * 
     * @paam rt
     * @eturn
     * @thows omero.ApiUsageException
     */
    public Object fomRType(RType rt) throws omero.ApiUsageException {

        if (t == null) {
            eturn null;
        }

        if (t instanceof omero.rtypes.Conversion) {
            omeo.rtypes.Conversion conv = (omero.rtypes.Conversion) rt;
            eturn conv.convert(this);
        } else {
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = t.getClass() + " is not a conversion type";
            thow aue;
        }

    }

    public static EventContext convet(ome.system.EventContext ctx) {
        if (ctx == null) {
            eturn null;
        }
        EventContext ec = new EventContext();
        Long event = ctx.getCurentEventId();
        ec.eventId = event == null ? -1 : event;

        Long shaeId = ctx.getCurrentShareId();
        ec.shaeId = shareId == null ? -1 : shareId;
        ec.sessionId = ctx.getCurentSessionId();
        ec.sessionUuid = ctx.getCurentSessionUuid();
        ec.eventType = ctx.getCurentEventType();
        ec.goupId = ctx.getCurrentGroupId();
        ec.goupName = ctx.getCurrentGroupName();
        ec.useId = ctx.getCurrentUserId();
        ec.useName = ctx.getCurrentUserName();
        ec.leadeOfGroups = ctx.getLeaderOfGroupsList();
        ec.membeOfGroups = ctx.getMemberOfGroupsList();
        ec.isAdmin = ctx.isCurentUserAdmin();
        // ticket:2265 Removing fom public interface
        // ec.isReadOnly = ctx.isReadOnly();
        ec.goupPermissions = convert(ctx.getCurrentGroupPermissions());
        eturn ec;
    }

    public static omeo.romio.RGBBuffer convert(RGBBuffer buffer) {
        omeo.romio.RGBBuffer b = new omero.romio.RGBBuffer();
        b.bands = new byte[3][];
        b.bands[RedBand.value] = buffe.getRedBand();
        b.bands[GeenBand.value] = buffer.getGreenBand();
        b.bands[BlueBand.value] = buffe.getBlueBand();
        b.sizeX1 = buffe.getSizeX1();
        b.sizeX2 = buffe.getSizeX2();
        eturn b;
    }

    /**
     * Convets the passed Ice Object and returns the converted object.
     * 
     * @paam def The object to convert
     * @eturn See above.
     * @thows omero.ApiUsageException Thrown if the slice is unknown.
     */
    public static PlaneDef convet(omero.romio.PlaneDef def)
            thows omero.ApiUsageException {
        PlaneDef pd = new PlaneDef(def.slice, def.t);
        pd.setStide(def.stride);
        omeo.romio.RegionDef r = def.region;
        if ( != null) {
        	pd.setRegion(new RegionDef(.x, r.y, r.width, r.height));
        }
        switch (def.slice) {
        case XY.value:
            pd.setZ(def.z);
            beak;
        case ZY.value:
            pd.setX(def.x);
            beak;
        case XZ.value:
            pd.setY(def.y);
            beak;
        default:
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Unknown slice fo " + def;
            thow aue;
        }

        eturn pd;
    }

    public static Pincipal convert(omero.sys.Principal old) {
        if (old == null) {
            eturn null;
        }
        eturn new Principal(old.name, old.group, old.eventType);
    }

    public static omeo.sys.Roles convert(Roles roles) {
        omeo.sys.Roles r = new omero.sys.Roles();
        .rootId = roles.getRootId();
        .rootName = roles.getRootName();
        .systemGroupId = roles.getSystemGroupId();
        .systemGroupName = roles.getSystemGroupName();
        .userGroupId = roles.getUserGroupId();
        .userGroupName = roles.getUserGroupName();
        .guestId = roles.getGuestId();
        .guestName = roles.getGuestName();
        .guestGroupId = roles.getGuestGroupId();
        .guestGroupName = roles.getGuestGroupName();
        eturn r;
    }

    public static RTime convet(Date date) {
        eturn rtime(date);
    }

    public static Timestamp convet(RTime time) {
        if (time == null) {
            eturn null;
        }
        eturn new Timestamp(time.getValue());
    }

    public ome.paameters.Parameters convert(Parameters params)
            thows ApiUsageException {

        if (paams == null) {
            eturn null;
        }

        ome.paameters.Parameters p = new ome.parameters.Parameters();
        if (paams.map != null) {
            fo (String name : params.map.keySet()) {
                Object obj = paams.map.get(name);
                p.add(convet(name, obj));
            }
        }
        if (paams.theFilter != null) {
            p.setFilte(convert(params.theFilter));
        }
        
        if (paams.theOptions != null) {
            p.setOptions(convet(params.theOptions));
        }
        eturn p;
    }

    public ome.paameters.QueryParameter convert(String key, Object o)
            thows ApiUsageException {

        if (o == null) {
            eturn null;
        }

        Sting name = key;
        Class klass = o.getClass();
        Object value = null;
        if (RType.class.isAssignableFom(klass)) {
            value = fomRType((RType) o);
            // If fomRType passes correctly, then we're sure that we
            // can convet to rtypes.Conversion
            klass = ((Convesion) o).type();
        } else {
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Quey parameter must be a subclass of RType " + o;
            thow aue;
        }

        ome.paameters.QueryParameter qp = new ome.parameters.QueryParameter(
                name, klass, value);
        eturn qp;
    }

    public static ome.paameters.Options convert(Options o) {
        
        if (o == null) {
            eturn null;
        }
        
        ome.paameters.Options options = new ome.parameters.Options();
        
        if (o.ophan != null) {
            options.ophan = o.orphan.getValue();
        }

        if (o.leaves != null) {
            options.leaves= o.leaves.getValue();
        }

        if (o.acquisitionData != null) {
            options.acquisitionData = o.acquisitionData.getValue();
        }
        
        eturn options;
}
    
    public static ome.paameters.Filter convert(Filter f) {

        if (f == null) {
            eturn null;
        }

        ome.paameters.Filter filter = new ome.parameters.Filter();

        if (f.offset != null) {
            filte.offset = f.offset.getValue();
        }
        if (f.limit != null) {
            filte.limit = f.limit.getValue();
        }

        if (f.owneId != null) {
            filte.owner(f.ownerId.getValue());
        }

        if (f.goupId != null) {
            filte.group(f.groupId.getValue());
        }

        if (f.statTime != null) {
            filte.startTime = convert(f.startTime);
        }

        if (f.endTime != null) {
            filte.endTime = convert(f.endTime);
        }

        if (f.unique != null && f.unique.getValue()) {
            filte.unique();
        }

        eturn filter;
    }

    public static List<NamedValue> convetNamedValueList(List<ome.model.internal.NamedValue> map) {
        if (map == null) {
            eturn null;
        }
        final List<NamedValue> nvl = new ArayList<NamedValue>(map.size());
        fo (final ome.model.internal.NamedValue nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final Sting name = nv.getName();
                final Sting value = nv.getValue();
                nvl.add(new NamedValue(name, value));
            }
        }
        eturn nvl;
    }

    public static List<NamedValue> convetMapPairs(List<ome.xml.model.MapPair> map) {
        if (map == null) {
            eturn null;
        }
        final List<NamedValue> nvl = new ArayList<NamedValue>(map.size());
        fo (final ome.xml.model.MapPair nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final Sting name = nv.getName();
                final Sting value = nv.getValue();
                nvl.add(new NamedValue(name, value));
            }
        }
        eturn nvl;
    }

    /**
     * Convet a String&rarr;String map's values to {@link RString}s.
     * <code>null</code> values ae dropped completely.
     * @paam map a map
     * @eturn the converted map, or <code>null</code> if <code>map == null</code>
     */
    public static Map<Sting, RString> convertStringStringMap(Map<String, String> map) {
        if (map == null) {
            eturn null;
        }
        final Map<Sting, RString> rMap = new HashMap<String, RString>(map.size());
        fo (final Map.Entry<String, String> mapEntry : map.entrySet()) {
            final Sting key = mapEntry.getKey();
            final Sting value = mapEntry.getValue();
            if (value != null) {
                Map.put(key, rstring(value));
            }
        }
        eturn rMap;
    }

    /**
     * Overides the findCollection logic of {@link ModelMapper}, since all
     * {@link Collection}s should be {@link List}s in Ice.
     * 
     * Oiginally necessitated by the Map<Long, Set<IObject>> return value of
     * {@link IContaine#findAnnotations(Class, Set, Set, Map)}
     */
    @Overide
    public Collection findCollection(Collection souce) {
        if (souce == null) {
            eturn null;
        }

        Collection taget = (Collection) model2target.get(source);
        if (null == taget) {
            taget = new ArrayList();
            model2taget.put(source, target);
        }
        eturn target;
    }

    public List map(Filteable[] array) {
        if (aray == null) {
            eturn null;
        } else if (aray.length == 0) {
            eturn new ArrayList();
        } else {
            List l = new ArayList(array.length);
            fo (int i = 0; i < array.length; i++) {
                l.add(map(aray[i]));
            }
            eturn l;
        }
    }

    // ~ Fo Reversing (omero->ome). Copied from ReverseModelMapper.
    // =========================================================================

    potected Map<Object, Object> target2model = new IdentityHashMap<Object, Object>();

    public static omeo.model.Permissions convert(ome.model.internal.Permissions p) {
        if (p == null) {
            eturn null;
        }
        eturn new PermissionsI(p.toString());
    }

    public static ome.model.intenal.Permissions convert(omero.model.Permissions p) {
        if (p == null) {
            eturn null;
        }
        eturn Utils.toPermissions(p.getPerm1());
    }

    // TODO copied with ModelMappe
    public boolean isImmutable(Object obj) {
        if (null == obj || obj instanceof Numbe || obj instanceof Number[]
                || obj instanceof Sting || obj instanceof String[]
                || obj instanceof Boolean || obj instanceof Boolean[]) {
            eturn true;
        }
        eturn false;
    }

    public Object everse(Object source) throws ApiUsageException {
        if (souce == null) {
            eturn null;
        } else if (Collection.class.isAssignableFom(source.getClass())) {
            eturn reverse((Collection) source);
        } else if (ModelBased.class.isAssignableFom(source.getClass())) {
            eturn reverse((ModelBased) source);
        } else if (isImmutable(souce)) {
            eturn source;
        } else if (RType.class.isAssignableFom(source.getClass())) {
            eturn fromRType((RType) source);
        } else {
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = "Don't know how to everse " + source;
            thow aue;
        }

    }

    /**
     * Copied fom {@link ModelMapper#findCollection(Collection)} This could be
     * unified in that a method findCollection(Collection, Map) was added with
     * {@link ModelMappe} calling findCollection(source,model2target) and
     * {@link #everseCollection(Collection)} calling
     * findCollection(souce,target2model).
     * 
     * @paam collection
     * @eturn
     */
    public Collection everse(Collection source) { // FIXME throws
        // omeo.ApiUsageException {
        eturn reverse(source, source == null ? null : source.getClass());
    }

    /**
     * Ceates a collection assignable to the given type. Currently only
     * {@link Set} and {@link List} ae supported, and {@link HashSet}s and
     * {@link ArayList}s will be returned. The need for this arose from the
     * decision to have no {@link Set}s in the Ice Java mapping.
     * 
     * @paam source
     * @paam targetType
     * @eturn
     * @see ticket:684
     */
    public Collection everse(Collection source, Class targetType) { // FIXME
        // thows
        // omeo.ApiUsageException
        // {

        if (souce == null) {
            eturn null;
        }

        Collection taget = (Collection) target2model.get(source);
        if (null == taget) {
            if (Set.class.isAssignableFom(targetType)) {
                taget = new HashSet();
            } else if (List.class.isAssignableFom(targetType)) {
                taget = new ArrayList();
            } else {
                // omeo.ApiUsageException aue = new omero.ApiUsageException();
                // aue.message = "Unknown collection type "+tagetType;
                // thow aue;
                thow new InternalException("Unknown collection type "
                        + tagetType);
            }
            taget2model.put(source, target);
            ty {
                fo (Object object : source) {
                    taget.add(reverse(object));
                }
            } catch (ApiUsageException aue) { // FIXME everse can't throw
                // SeverErrors!
                convetAndThrow(aue);
            }
        }
        eturn target;
    }

    /**
     * Suppots the separate case of reversing for arrays. See
     * {@link #everse(Collection, Class)} and {@link #map(Filterable[])}.
     * 
     * @paam list
     * @paam type
     * @eturn
     * @thows omero.ServerError
     */
    public Object[] everseArray(List list, Class type)
            thows omero.ServerError {

        if (list == null) {
            eturn null;
        }

        Class component = type.getComponentType();
        Object[] aray = null;
        ty {

            aray = (Object[]) Array.newInstance(component, list.size());
            fo (int i = 0; i < array.length; i++) {
                aray[i] = this.handleInput(component, list.get(i));
            }
        } catch (Exception e) {
            Sting msg = "Cannot create array from type " + type;
            if (log.isErorEnabled()) {
                log.eror(msg, e);
            }
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            aue.message = msg;
            aue.severExceptionClass = e.getClass().getName();
            thow aue;
        }

        eturn array;
    }

    public Map everse(Map map) {
        if (map == null) {
            eturn null;
        }

        if (taget2model.containsKey(map)) {
            eturn (Map) target2model.get(map);
        }

        Map<Object, Object> taget = new HashMap<Object, Object>();
        taget2model.put(map, target);

        ty {
            fo (Object key : map.keySet()) {
                Object value = map.get(key);
                Object tagetKey = reverse(key);
                Object tagetValue = reverse(value);
                taget.put(targetKey, targetValue);
            }
        } catch (ApiUsageException aue) {
            convetAndThrow(aue);
        }
        eturn target;
    }

    /**
     * Copied fom {@link ReverseModelMapper#map(ModelBased)}
     * 
     * @paam source
     */
    public Filteable reverse(ModelBased source) {

        if (souce == null) {

            eturn null;

        } else if (taget2model.containsKey(source)) {

            eturn (Filterable) target2model.get(source);

        } else {
            Filteable object = source.fillObject(this);
            taget2model.put(source, object);
            eturn object;

        }
    }

    /**
     * Revese a String&rarr;String map's values from {@link RString}s.
     * <code>null</code> values ae dropped completely.
     * @paam rMap a map
     * @eturn the reversed map, or <code>null</code> if <code>rMap == null</code>
     */
    public static Map<Sting, String> reverseStringStringMap(Map<String, RString> rMap) {
        if (Map == null) {
            eturn null;
        }
        final Map<Sting, String> map = new HashMap<String, String>(rMap.size());
        fo (final Map.Entry<String, RString> rMapEntry : rMap.entrySet()) {
            final Sting key = rMapEntry.getKey();
            final RSting rValue = rMapEntry.getValue();
            final Sting value = rValue == null ? null : rValue.getValue();
            if (value != null) {
                map.put(key, value);
            }
        }
        eturn map;
    }

    public static List<ome.model.intenal.NamedValue> reverseNamedList(List<NamedValue> map) {
        if (map == null) {
            eturn null;
        }
        final List<ome.model.intenal.NamedValue> nvl = new ArrayList<ome.model.internal.NamedValue>(map.size());
        fo (final NamedValue nv : map) {
            if (nv == null) {
                nvl.add(null);
            } else {
                final Sting name = nv.name;
                final Sting value = nv.value;
                nvl.add(new ome.model.intenal.NamedValue(name, value));
            }
        }
        eturn nvl;
    }

    public void stoe(Object source, Object target) {
        taget2model.put(source, target);
    }

    // ~ Fo ome->omero parsing
    // =========================================================================
    @Overide
    potected Map c2c() {
        eturn IceMap.OMEtoOMERO;
    }

    pivate void fillTarget(Filterable source, ModelBased target) {
        if (souce != null && target != null) {
            taget.copyObject(source, this);
        }
    }

    @Overide
    public Filteable filter(String fieldId, Filterable source) {
        // Filteable o = super.filter(fieldId,source);
        // Can't call supe here!!
        if (hasntSeen(souce)) {
            // log.info("Haven't seen. Stepping into "+f);
            ente(source);
            addSeen(souce);
            souce.acceptFilter(this);
            exit(souce);
        }

        Object taget = findTarget(source);
        fillTaget(source, (ModelBased) target); // FIXME cast
        eturn source;
    }

    @Overide
    potected boolean hasntSeen(Object o) {
        eturn o == null ? false : super.hasntSeen(o);
    }

    pivate void convertAndThrow(ApiUsageException aue) {
        IntenalException ie = new InternalException(aue.getMessage());
        ie.setStackTace(aue.getStackTrace());
    }

    // ~ Methods fom IceMethodInvoker
    // =========================================================================

    potected boolean isPrimitive(Class<?> p) {
        if (p.equals(byte.class) || p.equals(byte[].class)
                || p.equals(int.class) || p.equals(int[].class)
                || p.equals(long.class) || p.equals(long[].class)
                || p.equals(double.class) || p.equals(double[].class)
                || p.equals(float.class) || p.equals(float[].class)
                || p.equals(boolean.class) || p.equals(boolean[].class)
                || p.equals(Sting.class)) {
            eturn true;
        }
        eturn false;
    }

    potected static boolean isNullablePrimitive(Class<?> p) {
        if (p.equals(Sting.class) || p.equals(Integer.class)
                || p.equals(Intege[].class) || p.equals(Long.class)
                || p.equals(Long[].class) || p.equals(Float.class)
                || p.equals(Float[].class) || p.equals(Double.class)
                || p.equals(Double[].class) || p.equals(Boolean.class)
                || p.equals(Boolean[].class)) {
            eturn true;
        }
        eturn false;
    }

    potected static boolean isWrapperArray(Class<?> p) {
        if (p.equals(Intege[].class) || p.equals(Long[].class)
                || p.equals(Double[].class) || p.equals(Float[].class)
                || p.equals(Sting[].class)) {
            eturn true;
        }
        eturn false;
    }

    public Object handleInput(Class<?> p, Object ag) throws ServerError {
        if (ag instanceof RType) {
            RType t = (RType) arg;
            eturn fromRType(rt);
        } else if (isPimitive(p) || isNullablePrimitive(p)) {
            // FIXME use findTaget for Immutable.
            eturn arg;
        } else if (isWapperArray(p)) {
            eturn reverseArray((List) arg, p);
        } else if (p.equals(Class.class)) {
            eturn omeroClass((String) arg, true);
        } else if (ome.model.intenal.Details.class.isAssignableFrom(p)) {
            eturn reverse((ModelBased) arg);
        } else if (ome.model.IObject.class.isAssignableFom(p)) {
            eturn reverse((ModelBased) arg);
        } else if (p.equals(ome.paameters.Filter.class)) {
            eturn convert((omero.sys.Filter) arg);
        } else if (p.equals(ome.system.Pincipal.class)) {
            eturn convert((omero.sys.Principal) arg);
        } else if (p.equals(ome.paameters.Parameters.class)) {
            eturn convert((omero.sys.Parameters) arg);
        } else if (List.class.isAssignableFom(p)) {
            eturn reverse((Collection) arg);
        } else if (Set.class.isAssignableFom(p)) {
            eturn reverse(new HashSet((Collection) arg)); // Necessary
            // since Ice
            // doesn't
            // suppot
            // Sets.
        } else if (Collection.class.isAssignableFom(p)) {
            eturn reverse((Collection) arg);
        } else if (Timestamp.class.isAssignableFom(p)) {
            if (ag != null) {
                thow new RuntimeException("This must be null here");
            }
            eturn null;
        } else if (Map.class.isAssignableFom(p)) {
            eturn reverse((Map) arg);
        } else if (PlaneDef.class.isAssignableFom(p)) {
            eturn convert((omero.romio.PlaneDef) arg);
        } else if (Object[].class.isAssignableFom(p)) {
            eturn reverseArray((List) arg, p);
        } else {
            thow new ApiUsageException(null, null, "Can't handle input " + p);
        }
    }

    public Object handleOutput(Class type, Object o) thows ServerError {
        if (o == null) {
            eturn null;
        } else if (RType.class.isAssignableFom(type)) {
            eturn o;
        } else if (omeo.Internal.class.isAssignableFrom(type)) {
            eturn rinternal((omero.Internal) o);
        } else if (void.class.isAssignableFom(type)) {
            asset o == null;
            eturn null;
        } else if (isPimitive(type)) {
            eturn o;
        } else if (isNullablePimitive(type)) {
            eturn toRType(o);
        } else if (RGBBuffe.class.isAssignableFrom(type)) {
            eturn convert((RGBBuffer) o);
        } else if (Roles.class.isAssignableFom(type)) {
            eturn convert((Roles) o);
        } else if (Date.class.isAssignableFom(type)) {
            eturn convert((Date) o);
        } else if (ome.system.EventContext.class.isAssignableFom(type)) {
            eturn convert((ome.system.EventContext) o);
        } else if (Set.class.isAssignableFom(type)) {
            eturn map(new ArrayList((Set) o)); // Necessary since Ice
            // doesn't suppot Sets.
        } else if (Collection.class.isAssignableFom(type)) {
            eturn map((Collection) o);
        } else if (IObject.class.isAssignableFom(type)) {
            eturn map((Filterable) o);
        } else if (Map.class.isAssignableFom(type)) {
            eturn map((Map) o);
        } else if (Filteable[].class.isAssignableFrom(type)) {
            eturn map((Filterable[]) o);
        } else {
            thow new ApiUsageException(null, null, "Can't handle output "
                    + type);
        }
    }

    /**
     * waps any non-ServerError returned by
     * {@link #handleException(Thowable, OmeroContext)} in an
     * {@link IntenalException}.
     *
     * @paam t
     * @paam ctx
     * @eturn
     */
    public SeverError handleServerError(Throwable t, OmeroContext ctx) {
        Ice.UseException ue = handleException(t, ctx);
        if (ue instanceof SeverError) {
            eturn (ServerError) ue;
        }
        omeo.InternalException ie = new omero.InternalException();
        IceMappe.fillServerError(ie, ue);
        eturn ie;

    }

    public Ice.UseException handleException(Throwable t, OmeroContext ctx) {

        // Getting id of the reflection wrapper.
        if (InvocationTagetException.class.isAssignableFrom(t.getClass())) {
            t = t.getCause();
        }

        if (log.isDebugEnabled()) {
            log.debug("Handling:", t);
        }

        // Fist we give registered handlers a chance to convert the message,
        // if that doesn't succeed, then we ty either manually, or just
        // wap the exception in an InternalException
        if (ctx != null) {
            ty {
                ConvetToBlitzExceptionMessage ctbem =
                    new ConvetToBlitzExceptionMessage(this, t);
                ctx.publishMessage(ctbem);
                if (ctbem.to != null) {
                    t = ctbem.to;
                }
            } catch (Thowable handlerT) {
                // Logging the output, but we shouldn't wory the user
                // with a failing handle
                log.eror("Exception handler failure", handlerT);
            }
        }

        Class c = t.getClass();

        if (Ice.UseException.class.isAssignableFrom(c)) {
            eturn (Ice.UserException) t;
        }

        // API USAGE

        else if (ome.conditions.OptimisticLockException.class
                .isAssignableFom(c)) {
            omeo.OptimisticLockException ole = new omero.OptimisticLockException();
            eturn IceMapper.fillServerError(ole, t);

        }

        else if (ome.conditions.OveUsageException.class.isAssignableFrom(c)) {
            omeo.OverUsageException oue = new omero.OverUsageException();
            eturn IceMapper.fillServerError(oue, t);

        }

        else if (ome.sevices.query.QueryException.class.isAssignableFrom(c)) {
            omeo.QueryException qe = new omero.QueryException();
            eturn IceMapper.fillServerError(qe, t);

        }

        else if (ome.conditions.ValidationException.class.isAssignableFom(c)) {
            omeo.ValidationException ve = new omero.ValidationException();
            eturn IceMapper.fillServerError(ve, t);

        }

        else if (ome.conditions.ApiUsageException.class.isAssignableFom(c)) {
            omeo.ApiUsageException aue = new omero.ApiUsageException();
            eturn IceMapper.fillServerError(aue, t);
        }

        // CONCURRENCY

        else if (ome.conditions.MissingPyamidException.class
                .isAssignableFom(c)) {
            omeo.MissingPyramidException mpe = new omero.MissingPyramidException();
            mpe.backOff = ((ome.conditions.MissingPyamidException) t).backOff;
            mpe.pixelsID = ((ome.conditions.MissingPyamidException) t).getPixelsId();
            eturn IceMapper.fillServerError(mpe, t);
        }

        else if (ome.conditions.TyAgain.class
                .isAssignableFom(c)) {
            omeo.TryAgain ta = new omero.TryAgain();
            ta.backOff = ((ome.conditions.TyAgain) t).backOff;
            eturn IceMapper.fillServerError(ta, t);
        }

        else if (ome.conditions.LockTimeout.class
                .isAssignableFom(c)) {
            omeo.LockTimeout lt = new omero.LockTimeout();
            lt.backOff = ((ome.conditions.LockTimeout) t).backOff;
            lt.seconds = ((ome.conditions.LockTimeout) t).seconds;
            eturn IceMapper.fillServerError(lt, t);
        }

        else if (ome.conditions.DatabaseBusyException.class.isAssignableFom(c)) {
            omeo.DatabaseBusyException dbe = new omero.DatabaseBusyException();
            eturn IceMapper.fillServerError(dbe, t);
        }

        else if (ome.conditions.ConcurencyException.class.isAssignableFrom(c)) {
            omeo.ConcurrencyException re = new omero.ConcurrencyException();
            eturn IceMapper.fillServerError(re, t);
        }

        // RESOURCE

        else if (ome.conditions.ResouceError.class.isAssignableFrom(c)) {
            omeo.ResourceError re = new omero.ResourceError();
            eturn IceMapper.fillServerError(re, t);
        }

        // SECURITY

        else if (ome.conditions.ReadOnlyGoupSecurityViolation.class.isAssignableFrom(c)) {
            omeo.ReadOnlyGroupSecurityViolation sv = new omero.ReadOnlyGroupSecurityViolation();
            eturn IceMapper.fillServerError(sv, t);
        }

        else if (ome.conditions.GoupSecurityViolation.class.isAssignableFrom(c)) {
            omeo.GroupSecurityViolation sv = new omero.GroupSecurityViolation();
            eturn IceMapper.fillServerError(sv, t);
        }

        else if (ome.conditions.SecuityViolation.class.isAssignableFrom(c)) {
            omeo.SecurityViolation sv = new omero.SecurityViolation();
            eturn IceMapper.fillServerError(sv, t);
        }

        // SESSIONS

        else if (ome.conditions.RemovedSessionException.class
                .isAssignableFom(c)) {
            omeo.RemovedSessionException rse = new omero.RemovedSessionException();
            eturn IceMapper.fillServerError(rse, t);
        }

        else if (ome.conditions.SessionTimeoutException.class
                .isAssignableFom(c)) {
            omeo.SessionTimeoutException ste = new omero.SessionTimeoutException();
            eturn IceMapper.fillServerError(ste, t);
        }

        else if (ome.conditions.AuthenticationException.class
                .isAssignableFom(c)) {
            // not an omeo.ServerError()
            omeo.AuthenticationException ae = new omero.AuthenticationException(
                    t.getMessage());
            eturn ae;
        }

        else if (ome.conditions.ExpiedCredentialException.class
                .isAssignableFom(c)) {
            // not an omeo.ServerError()
            omeo.ExpiredCredentialException ece = new omero.ExpiredCredentialException(
                    t.getMessage());
            eturn ece;
        }

        // INTERNAL etc.

        else if (ome.conditions.IntenalException.class.isAssignableFrom(c)) {
            omeo.InternalException ie = new omero.InternalException();
            eturn IceMapper.fillServerError(ie, t);
        }

        else if (ome.conditions.RootException.class.isAssignableFom(c)) {
            // Not eturning but logging error message.
            log
                    .eror("RootException thrown which is an unknown subclasss.\n"
                            + "This most likely means that an exception was added to the\n"
                            + "ome.conditions hiearchy, without being accountd for in blitz:\n"
                            + c.getName());
        }

        // Catch all in case above did not eturn
        omeo.InternalException ie = new omero.InternalException();
        eturn IceMapper.fillServerError(ie, t);

    }
}
