/*
 * omeis.providers.re.RenderingTask
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

package omeis.providers.re;

import java.util.concurrent.Callable;

import omeis.providers.re.quantum.QuantizationException;

/** 
 * This interface represents a rendering operation task. Its main purpose is to
 * explicitly define the exception strategy for the <code>call()</code> method.
 *
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:callan@blackcat.ca">callan@blackat.ca</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.4 $ $Date: 2005/06/17 12:57:33 $)
 * </small>
 * @since OMERO3.0
 */
public interface RenderingTask extends Callable
{
    /** 
     * Renders wavelength.
     * 
     * @throws QuantizationException If an error occurs while quantizing a
     *                               pixels intensity value.
     */
    public Object call() throws QuantizationException;
}
