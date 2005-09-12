package ome.hibernate;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveEventListener;

public class SaveListener extends DefaultSaveEventListener {

	@Override
	public Serializable onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		return super.onSaveOrUpdate(event);
	}
	
}
