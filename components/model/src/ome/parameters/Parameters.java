/*
 * ome.parameters.Parameters
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.parameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.ApiUsageException;

/**
 * container object for {@link QueryParameter} and {@link Filter} instances.
 * 
 * The public Strings available here are used throughout this class and should
 * also be used in query strings as named parameteres. For example, the field
 * {@link Parameters#ID} has the value "id", and a query which would like to use
 * the {@link Parameters#addId(Long)} method, should define a named parameter of
 * the form ":id".
 * 
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since 3.0-M2
 */
public class Parameters implements Serializable {

    /**
     * named parameter "id". Used in query strings as ":id"
     */
    public final static String ID = "id";

    /**
     * named parameter "ids". Used in query strings as ":ids"
     */
    public final static String IDS = "ids";

    /**
     * named parameter "class". Used in query strings as ":class"
     */
    public final static String CLASS = "class";

    /**
     * named parameter "algorithm". Used in query strings as ":algorithm"
     */
    public final static String ALGORITHM = "algorithm";

    /**
     * named parameter "ownerId". Used in query strings as ":ownerId"
     */
    public final static String OWNER_ID = "ownerId";

    /**
     * named parameter "groupId". Used in query strings as ":groupId"
     */
    public final static String GROUP_ID = "groupId";

    private Filter filter;
    
    private Options options;

    /**
     * storage for the {@link QueryParameter query parameters}. For
     * serialization, {@link #writeObject(ObjectOutputStream)} and
     * {@link #readObject(ObjectInputStream)} have been over-written for a more
     * compact form.
     */
    private transient Map queryParameters = new HashMap();

    /**
     * default constructor. {@link Filter} is left null.
     * {@link QueryParameter queryParameters} collection is initialized to empty
     * {@link Collection}
     */
    public Parameters() {
    }

    /**
     * Filter constructor. Allows for the simple specification of "unique"
     * results. <code>new Parameters( new Filter().unique() ); </code> Filter
     * can be null since this is the default behavior anyway.
     */
    public Parameters(Filter filter) {
        this.filter = filter;
    }

    /**
     * copy constructor. {@link Filter} is taken from old instance and
     * {@link QueryParameter queryParameters} are merged.
     * 
     * @param old
     */
    public Parameters(Parameters old) {
        if (old == null) {
            return;
        }
        addAll(old);
    }

    /**
     * copy constructor. Merges {@link QueryParameter}s.
     */
    public Parameters(QueryParameter[] queryParameters) {
        addAll(queryParameters);
    }

    // ~ READ METHODS
    // =========================================================================

    /**
     * copies all QueryParameters to an array. Changes to this array do not
     * effect the internal QueryParameters.
     * 
     * @return array of QueryParameter.
     */
    public QueryParameter[] queryParameters() {
        return (QueryParameter[]) queryParameters.values().toArray(
                new QueryParameter[queryParameters.size()]);
    }

    /**
     * lookup a QueryParameter by name.
     */
    public QueryParameter get(String name) {
        return (QueryParameter) queryParameters.get(name);
    }

