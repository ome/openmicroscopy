package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import ome.api.IPojos;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;
import ome.util.builders.PojoOptions;

public class PojosCGCPathsQueryDefinition extends Query
{
    
    static Definitions defs = new Definitions(
        new IdsQueryParameterDef(),
        new OptionsQueryParameterDef(),
        new AlgorithmQueryParameterDef());
    
    public PojosCGCPathsQueryDefinition(Parameters parameters)
    {
        super(defs, parameters);
    }
    
    @Override
    protected void buildQuery(Session session) 
    throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(OPTIONS));
        
        String algo = (String) value(ALGORITHM);
        Collection ids = (Collection) value(IDS);

        Criteria cg = session.createCriteria(CategoryGroup.class);
        cg.setResultTransformer(Hierarchy.getChildTransformer(CategoryGroup.class)); 

        // TODO switch to new Hierarchy();
        Criteria[] h1 = Hierarchy.
            fetchChildren(cg, CategoryGroup.class, Integer.MAX_VALUE);
        Criteria img = h1[h1.length-1];
        
        if (IPojos.DECLASSIFICATION.equals(algo))
        {
            img.add(Restrictions.in("id",ids));
            setCriteria( cg );

        } else if (IPojos.CLASSIFICATION_NME.equals(algo)) {
            img.add(Restrictions.not(Restrictions.in("id",ids)));
            setCriteria( cg );
        
        } else if (IPojos.CLASSIFICATION_ME.equals(algo)) {

            Criteria cg2 = session.createCriteria(CategoryGroup.class);
            cg2.setProjection(Projections.property("id"));
            cg2.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            Criteria[] h = Hierarchy.joinChildren(cg2, CategoryGroup.class, 
                    Integer.MAX_VALUE);
            h[h.length-1].add(Restrictions.in("id", ids));
            Collection ids2 = cg2.list();

            if (ids != null && ids2.size() > 0)
                cg.add(Restrictions.not(Restrictions.in("id",ids2)));

            setCriteria( cg );
            
        } else {
            throw new IllegalArgumentException("Unkown algorithm: "+algo);
            // TODO here or in PojosImpl?
        }

    }
    
    @Override
    protected void enableFilters(Session session)
    {
        ownerOrGroupFilters(session, 
        		new String[]{CategoryGroup.OWNER_FILTER, Category.OWNER_FILTER},
        		new String[]{CategoryGroup.GROUP_FILTER, Category.GROUP_FILTER});
    }
    
}
