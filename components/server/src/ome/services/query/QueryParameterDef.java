package ome.services.query;

import java.util.Collection;
import java.util.Map;

public class QueryParameterDef
{

    public String  name;

    public Class   type;

    public boolean optional;

    public QueryParameterDef(String name, Class type, boolean optional)
    {
        if ( name == null )
            throw new IllegalArgumentException("Name cannot be null.");
        
        if ( type == null )
            throw new IllegalArgumentException("Type cannot be null.");
        
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public void errorIfInvalid( QueryParameter parameter ) 
    {
        if ( parameter.type == null )
        {
            if (! this.optional )
                throw new IllegalArgumentException(
                        "Non-optional parameter type cannot be null.");
            
        } else {
            if ( ! this.type.isAssignableFrom( parameter.type ))
                    throw new IllegalArgumentException(
                            String.format(
                            " Type of parameter %s doesn't match: %s != %s",
                                    name,this.type,parameter.type));
        
        }
        
        if ( parameter.value == null && ! this.optional ) 
            throw new IllegalArgumentException( 
                    "Non-optional parameter "+this.name+" may not be null.");
        
        if ( ! this.optional 
                && Collection.class.isAssignableFrom( this.type )
                && ((Collection) parameter.value ).size() < 1 )
            throw new IllegalArgumentException(
                    "Non-optional collections may not be empty.");

    }
    
}

class OptionsQueryParameterDef extends QueryParameterDef 
{
    public OptionsQueryParameterDef()
    {
        super( "options", Map.class, true );
    }
}

class CollectionQueryParameterDef extends QueryParameterDef
{
    
    public Class elementType;
    
    public CollectionQueryParameterDef(String name, boolean optional, 
            Class elementType)
    {
        super( name, Collection.class, optional );
        this.elementType = elementType;
        
    }
    
    @Override
    public void errorIfInvalid(QueryParameter parameter)
    {
        super.errorIfInvalid( parameter );
        
        if ( ! optional && ((Collection) parameter.value).size() < 1 )
            throw new IllegalArgumentException(
                    "Requried collection parameters may not be empty."
                    );
        
        if ( parameter.value != null ) 
            for (Object element : (Collection) parameter.value )
            {
                if ( ! elementType.isAssignableFrom( element.getClass() ))  
                    throw new IllegalArgumentException(
                            "Elements of type "+element.getClass().getName()+
                            " are not allowed in collections of type "+
                            elementType.getName());
            }
        
    }
    
    
}

class IdsQueryParameterDef extends CollectionQueryParameterDef
{
    public IdsQueryParameterDef( ) 
    {
        super( QP.IDS, false, Long.class );
    }
}