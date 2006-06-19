package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;
import ome.util.builders.PojoOptions;

public class PojosLoadHierarchyQueryDefinition 
    extends Query
{

    static Definitions defs = new Definitions(
            new CollectionQueryParameterDef(Parameters.IDS,true,Long.class),
            new OptionsQueryParameterDef(),
            new ClassQueryParameterDef());

    public PojosLoadHierarchyQueryDefinition(Parameters parameters)
    {
            super( defs, parameters );
    }

    @Override
    protected void buildQuery(Session session) 
    throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(OPTIONS));
        Class klass = (Class)value(CLASS);
        
        Criteria c = session.createCriteria( klass );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        
        // optional ids
        Collection ids = (Collection) value(IDS);
        if ( ids != null && ids.size() > 0)
            c.add(Restrictions.in("id",(Collection) value(IDS)));
        
        // fetch hierarchy
        int depth = po.isLeaves() ? Integer.MAX_VALUE : 1; 
        Hierarchy.fetchChildren(c,klass,depth); 
    
        setCriteria( c );
    }

    @Override
    protected void enableFilters(Session session)
    {
        ownerFilter(session, 
//              TODO this needs to be moved to Hierarchy.
                CategoryGroup.OWNER_FILTER, 
                Category.OWNER_FILTER,
                Project.OWNER_FILTER,
                Dataset.OWNER_FILTER
                );
    }

}
