package ome.formats.utests;

import java.lang.reflect.Method;
import java.util.HashMap;

import static omero.rtypes.*;
import ome.formats.enums.EnumerationProvider;
import omero.RString;
import omero.model.IObject;


public class TestEnumerationProvider implements EnumerationProvider
{

    public <T extends IObject> T getEnumeration(Class<T> klass, String value,
                                                boolean loaded)
    {

        try {
            Class concreteClass = Class.forName(klass.getName() + "I");
            IObject enumeration = (IObject) concreteClass.newInstance();
            enumeration.setId(rlong(-1L));
            Method setValue = concreteClass.getMethod(
                    "setValue", new Class[] { RString.class });
            setValue.invoke(enumeration, rstring("Unknown"));
            return (T) enumeration;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T extends IObject> HashMap<String, T> getEnumerations(Class<T> klass)
    {
        throw new RuntimeException("Not implemented yet.");
    }

}
