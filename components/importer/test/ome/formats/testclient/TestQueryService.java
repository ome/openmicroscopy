package ome.formats.testclient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ome.api.IQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;


public class TestQueryService implements IQuery
{

    public <T extends IObject> T find(Class<T> arg0, long arg1)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> List<T> findAll(Class<T> arg0, Filter arg1)
    {
        try
        {
            List<T> l = new ArrayList<T>();
            IEnum enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            Method setValue = arg0.getDeclaredMethod("setValue", new Class[] { String.class });
            setValue.invoke(enumeration, "Unknown");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "Ar");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "Gas");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "uint8");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "XYCZT");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "int16");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "Monochrome");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, " U Plan Apo");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "W");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "XYZCT");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "Oil");
            l.add((T) enumeration);
            enumeration = (IEnum) arg0.newInstance();
            enumeration.setId(-1L);
            setValue.invoke(enumeration, "1x1");
            l.add((T) enumeration);
            
            return l;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T extends IObject> List<T> findAllByExample(T arg0, Filter arg1)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> List<T> findAllByFullText(Class<T> arg0,
            String arg1, Parameters arg2)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> List<T> findAllByQuery(String arg0,
            Parameters arg1)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> List<T> findAllByString(Class<T> arg0,
            String arg1, String arg2, boolean arg3, Filter arg4)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> T findByExample(T arg0) throws ApiUsageException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> T findByQuery(String arg0, Parameters arg1)
            throws ValidationException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> T findByString(Class<T> arg0, String arg1,
            String arg2) throws ApiUsageException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> T get(Class<T> arg0, long arg1)
            throws ValidationException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

    public <T extends IObject> T refresh(T arg0) throws ApiUsageException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new RuntimeException("Not implemented yet.");
    }

}
