/*
 * ome.dao.DaoFactory
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

package ome.dao;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** collection of data access objects.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 2.0
 */
public class DaoFactory {

	private AnalysisDao ydao;
	private AnnotationDao adao;
	private ContainerDao cdao;
	private GenericDao gdao;
	private PixelsDao pdao;
	
	public DaoFactory(
			AnalysisDao analysis,
			AnnotationDao annotations,
			ContainerDao container,
			GenericDao generic,
			PixelsDao pixels){
		ydao = analysis;
		adao = annotations;
		cdao = container;
		gdao = generic;
		pdao = pixels;
	}

	public AnalysisDao analysis(){
		return ydao;
	}
	
	public AnnotationDao annotation(){
		return adao;
	}

	public ContainerDao container(){
		return cdao;
	}
	
	public GenericDao generic(){
		return gdao;
	}
	
	public PixelsDao pixels(){
		return pdao;
	}
		
}