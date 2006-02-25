package ome.services.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.builders.PojoOptions;

public class PojosLoadHierarchyQueryDefinition extends Query
{
    
    public PojosLoadHierarchyQueryDefinition(QueryParameter... parameters)
    {
        super(parameters);
    }
    
    protected void defineParameters(){
        defs = new QueryParameterDef[]{
                new QueryParameterDef(QP.CLASS,Class.class,false),
                new QueryParameterDef(QP.IDS,Collection.class,false),
                new QueryParameterDef(OWNER_ID,Long.class,true),
                new QueryParameterDef(QP.OPTIONS,Map.class,true)
        };
    }

    @Override
    protected Object runQuery(Session session) throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(QP.OPTIONS));
        
        Criteria c = session.createCriteria((Class)value(QP.CLASS));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in("id",(Collection) value(QP.IDS)));

        int depth = po.isLeaves() ? 2 : 1; 
        fetchChildren(c,Project.class,depth); 
      
        return c.list();
    }


    protected boolean projectOwnerFilterAlreadyEnabled = false;
    
    @Override
    protected void enableFilters(Session session)
    {
        if (session.getEnabledFilter(Project.OWNER_FILTER) != null) 
            projectOwnerFilterAlreadyEnabled = true;

        if (check(OWNER_ID)) {
            
            session.enableFilter(Project.OWNER_FILTER)
            .setParameter(OWNER_ID,value(OWNER_ID));
            
        }
    }

    @Override
    protected void disableFilters(Session session)
    {
        if (!projectOwnerFilterAlreadyEnabled)
            session.disableFilter(Project.OWNER_FILTER);
    }

}