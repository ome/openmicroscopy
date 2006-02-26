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

import ome.api.IPojos;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.builders.PojoOptions;

public class PojosCGCPathsQueryDefinition extends Query
{
    
    public PojosCGCPathsQueryDefinition(QueryParameter... parameters)
    {
        super(parameters);
    }
    
    protected void defineParameters(){
        defs = new QueryParameterDef[]{
                new QueryParameterDef(QP.IDS,Collection.class,false),
                new QueryParameterDef("algorithm", String.class, false),
                new QueryParameterDef(QP.OPTIONS,Map.class,true)
        };
    }

    @Override
    protected Object runQuery(Session session) throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(QP.OPTIONS));
        
        String algo = (String) value("algorithm");
        Collection ids = (Collection) value(QP.IDS);
        
        // TODO Leaves
        if (IPojos.DECLASSIFICATION.equals(algo))
        {
            Criteria cg = session.createCriteria(CategoryGroup.class);
            Hierarchy.fetchChildren(cg, CategoryGroup.class, Integer.MAX_VALUE);
            cg.add(Restrictions.in("id",ids));
            return cg.list();
        } else if (IPojos.CLASSIFICATION_NME.equals(algo)) {
            Criteria cg = session.createCriteria(CategoryGroup.class);
            Hierarchy.fetchChildren(cg, CategoryGroup.class, Integer.MAX_VALUE);
            cg.add(Restrictions.not(Restrictions.in("id",ids)));
            return cg.list();
        } else if (IPojos.CLASSIFICATION_ME.equals(algo)) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(" select new list(cg, c) from CategoryGroup cg ");
            sb.append(" left outer join fetch categoryLinks cgcl ");
            sb.append(" left outer join fetch cgcl.child c ");
            sb.append(" left outer join fetch c.imageLinks cil ");
            sb.append(" left outer join fetch cil.child img ");
            sb.append(" where cg not in (");
            sb.append("   select cg2 from CategoryGroup cg2 ");
            sb.append("     where cg2.categoryLinks.child.imageLinks.child.id ");
            sb.append("     in (:id_list) )");
            org.hibernate.Query q = session.createQuery(sb.toString());
            q.setParameterList("id_list",ids);
            return q.list();
            
//            Criteria cg2 = session.createCriteria(CategoryGroup.class);
//            cg2.add(Restrictions.in("categoryLinks.child.imageLinks.child.id", ids));
//            // TODO ask hierarchy for this path.
//            Collection ids2 = cg2.list();
//            cg.add(Restrictions.not(Restrictions.in("id",ids2)));
            
        } else {
            throw new IllegalArgumentException("Unkown algo: "+algo);
            // TODO here or in PojosImpl?
        }

    }

}