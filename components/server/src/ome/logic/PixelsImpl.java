/*
 * ome.logic.PixelsImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package ome.logic;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.userdetails.User;
import org.hibernate.Query;

//Application-internal dependencies
import ome.model.meta.Experimenter;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.3 $ $Date: 2005/06/12 23:27:31 $)
 * </small>
 * @since OME2.2
 */
class PixelsImpl extends AbstractLevel2Service
    implements ome.api.IPixels
{

	private static Log log = LogFactory.getLog(PixelsImpl.class);
	
	public Pixels retrievePixDescription(long pixId) {
		Pixels p = (Pixels) _query.getById(Pixels.class, pixId);
		return p;
	}

	//FIXME current user!
	public RenderingDef retrieveRndSettings(long pixId) {

        /* FROM earlier DAO
        Pixels pix = (Pixels) session.load(Pixels.class,pixId);
        Experimenter user = (Experimenter) session.load(Experimenter.class,userId);
        
        Query q = session.createQuery("from RenderingSetting r where r.image = :img and r.moduleExecution.experimenter = :exp");
        q.setEntity("img",pix.getImage());
        q.setEntity("exp",user);
        return q.uniqueResult(); */
        
		// Get User Id
		SecurityContext ctx = SecurityContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();
		User u = (User) auth.getPrincipal();
		Experimenter user = (Experimenter) _query.getUniqueByFieldEq(Experimenter.class, "omeName", u.getUsername());
		// TODO user needs to be lazy loaded and cached.
		//return daos.pixels().retrieveRndSettings(user.getId().longValue(),pixId);
        
        throw new UnsupportedOperationException("Doesn't work yet");

	}

	public void saveRndSettings(RenderingDef rndSettings) {
		//pdao.saveRndSettings(1,pixId,rndSettings);
		
		// RenderingDef rnd = retrieveRndSettings(pixId);
		
//		if (null==rnd){
//			
//		} else {
//			
//		}
		
		throw new UnsupportedOperationException("No writes!!!");
		
	}


}
