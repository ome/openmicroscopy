/*
 * omeis.providers.re.codomain.CodomainMap
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

package omeis.providers.re.codomain;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Declares the interface common to all codomain maps.
 * Subclasses encapsulates a parameterized map:
 * <p><code>
 * y = f(x, p[1], ..., p[n])
 * </code></p>
 * <p>The actual values of the parameters <code>p[k]</code> are defined by the
 * {@link CodomainMapContext} which is passed to the
 * {@link #setContext(CodomainMapContext) setContext} method.
 * </p>
 *
 * @see CodomainMapContext
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 17:23:37 $)
 * </small>
 * @since OME2.2
 */
public interface CodomainMap
{
	
	/**
	 * Sets the parameters used to write the equation of the specified
	 * codomain transformation. 
	 * 
	 * @param cxt	Specifies the parameters.
	 */
	public void setContext(CodomainMapContext cxt);
	
	/**
	 * Performs the transformation.
	 * 
	 * @param x	The input value.
	 * @return The output value, y.
	 */
	public int transform(int x);

}

