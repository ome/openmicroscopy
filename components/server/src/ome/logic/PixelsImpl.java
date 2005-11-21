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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.providers.dao.User;

//Application-internal dependencies
import ome.api.Pixels;
import ome.dao.DaoFactory;
import ome.model.Experimenter;
import ome.model.ImagePixel;
import ome.model.RenderingSetting;

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
class PixelsImpl
    implements Pixels
{

	private static Log log = LogFactory.getLog(PixelsImpl.class);
	
	DaoFactory daos;
	
	public PixelsImpl(DaoFactory factory){
		this.daos = factory;
	}
	
	public ImagePixel retrievePixDescription(int pixId) {
		ImagePixel p = (ImagePixel) daos.generic().getById(ImagePixel.class, pixId);
		return p;
	}

	//FIXME current user!
	public RenderingSetting retrieveRndSettings(int pixId) {

		// Get User Id
		SecureContext ctx = (SecureContext) ContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();
		User u = (User) auth.getPrincipal();
		Experimenter user = (Experimenter) daos.generic().getUniqueByFieldEq(Experimenter.class, "omeName", u.getUsername());
		// TODO user needs to be lazy loaded and cached.
		return daos.pixels().retrieveRndSettings(user.getAttributeId(),pixId);

	}

	public void saveRndSettings(int pixId, RenderingSetting rndSettings) {
		//pdao.saveRndSettings(1,pixId,rndSettings);
		
		RenderingSetting rnd = retrieveRndSettings(pixId);
		
		if (null==rnd){
			
		} else {
			
		}
		
		throw new UnsupportedOperationException("No writes!!!");
		
	}


}
