package ome.services.dao;

import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.model.IObject;

public class Query {

    IQuery iQuery = new IQuery(){

        public Object getById(Class arg0, long arg1)
        {
            try
            {
                return arg0.newInstance();
            } catch (Exception e)
            {
                return null;
            }
        }

        public List getByClass(Class arg0)
        {
            return null;
            
            
        }

        public Object getUniqueByExample(Object arg0)
        {
            return null;
            
            
        }

        public List getListByExample(Object arg0)
        {
            return null;
            
            
        }

        public Object getUniqueByFieldILike(Class arg0, String arg1, String arg2)
        {
            return null;
            
            
        }

        public List getListByFieldILike(Class arg0, String arg1, String arg2)
        {
            return null;
            
            
        }

        public Object getUniqueByFieldEq(Class arg0, String arg1, Object arg2)
        {
            return null;
            
            
        }

        public List getListByFieldEq(Class arg0, String arg1, Object arg2)
        {
            return null;
            
            
        }

        public Object getUniqueByMap(Class arg0, Map arg1)
        {
            return null;
            
            
        }

        public List getListByMap(Class arg0, Map arg1)
        {
            return null;
            
            
        }

        public Object queryUnique(String arg0, Object[] arg1)
        {
            return null;
            
            
        }

        public List queryList(String arg0, Object[] arg1)
        {
            return null;
            
            
        }

        public Object queryUniqueMap(String arg0, Map arg1)
        {
            return null;
            
            
        }

        public List queryListMap(String arg0, Map arg1)
        {
            return null;
            
            
        }
        
    };
    
	<T extends IObject> Dao<T> getDao() { return new Dao(iQuery); }
	
}
