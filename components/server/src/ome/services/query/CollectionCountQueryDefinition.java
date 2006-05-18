package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ome.parameters.Parameters;
import ome.tools.lsid.LsidUtils;

public class CollectionCountQueryDefinition extends Query
{

    static Definitions defs = new Definitions(
            new IdsQueryParameterDef(),
            new QueryParameterDef("field", String.class, false)
            );
    
    public CollectionCountQueryDefinition(Parameters parameters)
    {
        super( defs, parameters );
    }

    @Override
    protected Object runQuery(Session session)
            throws HibernateException, SQLException
    {
        String s_field = (String) value("field"); // TODO Generics??? if not
        // in arrays!
        String s_target = LsidUtils.parseType(s_field);
        String s_collection = LsidUtils.parseField(s_field);
        String s_query = String.format(
                "select target.id, count(collection) from %s target "
                        + "join target.%s collection "
                        + ( check("ids") ? "where target.id in (:ids)" : "" )
                        + "group by target.id",
                        s_target, s_collection);

        org.hibernate.Query q = session.createQuery(s_query);
        if (check("ids")){
            q.setParameterList("ids",(Collection) value("ids"));
        }
        return q.list();

    }

    // TODO filters...?
}
