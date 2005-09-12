package ome.hibernate;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.NewModel;
import ome.api.OMEModel;
import ome.model.meta.Experimenter;
import ome.model.meta.Event;
import ome.security.CurrentEvent;
import ome.security.CurrentUser;

import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public class EventInterceptor implements Interceptor {

	//http://www.hibernate.org/195.html !!! TODO
	
	// http://www.jroller.com/page/ksevindik/20050417
    class Events {
        public Map<Class,Set<NewModel>> inserts  = new HashMap<Class,Set<NewModel>>();
        public Map<Class,Set<NewModel>> updates = new HashMap<Class,Set<NewModel>>();
        public Map<Class,Set<NewModel>> deletes = new HashMap<Class,Set<NewModel>>();
    }

    private ThreadLocal eventSetHolder = new ThreadLocal();

    private Events events() {
        Object o = eventSetHolder.get();
        if(o == null) {
                   o = new Events();
                   eventSetHolder.set(o);
        }
        return (Events)o;
    }

    public void reset() {
    	eventSetHolder.remove();
    }
    
    private void add(Map<Class, Set<NewModel>> m, Object entity, Serializable id){
    	if (entity instanceof NewModel) {
    		NewModel ome = (NewModel) entity;
    		Class c = ome.getClass();
			Object key = m.get(c);
			if (null == key) 
				m.put(c,new HashSet<NewModel>());

			if (ome.getId() == null) throw new RuntimeException("null id on "+ome);
			m.get(c).add(ome);
			
		}
    }

    private HibernateTemplate t;
    
    public void setHibernateTemplate (HibernateTemplate template){
    	this.t = template;
    }
    
    public boolean onFlushDirty(Object entity, Serializable id, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) throws CallbackException {
   		add(events().updates,entity,id);
    	return false;
    }

    public boolean onSave(Object entity, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException {
    	if (entity instanceof NewModel) {
			NewModel ome = (NewModel) entity;
			Experimenter ex = CurrentUser.asExperimenter();
			saveOrUpdate(ex);
			ome.setOwner(ex);
			Event ev = CurrentEvent.getEvent();
			saveOrUpdate(ev);
			ome.setCreationEvent(ev);
		}
   		add(events().inserts,entity,id);
   		return true;
    }

    public void onDelete(Object entity, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException {
   		add(events().deletes,entity,id);
   	}

    public void saveOrUpdate(OMEModel ome){ // refactor to Current* TODO
		SessionFactory sf = t.getSessionFactory();
		Session s = SessionFactoryUtils.getNewSession(sf,new NullInterceptor());
		s.saveOrUpdate(ome);
		s.flush();s.close();
    }
    
    
    public void postFlush(Iterator arg0) throws CallbackException {
    	try {
    		
    		// AUDIT
    		SessionFactory sf = t.getSessionFactory();
    		Session s = SessionFactoryUtils.getNewSession(sf,new NullInterceptor());//null);//TODO unneeded? 
    		new EventCreator().doIt(s,events());
    		s.flush(); s.close();
    		//TODOSessionFactoryUtils.releaseSession(s, sf);
    		
    		// INDEX
    		LuceneIndexer l = new LuceneIndexer();
    		l.setFile(new File("/tmp/j"));
    		l.doIt(events());
    		
    	} catch (Exception e){
    		e.printStackTrace();
    	} finally {
    		reset();
    	}
    	
//    	t.execute(new HibernateCallback(){
//    		public Object doInHibernate(Session session) throws HibernateException, SQLException 
//    		{
//    			doIt(t.getSessionFactory().openSession(session.connection()));
//    	        return null;
//    		}  
//    	});
    }
    
	public boolean onLoad(Object arg0, Serializable id, Object[] arg2, String[] arg3, Type[] arg4) throws CallbackException { return false; }
	public void preFlush(Iterator arg0) throws CallbackException { }
	public Boolean isTransient(Object arg0) { return null;	}
	public int[] findDirty(Object arg0, Serializable id, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) { return null; }
	public Object instantiate(String arg0, EntityMode arg1, Serializable arg2) throws CallbackException { return null; }
	public String getEntityName(Object arg0) throws CallbackException {return null;}
	public Object getEntity(String arg0, Serializable id) throws CallbackException {return null;}
	public void afterTransactionBegin(Transaction arg0) {}
	public void beforeTransactionCompletion(Transaction arg0) {}
	public void afterTransactionCompletion(Transaction arg0) { 
		reset();
	}

}
