/*
 * org.openmicroscopy.shoola.agents.viewer.movie.defs.MovieSettings
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

package org.openmicroscopy.shoola.agents.viewer.movie.defs;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Object only useful to store temporarily the movie settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class MovieSettings
{
    
    /** Start value of the z-movie. */
    private int startZ;
    
    /** End value for z-movie. */
    private int endZ;
    
    /** Start value of the t-movie. */
    private int startT;
    
    /** End value for t-movie. */
    private int endT;
    
    /** 
     * One of the constants defined by 
     * {@link org.openmicroscopy.shoola.agents.viewer.movie.Player}
     */
    private int movieType;
    
    /** 
     * One of the constants defined by 
     * {@link org.openmicroscopy.shoola.agents.viewer.movie.Player}
     */
    private int movieIndex;
    
    /** 
     * One of the constants defined by 
     * {@link org.openmicroscopy.shoola.agents.viewer.movie.Player}
     */
    private int rate;
    
    public MovieSettings(int startZ, int endZ, int startT, int endT, 
                            int movieType, int movieIndex, int rate) 
    {
        this.startZ = startZ;
        this.endZ = endZ;
        this.startT = startT;
        this.endT = endT;
        this.movieType = movieType;
        this.movieIndex = movieIndex;
        this.rate = rate;
    }

    public int getEndT() { return endT; }

    public int getEndZ() { return endZ; }

    public int getMovieIndex() { return movieIndex; }

    public int getMovieType() { return movieType; }

    public int getRate() { return rate; }
    
    public int getStartT() { return startT; }

    public int getStartZ() { return startZ; }
    

    public void setAll(int startZ, int endZ, int startT, int endT, 
            int movieType, int movieIndex, int rate) 
    {
        this.startZ = startZ;
        this.endZ = endZ;
        this.startT = startT;
        this.endT = endT;
        this.movieType = movieType;
        this.movieIndex = movieIndex;
        this.rate = rate;
    }
    
}
