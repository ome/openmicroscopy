package ome.services.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public abstract class PojosQP extends QP
{

    public final static String ALGORITHM = "algorithm";
    
    public static QueryParameter klass( Class klass ){
        return Class( CLASS, klass );
    }
    
    public static QueryParameter ids( Collection ids ){
        return new QueryParameter( QP.IDS, Collection.class, ids );
    }
    
    public static QueryParameter options( Map options ){
        return Map( OPTIONS, options );
    }
    
    public static QueryParameter algorithm( String algo ){
        return QP.String( ALGORITHM, algo );
    }
}
