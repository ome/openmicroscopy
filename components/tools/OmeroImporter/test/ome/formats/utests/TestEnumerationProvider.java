package ome.formats.utests;

import java.lang.reflect.Method;
import java.util.HashMap;

import static omero.rtypes.*;
import ome.formats.enums.EnumerationProvider;
import omero.RString;
import omero.model.IObject;


public class TestEnumerationProvider implements EnumerationProvider
{

    public IObject getEnumeration(Class<? extends IObject> klass, String value,
            boolean loaded)
    {
        
        try {
            Class concreteClass = Class.forName(klass.getName() + "I");
            IObject enumeration = (IObject) concreteClass.newInstance();
            enumeration.setId(rlong(-1L));
            Method setValue = concreteClass.getMethod(
                    "setValue", new Class[] { RString.class });
            setValue.invoke(enumeration, rstring("Unknown"));
            return enumeration;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, IObject> getEnumerations(Class<? extends IObject> klass)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

}
