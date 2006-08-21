package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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
        
        Criteria pix = c.createCriteria("defaultPixels",LEFT_JOIN);
        pix.createCriteria("pixelsType",LEFT_JOIN);
        pix.createCriteria("pixelsDimensions",LEFT_JOIN);
        
        
        Class klass = (Class) value(CLASS);
        Collection ids = (Collection) value(IDS);
        
        // see https://trac.openmicroscopy.org.uk/omero/ticket/296
        if (Image.class.isAssignableFrom(klass))
        {
        	c.add(Restrictions.in("id",ids));
        } else {
	        // Add restrictions to the most distant criteria
	        Criteria[] hy = 
	            Hierarchy.fetchParents(c,klass,Integer.MAX_VALUE);
	        hy[hy.length-1].add(Restrictions.in("id",ids));
        }
	    
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
