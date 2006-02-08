package ome.tools.hibernate;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

public class NullInterceptor { // FIXME del implements Interceptor {

    public boolean onFlushDirty(Object entity, Serializable id, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) throws CallbackException {
    	return false;
    }

    public boolean onSave(Object entity, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException {
   		return false;
    }

    public void onDelete(Object entity, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException {}
    public void postFlush(Iterator arg0) throws CallbackException {}
	public boolean onLoad(Object arg0, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException { return false; }
	public void preFlush(Iterator arg0) throws CallbackException { }
	public Boolean isTransient(Object arg0) { return null;	}
	public int[] findDirty(Object arg0, Serializable id, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) { return null; }
	public Object instantiate(String arg0, EntityMode arg1, Serializable arg2) throws CallbackException { return null; }
	public String getEntityName(Object arg0) throws CallbackException {return null;}
	public Object getEntity(String arg0, Serializable id) throws CallbackException {return null;}
	public void afterTransactionBegin(Transaction arg0) {}
	public void beforeTransactionCompletion(Transaction arg0) {}
	public void afterTransactionCompletion(Transaction arg0) {}

}