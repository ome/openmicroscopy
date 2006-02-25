package ome.services.query;

import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class QP
{
    
    
    public static QueryParameter Class(String name, Class value){
        return new QueryParameter(name,Class.class,value);
    }
    
    public static QueryParameter Long(String name, Long value){
        return new QueryParameter(name,Long.class,value);
    }
    
    public static QueryParameter Set(String name, Set value){
        return new QueryParameter(name, Set.class, value);
    }
    
    public static QueryParameter List(String name, List value){
        return new QueryParameter(name,List.class,value);
    }
    
    public static QueryParameter Map(String name, Map value){
        return new QueryParameter(name,Map.class,value);
    }
    
    public static QueryParameter String(String name, String value){
        return new QueryParameter(name,String.class,value);
    }
    
    public static QueryParameter Null(String name){
        return new QueryParameter(name,null,null);
    }

    // For Queries
    public final static String CLASS = "class";
    public final static String OPTIONS = "options";
    public final static String IDS = "ids";
}
