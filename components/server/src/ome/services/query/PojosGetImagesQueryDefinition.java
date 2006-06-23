package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;

public class PojosGetImagesQueryDefinition 
    extends AbstractClassIdsOptionsQuery
{

    public PojosGetImagesQueryDefinition(Parameters parameters)
    {
        super( parameters );
    }  

    @Override
    protected void buildQuery(Session session) 
    throws HibernateException, SQLException
    {
        Criteria c = session.createCriteria(Image.class);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.createCriteria("defaultPixels",LEFT_JOIN);
        
        // Add restrictions to the most distant criteria
        Criteria[] hy = 
            Hierarchy.fetchParents(c,(Class) value(CLASS),Integer.MAX_VALUE);
        hy[hy.length-1].add(Restrictions.in("id",(Collection) value(IDS)));
        setCriteria( c );
    }
    
    @Override
    protected void enableFilters(Session session)
    {
        ownerFilter(session, Image.OWNER_FILTER);
    }    

}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
