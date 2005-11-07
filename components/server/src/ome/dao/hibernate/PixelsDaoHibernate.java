/*
 * ome.dao.hibernate.PixelsDaoHibernate
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

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

//Application-internal dependencies
import ome.dao.PixelsDao;
import ome.model.meta.Experimenter;
import ome.model.core.ImagePixel;
import ome.model.core.RenderingSetting;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/** uses Hibernate to fulfill annotation needs.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * 
 */
public class PixelsDaoHibernate extends HibernateDaoSupport implements PixelsDao {

	private static Log log = LogFactory.getLog(PixelsDaoHibernate.class);
	
    public RenderingSetting retrieveRndSettings(final int userId, final int pixId) {
        return (RenderingSetting) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

        		// Get Image Id. TODO Ask Jean-Marie how evil this is.
        		ImagePixel pix = (ImagePixel) session.load(ImagePixel.class,pixId);
        		Experimenter user = (Experimenter) session.load(Experimenter.class,userId);
        		
        		Query q = session.createQuery("from RenderingSetting r where r.image = :img and r.moduleExecution.experimenter = :exp");
        		q.setEntity("img",pix.getImage());
        		q.setEntity("exp",user);
        		return q.uniqueResult(); 

            }
        });
    }

	public void saveRndSettings(int userId, int pixId, RenderingSetting rndSetting) {
		// TODO Auto-generated method stub
		//
		throw new RuntimeException("Not implemented yet.");
	}

	public int createRndSettings() {
		// TODO Auto-generated method stub
		//return 0;
		throw new RuntimeException("Not implemented yet.");
	}

  
}
