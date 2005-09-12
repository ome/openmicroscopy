package ome.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

//CompositeUserType, EnhancedUserType, UserCollectionType, UserVersionType
//org.hibernate.usertype.ParameterizedType
public class ArrayStringType implements UserType {

	private int[] parse(String list) {
//		int stop=list.indexOf("]");
//		String cut = list.substring(1,stop);
//		String ints[] = cut.split(",");
//		int ids[] = new int[ints.length];
//		for (int i = 0; i < ids.length; i++) {
//			ids[i]=Integer.parseInt(ints[i].trim());
//		}
//		return ids;
		String strs[] = list.split(" ");
		int ids[] = new int[strs.length];
		for (int i = 0; i < ids.length; i++) {
			if (null!=strs[i] && !strs[i].equals("")){
				ids[i]=Integer.parseInt(strs[i]);
			}
		}
		return ids;
	}

	private String string(int[] ids){
		if (null==ids) return null;
		StringBuilder sb = new StringBuilder(ids.length*3);
		sb.append(" ");
		for (int i = 0; i < ids.length; i++) {
			int value = ids[i];
			sb.append(value);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public int[] sqlTypes() {
		return new int []{ Types.VARCHAR };
	}

	public Class returnedClass() {
		return int[].class;
	}

	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		return Arrays.equals((int[])arg0,(int[])arg1);
	}

	public int hashCode(Object arg0) throws HibernateException {
		return Arrays.hashCode((int[]) arg0);
	}

	public Object nullSafeGet(ResultSet arg0, String[] arg1, Object arg2) throws HibernateException, SQLException {
		String list = (String) Hibernate.STRING.nullSafeGet(arg0,arg1[0]);
		if (list==null) return new int[]{};
		return parse(list);
	}

	public void nullSafeSet(PreparedStatement arg0, Object arg1, int arg2) throws HibernateException, SQLException {
		String list = string((int[])arg1); //Arrays.toString((int[])arg1);
		Hibernate.STRING.nullSafeSet(arg0,list,arg2);
	}

	public Object deepCopy(Object arg0) throws HibernateException {
		if (arg0==null) return null;
		int l = ((int[]) arg0).length;
		int[] newIds = new int[l];
		System.arraycopy(arg0,0,newIds,0,l);
		return newIds;
	}

	public boolean isMutable() {
		return true;
	}

	public Serializable disassemble(Object arg0) throws HibernateException {
		return (Serializable) deepCopy(arg0);
	}

	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return (Serializable) deepCopy(arg0);
	}

	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return (Serializable) deepCopy(arg0);
	}

}
