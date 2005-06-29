/*
 * org.openmicroscopy.omero.logic.util.DaoUtils
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

package org.openmicroscopy.omero.logic;

//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.omero.OMEModel;

/** 
 * replaces all OMEModel.utils with DAO-based utilities on the server-side.
 * This is done by creating the object within the Spring Framework.
 * 
 * The design requirements for the model utilities are:
 * <ol>
 *   <li>we don't want Hibernate dependencies in /common</li>
 *   <li>we don't necessarily want to use reflection in a service call</li>
 *   <li>we <b>must</b> walk the tree to get rid of server-side proxies</li>
 *   <li>it must be easily configurable from Spring</li>
 * </ol>
 * 
 * OMEModel is the only super-class/interface of the model objects. It now contains
 * a getters and setters (instance methods) for a static field <code>utils</code>.
 * 
 * <code>utils</code> either contains the base null-op utility or a code-generated 
 * utility which "knows" what's in the model class.
 * 
 * When an instance of a <code>DaoUtils</code> is created in Spring, it uses the existing
 * metadata to iterate over all classes/tables and edit the OMEModel.utils field.  
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public interface DaoUtils {

    public void clean(Set setOfModelObjects);
    public void clean(OMEModel modelObject);
   
    
}
