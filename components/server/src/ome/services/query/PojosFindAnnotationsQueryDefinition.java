package ome.services.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.util.builders.PojoOptions;

public class PojosFindAnnotationsQueryDefinition extends Query
{

    static Definitions defs = new Definitions(
        new IdsQueryParameterDef(),
        new OptionsQueryParameterDef(),
        new QueryParameterDef(QP.CLASS,Class.class,false),
        new QueryParameterDef("annotatorIds",Collection.class,true));
        
    public PojosFindAnnotationsQueryDefinition(QueryParameter... parameters)
    {
        super( defs, parameters );
        // TODO set local fields here.
    }

    public final static Map<Class,Class> typeToAnnotationType 
    = new HashMap<Class,Class>();

    public final static Map<Class,String> annotationTypeToPath
    = new HashMap<Class,String>();

    static {
        typeToAnnotationType.put(Image.class,ImageAnnotation.class);
        annotationTypeToPath.put(ImageAnnotation.class,"image");
        typeToAnnotationType.put(Dataset.class,DatasetAnnotation.class);
        annotationTypeToPath.put(DatasetAnnotation.class,"dataset");
        //  TODO this should come from meta-analysis as with hierarchy
    }
    
    @Override
    protected Object runQuery(Session session) 
        throws HibernateException, SQLException
    {
        PojoOptions po = new PojoOptions((Map) value(QP.OPTIONS));
        
        Class target = typeToAnnotationType.get((Class) value(QP.CLASS));
        String path = annotationTypeToPath.get(target);
        
        Criteria c = session.createCriteria(target);
        
        c.createCriteria(path,LEFT_JOIN);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in(path+".id",(Collection) value(QP.IDS)));
        
        if (check("annotatorIds"))
        {
            c.add(Restrictions.in("details.id",(Collection) value("annotatorIds")));
        }

        return c.list();
    }


}