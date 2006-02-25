package ome.services.query;

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

}
