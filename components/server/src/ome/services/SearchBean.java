/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.services.search.QueryBuilder;
import ome.services.search.QueryTemplate;
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

    private final List<QueryTemplate> templates = Collections
            .synchronizedList(new ArrayList<QueryTemplate>());

    private final QueryBuilder builder = new QueryBuilder();

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

    @Transactional
    @RolesAllowed("user")
    public void ByAnnotatedWith(Annotation example) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void ByFullText(String query) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void ByHqlQuery(String query, Parameters p) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void BySomeMustNone(String[] some, String[] must, String[] none) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void ByTags(String[] tags) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public int activeQueries() {
        return templates.size();
    }

    @Transactional
    @RolesAllowed("user")
    public void addAnnotation(IAnnotated annotated, Annotation annotation) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void addTagToGroup(String tag, String tagGroup) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public List<String> allGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public List<String> allTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public void allTypes() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void allowProjections() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public Map<String, List<Annotation>> annotations(Class<IObject> klass,
            long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public void clearQueries() {
        templates.clear();
    }

    @Transactional
    @RolesAllowed("user")
    public List<Annotation> currentMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAlso(Map<T, String> fetches) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void fetchAnnotations(Class<T>... classes) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public int getBatchSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Transactional
    @RolesAllowed("user")
    public List<String> groupsForTag(String tag) {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isCaseSensitive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public boolean isMergedBatched() {
        // TODO Auto-generated method stub
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public void lastresultsAsWorkingGroup() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public IObject next() throws ApiUsageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBetween(Timestamp start, Timestamp stop) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void onlyAnnotatedBy(Details d) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public <A extends Annotation> void onlyAnnotatedWith(Class<A>... classes) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void onlyCreatedBetween(Timestamp start, Timestamp stop) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void onlyOwnedBy(Details d) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> void onlyTypes(Class<T>... classes) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Cannot remove via ome.api.Search");
    }

    @Transactional
    @RolesAllowed("user")
    public void removeAnnotation(IAnnotated annotated, Annotation annotation) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void removeTagFromGroup(String tag, String tagGroup) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void resetDefaults() {
        builder.setTemplate(new QueryTemplate());
    }

    @Transactional
    @RolesAllowed("user")
    public <T extends IObject> Map<T, List<Annotation>> results() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public void setBatchSize(int size) {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void setCaseSentivice() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void setIdOnly() {
        // TODO Auto-generated method stub

    }

    @Transactional
    @RolesAllowed("user")
    public void setMergedBatches(boolean merge) {
        builder.getTemplate().merged = true;
    }

    @Transactional
    @RolesAllowed("user")
    public boolean tag(IAnnotated annotated, String tag, boolean reuse) {
        // TODO Auto-generated method stub
        return false;
    }

    @Transactional
    @RolesAllowed("user")
    public Map<String, String> tags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    @RolesAllowed("user")
    public List<String> tagsInGroup(String tagGroup) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends IObject> void fetchAlso(String... fetches) {
        // TODO Auto-generated method stub

    }

    public void isReturnUnloaded() {
        // TODO Auto-generated method stub

    }

    public void isUseProjections() {
        // TODO Auto-generated method stub

    }

    public <T extends IObject> T next() {
        // TODO Auto-generated method stub
        return null;
    }

    public void onlyModifiedBetween(Timestamp start, Timestamp stop) {
        // TODO Auto-generated method stub

    }

    public <T extends IObject> void onlyType(Class<T> klass) {
        // TODO Auto-generated method stub

    }

    public void setCaseSentivice(boolean caseSensitive) {
        // TODO Auto-generated method stub

    }

    public void setReturnUnloaded(boolean returnUnloaded) {
        // TODO Auto-generated method stub

    }

    public void setUseProjections(boolean useProjections) {
        // TODO Auto-generated method stub

    }

}
