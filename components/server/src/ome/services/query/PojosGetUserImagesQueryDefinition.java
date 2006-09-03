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

public class PojosGetUserImagesQueryDefinition 
    extends Query
{

    static Definitions defs = new Definitions(
            new OptionsQueryParameterDef());

    public PojosGetUserImagesQueryDefinition(Parameters parameters)
    {
        super( defs, parameters );
    }
        
    @Override
    protected void buildQuery(Session session) 
    throws HibernateException, SQLException
    {
    	// TODO copied from PojosGetImages refactor
        Criteria c = session.createCriteria(Image.class);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        Criteria pix = c.createCriteria("defaultPixels",LEFT_JOIN);
        pix.createCriteria("pixelsType",LEFT_JOIN);
        pix.createCriteria("pixelsDimensions",LEFT_JOIN);
        // endTODO
	    
        setCriteria( c );
    }
    
    @Override
    protected void enableFilters(Session session)
    {
        ownerOrGroupFilters(session, 
        		new String[]{Image.OWNER_FILTER},
        		new String[]{Image.GROUP_FILTER});
    }    

}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
