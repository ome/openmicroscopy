/* ome.security.basic.FlushEntityListener
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

package ome.security.basic;

// Java imports


// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.event.def.DefaultFlushEntityEventListener;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

/**
 * responsible for responding to {@link FlushEntityEvent}. Necessary to perform
 * clean up of entities.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0
 * @see     BasicSecuritySystem#lockMarked()
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class FlushEntityEventListener extends DefaultFlushEntityEventListener
{

	private static final long serialVersionUID = 240558701677298961L;

	private static Log log = LogFactory.getLog( FlushEntityEventListener.class );
	
	private BasicSecuritySystem secSys;
	
	/** main constructor. Requires a non-null security system */
	public FlushEntityEventListener( BasicSecuritySystem securitySystem )
	{
		Assert.notNull(securitySystem);
		this.secSys = securitySystem;
	}
    
	@Override
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException
	{
		secSys.lockMarked();
		super.onFlushEntity(event);
	}
}
