package ome.ro.ejb;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.util.Filter;
import ome.util.Filterable;
import ome.util.Utils;

public class ShallowCopy {

	public IObject copy(IObject original) {
		IObject f = ShallowCopy.reflectiveNewInstance(original);
		SetValues set = new SetValues(f);
		original.acceptFilter(set);
		return f;
	}

	public static IObject reflectiveNewInstance(Filterable f) {
		Method m;
		IObject iobj;
		try {
			Class c = Utils.trueClass(f.getClass());
			iobj = (IObject) c.newInstance();
		} catch (Exception e) {
			InternalException ie = new InternalException(e.getMessage());
			ie.setStackTrace(e.getStackTrace());
			throw ie;
		}

		return iobj;
	}

}

class SetValues implements Filter {

	private IObject target;

	public SetValues(IObject target) {
		this.target = target;
	}

	public Filterable filter(String fieldId, Filterable f) {
		if (f == null)
			return null;
		if (Details.class.isAssignableFrom(f.getClass())) {
			target.putAt(fieldId, ((Details) f).shallowCopy());
		}

		else if (IObject.class.isAssignableFrom(f.getClass())) {
			IObject old = (IObject) f;
			IObject iobj = (IObject) ShallowCopy.reflectiveNewInstance(f);
			iobj.setId(old.getId());
			iobj.unload();
			target.putAt(fieldId, iobj);
		}

		else {
			throw new InternalException("Unknown filterable type:"
					+ f.getClass());
		}

		return f;

	}

	public Collection filter(String fieldId, Collection c) {
		target.putAt(fieldId, null);
		return c;
	}

	public Map filter(String fieldId, Map m) {
		target.putAt(fieldId, null);
		return m;
	}

	public Object filter(String fieldId, Object o) {
		
		if (o == null) {
			target.putAt(fieldId, null);
		}
		
		// TODO add Object[] filter(Object[]) method to Filterable
		else if (Object[].class.isAssignableFrom(o.getClass()))
		{
			target.putAt(fieldId, null);
		}
		
		else {
			target.putAt(fieldId, o);
		}
		return o;
	}

}

class StoreValues implements Filter {

	public Map values = new HashMap();

	public Filterable filter(String fieldId, Filterable f) {
		if (f == null)
			return null;
		if (Details.class.isAssignableFrom(f.getClass())) {
			values.put(fieldId, ((Details) f).shallowCopy());
		}

		else if (IObject.class.isAssignableFrom(f.getClass())) {
			IObject old = (IObject) f;
			IObject iobj = (IObject) ShallowCopy.reflectiveNewInstance(f);
			iobj.setId(old.getId());
			iobj.unload();
			values.put(fieldId, iobj);
		}

		else {
			throw new InternalException("Unknown filterable type:"
					+ f.getClass());
		}

		return f;

	}

	public Collection filter(String fieldId, Collection c) {
		values.put(fieldId, null);
		return c;
	}

	public Map filter(String fieldId, Map m) {
		values.put(fieldId, null);
		return m;
	}

	public Object filter(String fieldId, Object o) {
		if (o == null) {
			values.put(fieldId, null);
		}
		values.put(fieldId, o);
		return o;
	}

}
