package ome.tools.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Properties;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.hibernate.usertype.UserVersionType;

//CompositeUserType, EnhancedUserType, UserCollectionType, UserVersionType
//org.hibernate.usertype.ParameterizedType
public class PermissionsType implements UserType, ParameterizedType {

	byte[] perms = new byte[]{12};
	
	public int[] sqlTypes() {
		return new int []{ Types.VARBINARY };
	}

	public Class returnedClass() {
		return byte[].class;
	}

	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		return Arrays.equals((byte[])arg0,(byte[])arg1);
	}

	public int hashCode(Object arg0) throws HibernateException {
		return Arrays.hashCode((byte[]) arg0);
	}

	public Object nullSafeGet(ResultSet arg0, String[] arg1, Object arg2) throws HibernateException, SQLException {
		byte[] list = (byte[]) Hibernate.BINARY.nullSafeGet(arg0,arg1[0]);
		if (list==null) return new byte[]{}; // TODO
		return list;
	}

	public void nullSafeSet(PreparedStatement arg0, Object arg1, int arg2) throws HibernateException, SQLException {
		if (arg1==null){
			Hibernate.BINARY.nullSafeSet(arg0,perms,arg2);
		} else {
			Hibernate.BINARY.nullSafeSet(arg0,arg1,arg2);
		}
	}

	public Object deepCopy(Object arg0) throws HibernateException {
		if (arg0==null) return null;
		int l = ((byte[]) arg0).length;
		byte[] newBits = new byte[l];
		System.arraycopy(arg0,0,newBits,0,l);
		return newBits;
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

	public void setParameterValues(Properties params) {
		if (params != null) {
			String defaultPerms = params.getProperty("default-perms");
			if (defaultPerms != null) {
				// TODO this.perms = 
			}
		}
			      
	}

}
