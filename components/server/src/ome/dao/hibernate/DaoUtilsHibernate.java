/*
 * ome.logic.util.DaoUtilsHibernate
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

package ome.dao.hibernate;

//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

//Application-internal dependencies
import ome.util.BaseModelUtils;
import ome.api.OMEModel;
import ome.dao.DaoUtils;



/** 
 * replaces all OMEModel.utils with DAO-based utilities on the server-side.
 * This is done by creating the object within the Spring Framework. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class DaoUtilsHibernate implements DaoUtils {

    private static Log log = LogFactory.getLog(DaoUtilsHibernate.class);

    protected SessionFactory sessions;

    /** updating the model utilities here means that after a change to the 
     * Spring Conext (i.e. a new SessionFactory) simply creating a new
     * DaoUtils is enough to have the system 
     * in a consistent state
     * @param sessions
     */
public DaoUtilsHibernate(SessionFactory sessions){
        this.sessions=sessions;
        Map map = sessions.getAllClassMetadata();
        String msg = "Key value in metadata doesn't represent a class: can't get instance for ";
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {

            // Get an instance of this type
            String objClass = (String) it.next();
            OMEModel modelInstance = null;
            try {
                modelInstance = (OMEModel) Class.forName(objClass).newInstance();
            } catch (Exception e){
                 log.fatal(msg+objClass);
                throw new IllegalArgumentException(msg+objClass);
            }

            // Get a ModelUtil for this type and add it
            String[] parts = objClass.split("[.]");
            String utilClass = "ome.dao.utils."+parts[parts.length-1]+"Utils";
            try {
                BaseModelUtils utilsInstance = (BaseModelUtils) Class.forName(utilClass).newInstance();
                modelInstance.setUtils(utilsInstance	);
            } catch (Exception e){
                 log.fatal(msg+utilClass);
                throw new IllegalArgumentException(msg+utilClass);
            }

         }
    }
    public void clean(Set setOfModelObjects) {//TODO generics
        if (setOfModelObjects!=null){
            Set done = new HashSet();
            for (Iterator it = setOfModelObjects.iterator(); it.hasNext();) {
                Object o = it.next();
                if (o instanceof OMEModel) {
                    OMEModel modelObj = (OMEModel) o;
                    modelObj.getUtils().clean(modelObj, done);
                } else if (o instanceof Set) {
                    Set innerSetOfModelObjects = (Set) o;
                    clean(innerSetOfModelObjects);
                } else {
                    throw new IllegalArgumentException("Can't clean objects of type "+o.getClass());
                }
            }
        }
    }

    public void clean(OMEModel modelObj) {
        if (modelObj!=null){
            modelObj.getUtils().clean(modelObj, new HashSet());
        }
    }

}
