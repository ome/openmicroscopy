package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.api.IPojos;
import ome.model.containers.CategoryGroup;
import ome.util.builders.PojoOptions;

public class PojosCGCPathsQueryDefinition extends Query
{
    
    static {
        addDefinition(new IdsQueryParameterDef());
        addDefinition(new OptionsQueryParameterDef());
        addDefinition(new QueryParameterDef(PojosQP.ALGORITHM, 
                String.class, false));
    }
    
    public PojosCGCPathsQueryDefinition(QueryParameter... parameters)
    {
        super(parameters);
    }
    
    @Override
    protected Object runQuery(Session session) 
    throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(QP.OPTIONS));
        
        String algo = (String) value(PojosQP.ALGORITHM);
        Collection ids = (Collection) value(QP.IDS);

        Criteria cg = session.createCriteria(CategoryGroup.class);
        Criteria[] h1 = Hierarchy.
            fetchChildren(cg, CategoryGroup.class, Integer.MAX_VALUE);
        Criteria img = h1[h1.length-1];
        // TODO switch to new Hierarchy();
        
        if (IPojos.DECLASSIFICATION.equals(algo))
        {
            img.add(Restrictions.in("id",ids));
            return cg.list();
        } else if (IPojos.CLASSIFICATION_NME.equals(algo)) {
            img.add(Restrictions.not(Restrictions.in("id",ids)));
            return cg.list();
        } else if (IPojos.CLASSIFICATION_ME.equals(algo)) {

            Criteria cg2 = session.createCriteria(CategoryGroup.class);
            Criteria[] h = Hierarchy.joinChildren(cg2, CategoryGroup.class, 
                    Integer.MAX_VALUE);
            h[h.length-1].add(Restrictions.in("id", ids));
            Collection ids2 = cg2.list();

            if (ids != null && ids2.size() > 0)
                cg.add(Restrictions.not(Restrictions.in("id",ids2)));

            return cg.list();
            
        } else {
            throw new IllegalArgumentException("Unkown algo: "+algo);
            // TODO here or in PojosImpl?
        }

    }

}