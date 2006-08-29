/*
 * ome.logic.AbstractLevel1Service
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports

//Third-party libraries
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

//Application-internal dependencies


/**
 * service level 1
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel1Service extends AbstractBean 
{

    // ~ HibernateDaoSupport methods
	// =========================================================================
	
	private HibernateDaoSupport support = new HibernateDaoSupport(){ /* ez */ };
	
    /**
     * delegates to {@link HibernateDaoSupport}. Used during initialization to
     * create a {@link HibernateTemplate}
     * @see HibernateDaoSupport#setSessionFactory(SessionFactory)
     */
	public final void setSessionFactory(SessionFactory sessionFactory) {
		  support.setSessionFactory(sessionFactory);
	}
	
	/**
	 * delegates to {@link HibernateDaoSupport} to get the current
	 * {@link SessionFactory}
	 * @see HibernateDaoSupport#getSessionFactory()
	 */
	public final SessionFactory getSessionFactory() {
		return support.getSessionFactory();
	}

	/**
	 * delegates to {@link HibernateDaoSupport}. Used during initialization to
	 * set the current {@link HibernateTemplate}
	 * @see HibernateDaoSupport#setHibernateTemplate(HibernateTemplate)
	 */
	public final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		support.setHibernateTemplate(hibernateTemplate);
	}

	/**
	 * delegates to {@link HibernateDaoSupport} to get the current 
	 * {@link HibernateTemplate} 
	 * @see HibernateDaoSupport#getHibernateTemplate()
	 */
	public final HibernateTemplate getHibernateTemplate() {
	  return support.getHibernateTemplate();
	}

}

