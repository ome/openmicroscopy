package ome.hibernate;

import java.util.Map;
import java.util.Set;

import ome.NewModel;
import ome.api.OMEModel;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;

import org.hibernate.Session;

public class EventCreator {

    public void doIt(Session s, EventInterceptor.Events events){
    		// perform inserting audit logs for entities those were enlisted in
			// inserts,
            // updates, and deletes sets...

    		// Get CurrentModule here
    		Event e = new Event();
    		e.setName("test");
    		s.save(e);
    		
    		EventLog l = new EventLog();
    		l.setEvent(e);
    		l.setExperimenter(1);
    		s.save(l);
    		
    		makeDiffs(s,l,events.inserts,"INSERT");
    		makeDiffs(s,l,events.updates,"UPDATES");
    		makeDiffs(s,l,events.deletes,"DELETES");
    }
	
    private void makeDiffs(Session s, EventLog l, Map<Class,Set<NewModel>> m, String action){
		for (Class key : m.keySet()){
			Set<NewModel> ids = m.get(key);
			if (ids.size()>0) {
				EventDiff d = new EventDiff();
				d.setAction(action);
				d.setEventLog(l);
				int[] ints = new int[ids.size()];
				NewModel omes[] = (NewModel[]) ids.toArray(new NewModel[ids.size()]);
				for (int i = 0; i < ints.length; i++) {
					ints[i] = omes[i].getId().intValue(); 
				}
				d.setIds(ints);
				d.setType(key.getName());
				s.save(d);
			}
		}
    }
}
