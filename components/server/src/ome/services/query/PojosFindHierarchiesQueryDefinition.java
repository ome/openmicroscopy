package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import ome.model.containers.Project;
import ome.model.core.Image;

public class PojosFindHierarchiesQueryDefinition extends Query
{

    @Override
    protected void defineParameters()
    {
        defs = new QueryParameterDef[] {
                new QueryParameterDef(QP.IDS, Collection.class, false),
                new QueryParameterDef(OWNER_ID, Long.class, true),
                new QueryParameterDef(QP.OPTIONS, Map.class, true) };
    }

    @Override
    protected Object runQuery(Session session) throws HibernateException, SQLException
    {
        Criteria c = session.createCriteria(Image.class);
        fetchParents(c,Project.class,2);
        return c.list();
        
    }

}
//select i from Image i
//#bottomUpHierarchy()
//    where 
//#imagelist()
//#filters()
//#typeExperimenter()
