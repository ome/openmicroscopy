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
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.services.search.AnnotatedWith;
import ome.services.search.FullText;
import ome.services.search.HqlQuery;
import ome.services.search.QueryTemplate;
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
public abstract class SearchBean extends AbstractStatefulBean implements Search {

    private final static long serialVersionUID = 59809384038000069L;

    /** The logger for this class. */
    private final static Log log = LogFactory.getLog(SearchBean.class);

    private final List<QueryTemplate> queries = Collections
            .synchronizedList(new ArrayList<QueryTemplate>());

    private final QueryTemplate template = new QueryTemplate();

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
        QueryTemplate byAnnotatedWith = new AnnotatedWith(example);
        synchronized (template) {
            byAnnotatedWith.copy(template);
        }
        queries.add(byAnnotatedWith);
    }

    @Transactional
    @RolesAllowed("user")
    public void ByFullText(String query) {
        QueryTemplate byFullText = new FullText(query);
        synchronized (template) {
            byFullText.copy(template);
        }
        queries.add(byFullText);

    }

    @Transactional
    @RolesAllowed("user")
    public void ByHqlQuery(String query, Parameters p) {
        QueryTemplate byHqlQuery = new HqlQuery(query, p);
        synchronized (template) {
            byHqlQuery.copy(template);
        }
        queries.add(byHqlQuery);
    }

    @Transactional
    @RolesAllowed("user")
    public void BySomeMustNone(String[] some, String[] must, String[] none) {
        QueryTemplate bySomeMustNone = new SomeMustNone(some, must, none);
        synchronized (template) {
            bySomeMustNone.copy(template);
        }
        queries.add(bySomeMustNone);
    }

    @Transactional
    @RolesAllowed("user")
    public void ByTags(String[] tags) {
        QueryTemplate byTags = new Tags(tags);
        synchronized (template) {
            byTags.copy(template);
        }
        queries.add(byTags);
    }

    //
    // FETCH METHODS
    //

    @Transactional
    @RolesAllowed("user")
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public IObject next() {
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
        return queries.size();
    }

    @Transactional
    @RolesAllowed("user")
    public void clearQueries() {
        queries.clear();
    }

    //
    // TEMPLATE STATE
    //

    @Transactional
    @RolesAllowed("user")
    public void resetDefaults() {
        synchronized (template) {
            template.copy(new QueryTemplate());
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAlso(Map<T, String> fetches) {
        synchronized (template) {
            throw new UnsupportedOperationException();
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAnnotations(Class<T>... classes) {
        synchronized (template) {
            throw new UnsupportedOperationException();
        }
    }

    @Transactional
    @RolesAllowed("user")
    public int getBatchSize() {
        synchronized (template) {
            return template.batchSize;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isCaseSensitive() {
        synchronized (template) {
            return template.caseSensitive;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isMergedBatches() {
        synchronized (template) {
            return template.mergedBatches;
        }

    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBetween(Timestamp start, Timestamp stop) {
        synchronized (template) {
            template.annotatedStart = QueryTemplate.copyTimestamp(start);
            template.annotatedStop = QueryTemplate.copyTimestamp(stop);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBy(Details d) {
        synchronized (template) {
            template.annotatedBy = QueryTemplate.copyDetails(d);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public <A extends Annotation> void onlyAnnotatedWith(Class<A>... classes) {
        synchronized (template) {
            List<Class<?>> list = Arrays.<Class<?>> asList(classes);
            template.onlyAnnotations = QueryTemplate.copyList(list);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyCreatedBetween(Timestamp start, Timestamp stop) {
        synchronized (template) {
            template.createdStart = QueryTemplate.copyTimestamp(start);
            template.createdStop = QueryTemplate.copyTimestamp(stop);
            throw new IllegalArgumentException("What invariants");
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyOwnedBy(Details d) {
        synchronized (template) {
            template.ownedBy = QueryTemplate.copyDetails(d);
        }
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
        synchronized (template) {
            template.onlyTypes = new ArrayList();
            for (Class<T> k : classes) {
                template.onlyTypes.add(k);
            }
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setBatchSize(int size) {
        synchronized (template) {
            template.batchSize = size;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setIdOnly() {
        synchronized (template) {
            template.idOnly = true;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setMergedBatches(boolean merge) {
        synchronized (template) {
            template.mergedBatches = merge;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void fetchAlso(String... fetches) {
        synchronized (template) {
            template.fetches = Arrays.asList(fetches);
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isReturnUnloaded() {
        synchronized (template) {
            return template.returnUnloaded;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isUseProjections() {
        synchronized (template) {
            return template.useProjections;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyModifiedBetween(Timestamp start, Timestamp stop) {
        synchronized (template) {
            template.modifiedStart = QueryTemplate.copyTimestamp(start);
            template.modifiedStop = QueryTemplate.copyTimestamp(stop);
            throw new RuntimeException("What checks need to be performed.");
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setCaseSentivice(boolean caseSensitive) {
        synchronized (template) {
            template.caseSensitive = caseSensitive;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setReturnUnloaded(boolean returnUnloaded) {
        synchronized (template) {
            template.returnUnloaded = returnUnloaded;
        }
    }

    @Transactional
    @RolesAllowed("user")
    public void setUseProjections(boolean useProjections) {
        synchronized (template) {
            template.useProjections = useProjections;
        }
    }

}
