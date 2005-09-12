package ome.hibernate;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;

public class SaveUpdateListener extends DefaultSaveOrUpdateEventListener {

	@Override
	public Serializable onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		return super.onSaveOrUpdate(event);
	}
	
}

// TODO Move interceptor code to events.
