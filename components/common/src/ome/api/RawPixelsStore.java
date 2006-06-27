/*
 * ome.api.IPixels
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

package ome.api;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.annotations.NotNull;

/** 
 * metadata gateway for the {@link omeis.providers.re.RenderingEngine}. This 
 * service provides all DB access that the rendering engine needs. This allows
 * the rendering engine to also be run external to the server (e.g. client-side)  
 *
 * @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.moore@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: 1.2 $ $Date: 2005/06/08 15:21:59 $)
 * </small>
 * @since OME2.2
 */
public interface RawPixelsStore extends StatefulServiceInterface
{
    //State management.
    public void setPixelsId( long pixelsId );
    
    /**
     * delegates to {@link ome.io.nio.PixelBuffer}
     * 
     * @param pixelsId
     * @return
     * @see ome.io.nio.PixelBuffer#getPlaneSize()
     */
    public Integer getPlaneSize( );
    
    public Integer getRowSize( );
    
    public Integer getStackSize( );
    
    public Integer getTimepointSize( );
    
    public Integer getTotalSize( );
    
    public Long getRowOffset( 
            @NotNull Integer y, 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    public Long getPlaneOffset( 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public Long getStackOffset( 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public Long getTimepointOffset( 
            @NotNull Integer t );
    
    public byte[] getRegion( 
            @NotNull Integer size, 
            Long offset );
    
    public byte[] getRow( 
            @NotNull Integer y, 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public byte[] getPlane( 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public byte[] getStack( 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public byte[] getTimepoint( 
            @NotNull Integer t );
    
    public void setRegion( 
            @NotNull Integer size, 
            Long offset, 
            @NotNull byte[] buffer );
    
    public void setRow( 
            @NotNull byte[] buffer, 
            @NotNull Integer y, 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public void setPlane( 
            @NotNull byte[] buffer, 
            @NotNull Integer z, 
            @NotNull Integer c,
            @NotNull Integer t );
    
    public void setStack( 
            @NotNull byte[] buffer, 
            @NotNull Integer z, 
            @NotNull Integer c, 
            @NotNull Integer t );
    
    public void setTimepoint( 
            @NotNull byte[] buffer, 
            @NotNull Integer t );
    
    public byte[] calculateMessageDigest( );
    
}
