/*
 * ome.dsl.SemanticType
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.dsl;

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies

/** represents a SemanticType <b>definition</b>.
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OMERO-3.0
 */
public abstract class SemanticType {

	// TYPE identifiers
	public final static String ABSTRACT = "abstract";
	public final static String TYPE = "type";
	public final static String CONTAINER = "container";
	public final static String RESULT = "result";
	public final static String LINK = "link";
	public final static String ENUM = "enum";
	public final static Set TYPES = new HashSet();
	static {
		TYPES.add(ABSTRACT);
		TYPES.add(TYPE);
		TYPES.add(CONTAINER);
		TYPES.add(RESULT);
		TYPES.add(LINK);
		TYPES.add(ENUM);
	}
	public final static Map TYPES2CLASSES = new HashMap();
	static {
		TYPES2CLASSES.put(ABSTRACT,AbstractType.class);
		TYPES2CLASSES.put(TYPE,BaseType.class);
		TYPES2CLASSES.put(CONTAINER,ContainerType.class);
		TYPES2CLASSES.put(LINK,LinkType.class);
		TYPES2CLASSES.put(RESULT,ResultType.class);
		TYPES2CLASSES.put(ENUM,EnumType.class);
	}
	
	// all properties
	private Set properties = new HashSet();
	
	/** future class name; required for all types */
	private String id;
	
	/** optional item */
	private String superclass;
	
	// possible interfaces 
	private Boolean abstrakt;
	private Boolean described;
	private Boolean global;
	private Boolean immutable;
	private Boolean named;
	
	/** sets the the various properties available in attrs USING DEFAULTS IF NOT AVAILABLE.
	 * Subclasses may override these values.
	 */
	public SemanticType(Properties attrs){
		setId(attrs.getProperty("id",getId()));
	
		if (null==getId()){
			throw new IllegalStateException("All types must have an id");
		}
		
		setAbstract(Boolean.valueOf(attrs.getProperty("abstract","false")));
		setDescribed(Boolean.valueOf(attrs.getProperty("described","false")));	
		setGlobal(Boolean.valueOf(attrs.getProperty("global","false")));
		setImmutable(Boolean.valueOf(attrs.getProperty("immutable","false")));
		setNamed(Boolean.valueOf(attrs.getProperty("named","false")));
		
		// TODO add "UnsupportedOperation for any other properties in attrs. same in Property
		
	}
	
	
	public void validate(){
		// Left empty in-case anyone forgets to override.
	}

	
	/** creates a new type based on the element-valued key in TYPES2CLASSES. Used mainly by the xml reader */
	public static SemanticType makeNew(String element, Properties attributes) throws IllegalArgumentException, IllegalStateException{
		Class klass = (Class) TYPES2CLASSES.get(element);

		if (null==klass){
			throw new IllegalArgumentException("TYPES2CLASSES does not contain type "+element);
		}
		
		SemanticType st;
		
		try {
			st = (SemanticType) klass.getConstructor(new Class[]{Properties.class}).newInstance(new Object[]{attributes});
		} catch (Exception e) {
			throw new IllegalStateException("Cannot instantiate class "+klass);
		}
		
		return st;
	}
	
	public String toString(){
		String result = "\n"+getId();
		for (Iterator it = getProperties().iterator(); it.hasNext();) {
			Property element = (Property) it.next();
			result+="\n  "+element.toString();
		}
		return result;
	}
	
	public static String typeToColumn(String type){
		return type.substring(type.lastIndexOf(".")+1).toLowerCase();
	}

	//
	// Getters and Setters
	// 
	
	public void setAbstract(Boolean abstrakt) {
		this.abstrakt = abstrakt;
	}

	public Boolean getAbstract() {
		return abstrakt;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setSuperclass(String superclass) {
		this.superclass = superclass;
	}

	public String getSuperclass() {
		return superclass;
	}

	public void setNamed(Boolean named) {
		this.named = named;
	}

	public Boolean getNamed() {
		return named;
	}

	public void setDescribed(Boolean described) {
		this.described = described;
	}

	public Boolean getDescribed() {
		return described;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public Boolean getImmutable() {
		return immutable;
	}

	public void setProperties(Set properties) {
		this.properties = properties;
	}

	public Set getProperties() {
		return properties;
	}


	public void setGlobal(Boolean global) {
		this.global = global;
	}


	public Boolean getGlobal() {
		return global;
	}
	
}

//
//
// Subclasses which implement specific logic. (Essentially aliases for multiple properties)
//
//

class BaseType extends SemanticType {
	public BaseType(Properties attrs){super(attrs);}
}

class AbstractType extends SemanticType {
	public AbstractType(Properties attrs){
		super(attrs);
		this.setAbstract(Boolean.TRUE);
	}
}

class ContainerType extends SemanticType {
	public ContainerType(Properties attrs){
		super(attrs);
		// TODO
	}
}

class LinkType extends SemanticType {	
	public LinkType(Properties attrs){
		super(attrs);
		String to = attrs.getProperty("to");
		String from = attrs.getProperty("from");
		String to_suffix="";
		String from_suffix="";
		if (to.equals(from)){
			to_suffix="1";
			from_suffix="2";
		}
		ManyOneField toMap = new ManyOneField(new Properties());
		toMap.setName(typeToColumn(to)+to_suffix);
		toMap.setType(to);
		ManyOneField fromMap = new ManyOneField(new Properties());
		fromMap.setName(typeToColumn(from)+from_suffix);
		fromMap.setType(from);
		getProperties().add(toMap);
		getProperties().add(fromMap);
	}
}

class ResultType extends SemanticType {
	public ResultType(Properties attrs){
		super(attrs);
		// TODO
	}
}

class EnumType extends SemanticType {

	public EnumType(Properties attrs){super(attrs);}
	
	public void validate(){
		for (Iterator it = getProperties().iterator(); it.hasNext();) {
			if (it.next().getClass()!=EntryField.class){
				throw new IllegalStateException("EnumTypes can only contain EntryProperties.");
			}
		}
		super.validate();
	}
}
