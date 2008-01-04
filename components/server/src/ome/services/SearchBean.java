/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.api.Search;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.services.search.AnnotatedWith;
import ome.services.search.FullText;
import ome.services.search.HqlQuery;
import ome.services.search.SearchAction;
import ome.services.search.SearchValues;
import ome.services.search.SomeMustNone;
import ome.services.search.Tags;
import ome.services.util.OmeroAroundInvoke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for submitting asynchronous tasks.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * 
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateful
@Remote(Search.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.Search")
@Local(Search.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.Search")
@Interceptors( { OmeroAroundInvoke.class })
@SecurityDomain("OmeroSecurity")
public class SearchBean extends AbstractStatefulBean implements Search {

    private final static long serialVersionUID = 59809384038000069L;

    /** The logger for this class. */
    private final static Log log = LogFactory.getLog(SearchBean.class);

    private final List<SearchAction> actions = Collections
            .synchronizedList(new ArrayList<SearchAction>());

    private final SearchValues values = new SearchValues();

    private List<IObject> lastResultsUnloaded;

    /** default constructor */
    public SearchBean() {
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return Search.class;
    }

    // Lifecycle methods
    // ===================================================

    /**
     * Configures a new or re-activated {@link SearchBean}. Currently, most
     * configuration is handled via field initializers or by default
     * serialization.
     */
    @PostConstruct
    @PostActivate
    public void create() {
        selfConfigure();
    }

    @PrePassivate
    @PreDestroy
    public void destroy() {
        // all state is passivated.
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @Remove
    public void close() {
    }

    // Interface methods ~
    // ============================================

    //
    // CREATE METHODS
    //

    @Transactional
    @RolesAllowed("user")
    public void ByAnnotatedWith(Annotation example) {
        SearchAction byAnnotatedWith;
        synchronized (values) {
            byAnnotatedWith = new AnnotatedWith(values, example);
        }
        actions.add(byAnnotatedWith);
    }

    @Transactional
    @RolesAllowed("user")
    public void ByFullText(String query) {
        SearchAction byFullText;
        synchronized (values) {
            byFullText = new FullText(values, query);
        }
        actions.add(byFullText);

    }

    @Transactional
    @RolesAllowed("user")
    public void ByHqlQuery(String query, Parameters p) {
        SearchAction byHqlQuery;
        synchronized (values) {
            byHqlQuery = new HqlQuery(values, query, p);
        }
        actions.add(byHqlQuery);
    }

    @Transactional
    @RolesAllowed("user")
    public void BySomeMustNone(String[] some, String[] must, String[] none) {
        SearchAction bySomeMustNone;
        synchronized (values) {
            bySomeMustNone = new SomeMustNone(values, some, must, none);
        }
        actions.add(bySomeMustNone);
    }

    @Transactional
    @RolesAllowed("user")
    public void ByTags(String[] tags) {
        SearchAction byTags;
        synchronized (values) {
            byTags = new Tags(values, tags);
        }
        actions.add(byTags);
    }

    @Transactional
    @RolesAllowed("user")
    public void ByGroupForTags(String group) {
        throw new UnsupportedOperationException();
    }

    @Transactional
    @RolesAllowed("user")
    public void ByTagForGroups(String tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Transactional
    @RolesAllowed("user")
    public void ByUUID(String[] uuids) {
        throw new UnsupportedOperationException();
    }

    //
    // FETCH METHODS
    //

    @Transactional
    @RolesAllowed("user")
    public boolean hasNext() {
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public IObject next() throws ApiUsageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public List<Annotation> currentMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> Map<T, List<Annotation>> results() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public void lastresultsAsWorkingGroup() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Cannot remove via ome.api.Search");
    }

    //
    // QUERY MANAGEMENT
    // 

    @Transactional
    @RolesAllowed("user")
    public int activeQueries() {
        return actions.size();
    }

    @Transactional
    @RolesAllowed("user")
    public void clearQueries() {
        actions.clear();
    }

    //
    // TEMPLATE STATE
    //

    @Transactional
    @RolesAllowed("user")
    public void resetDefaults() {
        synchronized (values) {
            values.copy(new SearchValues());
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAlso(Map<T, String> fetches) {
        synchronized (values) {
            throw new UnsupportedOperationException();
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAnnotations(Class<T>... classes) {
        synchronized (values) {
            throw new UnsupportedOperationException();
        }
    }

    @Transactional
    @RolesAllowed("user")
    public int getBatchSize() {
        synchronized (values) {
            return values.batchSize;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isCaseSensitive() {
        synchronized (values) {
            return values.caseSensitive;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isMergedBatches() {
        synchronized (values) {
            return values.mergedBatches;
        }

    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBetween(Timestamp start, Timestamp stop) {
        synchronized (values) {
            values.annotatedStart = SearchValues.copyTimestamp(start);
            values.annotatedStop = SearchValues.copyTimestamp(stop);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBy(Details d) {
        synchronized (values) {
            values.annotatedBy = SearchValues.copyDetails(d);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <A extends Annotation> void onlyAnnotatedWith(Class<A>... classes) {
        synchronized (values) {
            List<Class<?>> list = Arrays.<Class<?>> asList(classes);
            values.onlyAnnotations = SearchValues.copyList(list);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyCreatedBetween(Timestamp start, Timestamp stop) {
        synchronized (values) {
            values.createdStart = SearchValues.copyTimestamp(start);
            values.createdStop = SearchValues.copyTimestamp(stop);
            throw new IllegalArgumentException("What invariants");
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyOwnedBy(Details d) {
        synchronized (values) {
            values.ownedBy = SearchValues.copyDetails(d);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void allTypes() {
        // TODO Auto-generated method stub
    }

    @Transactional
    @RolesAllowed("user")
    @SuppressWarnings("all")
    public <T extends IObject> void onlyType(Class<T> klass) {
        onlyTypes(klass);
    }

    @Transactional
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public <T extends IObject> void onlyTypes(Class<T>... classes) {
        synchronized (values) {
            values.onlyTypes = new ArrayList();
            for (Class<T> k : classes) {
                values.onlyTypes.add(k);
            }
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setBatchSize(int size) {
        synchronized (values) {
            values.batchSize = size;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setIdOnly() {
        synchronized (values) {
            values.idOnly = true;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setMergedBatches(boolean merge) {
        synchronized (values) {
            values.mergedBatches = merge;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void fetchAlso(String... fetches) {
        synchronized (values) {
            values.fetches = Arrays.asList(fetches);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isReturnUnloaded() {
        synchronized (values) {
            return values.returnUnloaded;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isUseProjections() {
        synchronized (values) {
            return values.useProjections;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyModifiedBetween(Timestamp start, Timestamp stop) {
        synchronized (values) {
            values.modifiedStart = SearchValues.copyTimestamp(start);
            values.modifiedStop = SearchValues.copyTimestamp(stop);
            throw new RuntimeException("What checks need to be performed.");
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setCaseSentivice(boolean caseSensitive) {
        synchronized (values) {
            values.caseSensitive = caseSensitive;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setReturnUnloaded(boolean returnUnloaded) {
        synchronized (values) {
            values.returnUnloaded = returnUnloaded;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setUseProjections(boolean useProjections) {
        synchronized (values) {
            values.useProjections = useProjections;
        }
    }

    //
    // LOCAL API (mostly for testing)
    //

    public void addAction(SearchAction action) {
        synchronized (actions) {
            if (action == null) {
                throw new IllegalArgumentException("Action cannot be null");
            }
            actions.add(action);
        }
    }
}
