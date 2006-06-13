package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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
        Criteria c = session.createCriteria((Class) value(CLASS));
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in("id",(Collection) value(IDS)));
        Hierarchy.fetchChildren(c,(Class) value(CLASS),Integer.MAX_VALUE);
        setCriteria( c );
    }

}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
