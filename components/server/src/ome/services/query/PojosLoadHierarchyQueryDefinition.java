package ome.services.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;


public class PojosLoadHierarchyQueryDefinition implements HibernateCallback
{

    public final static FetchMode FETCH = FetchMode.JOIN;
    public final static int LEFT_JOIN = Criteria.LEFT_JOIN;
    
    public final static Map<Class,Integer> DEPTH = new HashMap<Class,Integer>();
    static {
        DEPTH.put(Project.class,2);
        DEPTH.put(Dataset.class,1);
        DEPTH.put(CategoryGroup.class,2);
        DEPTH.put(Category.class,1);
    }
    public final static Map<Class,List<String>> CHILDREN = new HashMap<Class,List<String>>();
    static {
        CHILDREN.put(Project.class,Arrays.asList("datasetLinks","imageLinks"));
        CHILDREN.put(Dataset.class,Arrays.asList("imageLinks"));
    }
    
    public Object doInHibernate(Session session) 
    throws HibernateException, SQLException
    {
        Criteria c = session.createCriteria(Project.class);
        c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        c.add(Restrictions.in("id",Arrays.asList(0l,1l)));
        fetchChild(c,Project.class);        

//        then use collection filters for the annotations for a user. (filter)
//        if group do a select before the call; if experimenter singleton
//          then for all members apply.
//          annotation y|s 
          
        return c.list();
        
    }

    protected void fetchChild(Criteria c, Class klass)
    {
        String children = CHILDREN.get(klass).get(0);
        String child = children+".child";
        String grandchildren = child+"."+CHILDREN.get(klass).get(1);
        String grandchild = grandchildren+".child";
        
        switch (DEPTH.get(klass))
        {
            case 2:
                c.createCriteria(grandchildren,LEFT_JOIN);
                c.createCriteria(grandchild,LEFT_JOIN);
                fetchAnnotations(c,grandchild);
            case 1:
                c.createCriteria(children,LEFT_JOIN);
                c.createCriteria(child,LEFT_JOIN);
                fetchAnnotations(c,child);
            case 0:
                return;
            default:
                throw new RuntimeException("Unhandled container depth.");
        }
    }
    
    protected void fetchAnnotations(Criteria c, String path)
    {
        String a_path = path+".annotations";
        c.createCriteria(a_path,LEFT_JOIN);
        c.createCriteria(a_path+".details",LEFT_JOIN);
        c.createCriteria(a_path+".details.owner",LEFT_JOIN)
            .add(Restrictions.in("id",Arrays.asList(1l)));
    }
    
}
//#select() ## Project p | Dataset d | CategoryGroup cg | Category c
//#topDownHierarchy()
//    where 
//#idlist()
//#filters()
//#if($noIds)
//#typeExperimenter()
//#else
//#imageExperimenter()
//#end
