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
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.api.IPixels;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.security.CurrentDetails;

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
@Transactional(readOnly=true)
class PixelsImpl extends AbstractLevel2Service
    implements IPixels
{

	private static Log log = LogFactory.getLog(PixelsImpl.class);

    @Override
    protected String getName()
    {
        return IPixels.class.getName();
    }
    
	public Pixels retrievePixDescription(long pixId) {
		Pixels p = (Pixels) iQuery.getById(Pixels.class, pixId);
		return p;
	}

    //TODO we need to validate and make sure only one RndDef per user. 
	public RenderingDef retrieveRndSettings(final long pixId) {
        
        final Long userId = CurrentDetails.getOwner().getId();
        return (RenderingDef) iQuery.queryUnique(
                " select rdef from RenderingDef rdef where " +
                " rdef.pixels.id = ? and rdef.details.owner.id = ?",
                new Object[]{pixId, userId});
	}

    @Transactional(readOnly=false)
	public void saveRndSettings(RenderingDef rndSettings) {
	    iUpdate.saveObject(rndSettings);
	}

}
