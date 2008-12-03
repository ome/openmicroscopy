package ome.formats.testclient;

import java.lang.reflect.Method;
import java.util.HashMap;

import ome.api.IQuery;
import ome.formats.enums.EnumerationProvider;
import ome.model.IEnum;


public class TestEnumerationProvider implements EnumerationProvider
{

    public IEnum getEnumeration(Class<? extends IEnum> klass, String value,
            boolean loaded)
    {
        
        try {
            IEnum enumeration = (IEnum) klass.newInstance();
            enumeration.setId(-1L);
            Method setValue = klass.getDeclaredMethod("setValue", new Class[] { String.class });
            setValue.invoke(enumeration, "Unknown");
            return enumeration;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, IEnum> getEnumerations(Class<? extends IEnum> klass)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

}
