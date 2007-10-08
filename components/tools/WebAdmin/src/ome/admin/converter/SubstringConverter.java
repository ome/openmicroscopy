/*
* ome.admin.controller
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.converter;

//Java imports

//Third-party libraries
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
 
/**
 * It's the converter class for changing input fields as Objects.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public final class SubstringConverter implements Converter  {
 
    /**
     * Creates a new instance of SubstringConverter
     */
    public SubstringConverter() {
        super();
    }
 
    /**
     * Gets as Object
     * @param fc {@link javax.faces.context.FacesContext}
     * @param toConvert {@link javax.faces.component.UIComponent}
     * @param d {@link java.lang.String}
     * @return Object
     */
    public Object getAsObject( FacesContext fc, UIComponent toConvert, String d ) {
        return d;
    }
 
    /**
     * Gets as String
     * @param fc {@link javax.faces.context.FacesContext} Faces Context
     * @param toConvert {@link javax.faces.component.UIComponent} UI Compnent for converting
     * @param ob {@link java.lang.Object} Object for converting cast as String
     * @return String
     */
    public String getAsString( FacesContext fc, UIComponent toConvert, Object ob) {
    	if(((String)ob)!=null)
	    	if(((String)ob).length()>40) {
	    		return ((String)ob).substring(0,40)+"...";   		
	    	} else return ((String)ob);
    	else return "";
    }
}