    /**
     * the Set of all names which would would return a non-null value from
     * {@link Parameters#get(String)}
     * 
     * @return a Set of Strings.
     */
    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        return new HashSet(queryParameters.keySet());
    }

    // ~ WRITE METHODS
    // =========================================================================

    public Parameters setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }
    
    public Parameters setOptions(Options options) {
        this.options = options;
        return this;
    }

    public Parameters add(QueryParameter parameter) {
        if (parameter == null) {
            throw new ApiUsageException("Parameter argument may not be null.");
        }

        queryParameters.put(parameter.name, parameter);
        return this;
    }

    /**
     * adds all the information from the passed in Parameters instance to this
     * instance. All {@link QueryParameter}s are added, and the {@link Filter}
     * instance is added <em>if</em> the current
     * 
     * @param old
     *            Non-null Parameters instance.
     * @return this
     */
    public Parameters addAll(Parameters old) {
        if (old == null) {
            throw new ApiUsageException("Parameters argument may not be null.");
        }

        if (old.filter != null) {
            if (filter != null) {
                throw new ApiUsageException(
                        "Two filters not allowed during copy constructor.");
            } else {
                filter = old.filter;
            }
        }
        
        if (old.options != null) {
            if (options != null) {
                throw new ApiUsageException(
                        "Two options not allowed during copy constructor.");
            } else {
                options = old.options;
            }
        }

        return addAll(old.queryParameters());

    }

    /**
     * adds all the information from the passed in Parameters instance to this
     * instance. All {@link QueryParameter}s are added, and the {@link Filter}
     * instance is added <em>if</em> the current
     * 
     * @param queryParameters
     *            Non-null array of QueryParameters.
     * @return this
     */
    public Parameters addAll(QueryParameter[] queryParameters) {

        if (queryParameters == null) {
            throw new ApiUsageException(
                    "Array of QueryParameters may not be null.");
        }

        for (int i = 0; i < queryParameters.length; i++) {
            add(queryParameters[i]);
        }

        return this;

    }

    public Parameters addClass(Class klass) {
        addClass(CLASS, klass);
        return this;
    }

    public Parameters addClass(String name, Class value) {
        add(new QueryParameter(name, Class.class, value));
        return this;
    }

    public Parameters addBoolean(String name, Boolean value) {
        add(new QueryParameter(name, Boolean.class, value));
        return this;
    }

    public Parameters addInteger(String name, Integer value) {
        add(new QueryParameter(name, Integer.class, value));
        return this;
    }

    public Parameters addLong(String name, Long value) {
        add(new QueryParameter(name, Long.class, value));
        return this;
    }

    public Parameters addSet(String name, Set value) {
        add(new QueryParameter(name, Set.class, value));
        return this;
    }

    public Parameters addList(String name, List value) {
        add(new QueryParameter(name, List.class, value));
        return this;
    }

    public Parameters addMap(String name, Map value) {
        add(new QueryParameter(name, Map.class, value));
        return this;
    }

    public Parameters addString(String name, String value) {
        add(new QueryParameter(name, String.class, value));
        return this;
    }

    public Parameters addId(Long id) {
        add(new QueryParameter(ID, Long.class, id));
        return this;
    }

    public Parameters addIds(Collection ids) {
        add(new QueryParameter(IDS, Collection.class, ids));
        return this;
    }

    public Parameters addAlgorithm(String algo) {
        addString(ALGORITHM, algo);
        return this;
    }
    
    // ~ Filter delegation methods
    // =========================================================================
    
    public Parameters exp(long id) {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.owner(id);
        return this;
    }
    
    public Parameters allExps() {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.owner(-1);
        return this;
    }
    
    public long owner() {
        if (this.filter != null) {
            return this.filter.owner();
        }
        return -1;
    }
    
    public boolean isExperimenter() {
        if (this.filter != null) {
            return this.filter.owner() != -1L;
        }
        return false;
    }
    
    /**
     * Fulfills the old PojoOptions requirement for returning null if no
     * owner set.
     */
    public Long getExperimenter() {
        long o = owner();
        if (o == -1) {
            return null;
        }
        return Long.valueOf(o);
    }
    
    public Parameters grp(long id) {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.group(id);
        return this;
    }
    
    public long group() {
        if (this.filter != null) {
            return this.filter.group();
        }
        return -1;
    }

    /**
     * Fulfills the old PojoOptions requirement for returning null if no
     * owner set.
     */
    public Long getGroup() {
        long g = group();
        if (g == -1) {
            return null;
        }
        return Long.valueOf(g);
    }
    
    public boolean isGroup() {
        if (this.filter != null) {
            return this.filter.group() != -1L;
        }
        return false;
    }
    
    public Parameters startTime(Timestamp timestamp) {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.startTime = timestamp;
        return this;
    }
    
    public Timestamp getStartTime() {
        if (this.filter != null) {
            return this.filter.startTime;
        }
        return null;
    }
    
    public Parameters endTime(Timestamp timestamp) {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.endTime = timestamp;
        return this;
    }
    
    public Timestamp getEndTime() {
        if (this.filter != null) {
            return this.filter.endTime;
        }
        return null;
    }
    
    public Parameters paginate(Integer offset, Integer limit) {
        if (this.filter == null) {
            this.filter = new Filter(); 
        }
        this.filter.limit = limit;
        this.filter.offset = offset;
        return this;
    }
    
    public Integer getLimit() {
        if (this.filter != null) {
            return this.filter.limit;
        }
        return null;
    }
    
    public Integer getOffset() {
        if (this.filter != null) {
            return this.filter.offset;
        }
        return null;
    }
    
    public boolean isPagination() {
        if (this.filter != null) {
            return this.filter.offset != null || this.filter.limit != null;
        }
        return false;
    }
    
    public Parameters page(Integer offset, Integer limit) {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.limit = limit;
        this.filter.offset = offset;
        return this;
    }

    public Parameters unique() {
        if (this.filter == null) {
            this.filter = new Filter();
        }
        this.filter.unique();
        return this;
    }
    
    public boolean isUnique() {
        if (this.filter != null) {
            return this.filter.isUnique();
        }
        return false;
    }
    

    // ~ Options delegation methods
    // =========================================================================

    public boolean isAcquisitionData() {
        if (this.options != null) {
            return this.options.acquisitionData;
        }
        return false;
    }

    public boolean isLeaves() {
        if (this.options != null) {
            return this.options.leaves;
        }
        return false;
    }
    
    public Parameters leaves() {
        if (this.options == null) {
            this.options = new Options();
        }
        this.options.leaves = true;
        return this;
    }
    
    public Parameters noLeaves() {
        if (this.options == null) {
            this.options = new Options();
        }
        this.options.leaves = false;
        return this;
    }
    
    public Parameters orphan() {
        if (this.options == null) {
            this.options = new Options();
        }
        this.options.orphan = true;
        return this;
    }
    
    public Parameters noOrphan() {
        if (this.options == null) {
            this.options = new Options();
        }
        this.options.orphan = false;
        return this;
    }
    
    public boolean isOrphan() {
        if (this.options != null) {
            return this.options.orphan;
        }
        return false;
    }

    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 6428983610525830551L;

    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();

        queryParameters = new HashMap();
        for (int i = 0; i < size; i++) {
            add((QueryParameter) s.readObject());
        }

    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        Set keySet = queryParameters.keySet();
        s.writeInt(keySet.size());

        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            s.writeObject(queryParameters.get(key));
        }

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PARAMS");
        if (filter != null) {
            sb.append(":");
            sb.append(filter);
        }
        if (options != null) {
            sb.append(":");
            sb.append(options);
        }
        if (queryParameters != null && queryParameters.size() > 0) {
            sb.append(":");
            for ( Object obj : queryParameters.values()) {
                QueryParameter qp = (QueryParameter) obj;
                sb.append(qp.name);
                sb.append("=");
                if (qp.value == null) {
                    sb.append(" ");
                    continue;
                }
                Class k = qp.value.getClass();
                if (Collection.class.isAssignableFrom(k)) {
                    sb.append(k.getSimpleName());
                    sb.append("(");
                    sb.append(((Collection) qp.value).size());
                    sb.append(")");
                } else if (Map.class.isAssignableFrom(k)) {
                    sb.append("map(");
                    sb.append(((Map) qp.value).size());
                    sb.append(")");
                } else {
                    sb.append(qp.value);
                }
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
}
