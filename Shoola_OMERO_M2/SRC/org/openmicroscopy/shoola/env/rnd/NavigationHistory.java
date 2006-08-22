/*
 * org.openmicroscopy.shoola.env.rnd.NavigationHistory
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;

/** 
 * Keeps track of the XY planes, within a given pixels set, that have been 
 * rendered and tries to guess the next ones that will be requested.
 * <p>We keep an history of {@link PlaneDef}s, one for each XY plane that has 
 * been renderered.  Obviously only the z and t coordinates of each entry are 
 * relevant and it is trivial that an instance of {@link PlaneDef} can be 
 * identified with a point in the <i>zOt</i> cartesian plane.  Once a 
 * {@link PlaneDef} has been added to the history, we refer to it as a 
 * <i>move</i>.
 * History entries are kept in the same order as they were 
 * {@link #addMove(PlaneDef) added}.  The oldest entry is removed to make room
 * for a new one when {@link #MAX_ENTRIES} is reached.</p>
 * <p>If the history is not empty, then we call the most recent move the 
 * <i>current move</i>.  If the history has more than one entry, then we refer 
 * to the second most recent move as to the <i>last move</i>.  The last move 
 * <i>L</i> and the current move <i>C</i> define a direction (the vector 
 * <i>LC</i>), which we call the current (navigation) direction.  Note that as 
 * long as the history size is greater than two, the current direction is always
 * a line.  
 * This is because a {@link PlaneDef} is never {@link #addMove(PlaneDef) added}
 * to the history if it is the same as the current move &#151; that is, if we
 * haven't moved at all from the previous point, so the current and last moves
 * are always different.</p>
 * <p>Future moves are predicted in a trivial way, based on the two most recent
 * moves in the history &#151; that is on the current navigation direction. 
 * This class should eventually evolve to do something more sophisticated and
 * possibly apply different stratgies to predict upcoming moves based on the
 * analysis of the history.</p>
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
class NavigationHistory
{   
    
    /** The number of stack frames in the pixels set. */
    final int       SIZE_Z;
    
    /** The number of timepoints in the pixels set. */
    final int       SIZE_T;
    
    /** The maximum number of entries in the {@link #history}. */
    final int       MAX_ENTRIES;
    
    /** 
     * List of {@link PlaneDef}s, one for each move.
     * That is, one for each XY plane that has been renderered.  Obviously only
     * the z and t coordinates of each entry are relevant.  Entries are kept in
     * the same order as they were {@link #addMove(PlaneDef) added}.  The
     * oldest entry is {@link #ensureCapacity() removed} to make room for a new
     * one when {@link #MAX_ENTRIES} is reached.
     */
    private List    history;
    
    
    /**
     * Makes sure there's enough room in {@link #history} for a move to be
     * added.
     * If {@link #MAX_ENTRIES} has been reached, then we remove the oldest
     * entry in {@link #history}.  That is, its first element.
     */
    private void ensureCapacity()
    {
        if (history.size() >= MAX_ENTRIES)  //2 at least, so there's element 0. 
            history.remove(0);
    }
    
    /**
     * Returns the plane definition that was added by the second last call
     * to {@link #addMove(PlaneDef)}.
     * If the {@link #history} size is less than two, then <code>null</code>
     * is returned instead.
     * 
     * @return See above.
     */
    private PlaneDef lastMove()
    {
        int hSize = history.size();
        if (1 < hSize) return (PlaneDef) history.get(hSize-2);
        return null;
    }
    
    /**
     * Returns the plane definition that was added by the last call
     * to {@link #addMove(PlaneDef)}.
     * If the {@link #history} size is less than one, then <code>null</code>
     * is returned instead.
     * 
     * @return See above.
     */
    private PlaneDef curMove()
    {
        int hSize = history.size();
        if (0 < hSize) return (PlaneDef) history.get(hSize-1);
        return null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param maxEntries    Maximum number of entries to keep in the history.
     *                      If less than two, it will be set to two.
     * @param sizeZ         The number of stack frames in the pixels set.  
     *                      Must be positive.
     * @param sizeT         The number of timepoints in the pixels set.
     *                      Must be positive.
     */
    NavigationHistory(int maxEntries, int sizeZ, int sizeT)
    {
        if (sizeZ <= 0) 
            throw new IllegalArgumentException(
                    "Non-positive sizeZ: "+sizeZ+".");
        if (sizeT <= 0) 
            throw new IllegalArgumentException(
                    "Non-positive  sizeT: "+sizeT+".");
        SIZE_Z = sizeZ;
        SIZE_T = sizeT;
        MAX_ENTRIES = (maxEntries < 2 ? 2 : maxEntries);
        history = new ArrayList(MAX_ENTRIES);
    }
    
    /**
     * Adds a move to the history.
     * This method is meant to record a move, so a copy of the passed argument
     * is made and then add it to the history.  This way we avoid possible
     * inconsistencies if the caller modifies <code>pd</code> after calling
     * this method.
     * Note that this method won't add <code>pd</code> to the history if it is 
     * the same as the current move &#151; that is, if we haven't moved at all 
     * from the previous point.
     * 
     * @param pd    Represents the move.  Mustn't be <code>null</code>.  
     *              Moreover, only XY planes are accepted and their z, t
     *              indexes must be within the bounds declared to the
     *              constructor of this class: {@link #SIZE_Z}, {@link #SIZE_T}.
     */
    void addMove(PlaneDef pd)
    {
        //First check pd is a good one.
        if (pd == null) throw new NullPointerException("No plane def.");
        int slice = pd.getSlice(), z = pd.getZ(), t = pd.getT();
        if (slice != PlaneDef.XY)
            throw new IllegalArgumentException(
                    "Can only accept XY planes: "+slice+".");
        if (SIZE_Z <= z)  //PlaneDef already checks 0 <= z.
            throw new IllegalArgumentException("z not in [0, SIZE_Z="+SIZE_Z+
                    "): "+z+".");
        if (SIZE_T <= t)  //PlaneDef already checks 0 <= t.
            throw new IllegalArgumentException("t not in [0, SIZE_T="+SIZE_T+
                    "): "+t+".");
        
        //Check if pd is the current move.  If so, return as we haven't moved
        //at all from the previous point.
        if (pd.equals(curMove())) return;  //curMove can be null, but pd is not.
        
        //Now make a copy to avoid caller changing entry after we added.
        pd = new PlaneDef(PlaneDef.XY, t);
        pd.setZ(z);
        
        //Make room for pd if we reached MAX_ENTRIES and finally add.
        ensureCapacity();  
        history.add(pd);
    }
    
    /**
     * Returns the navigation direction with respect to the two most recent
     * moves.
     * If the history size is less than two, this method returns 
     * <code>null</code> as the direction is undefined.  Otherwise, said 
     * <i>L</i> the last move and <i>C</i> the current move, this method
     * returns the line passing through <i>L</i> and <i>C</i>, having the same
     * orientation as the <i>LC</i> vector, and having its origin in <i>C</i>. 
     * 
     * @return One of the flags defined by this class.
     */
    Line currentDirection()
    {
        int hSize = history.size();
        
        //We need at least two moves to determine the direction. 
        if (hSize < 2) return null;
        
        //Get the most two recent moves.  Note they can't be null b/c addMove
        //doesn't allow null.  Moreover, they represent different points b/c
        //addMove doesn't add the same move twice in a row.  So we can build
        //a line.
        PlaneDef curMove = curMove(), lastMove = lastMove();
        PlanePoint L = new PlanePoint(lastMove.getZ(), lastMove.getT()),
                C = new PlanePoint(curMove.getZ(), curMove.getT());
        return new Line(L, C, C);  //Direction LC and origin C.
    }
    
    /**
     * Predicts at most <code>maxMoves</code> upcoming moves.  
     * Each move is represented by an instance of {@link PlaneDef} and all
     * of them are packed into the array returned by this method.  This array
     * never contains the current move (the move added by the most recent call
     * to the {@link #addMove(PlaneDef) addMove} method) and can have less than
     * <code>maxMoves</code> elements if it wasn't possible to predict that much
     * moves.  In particular, the length may be <code>0</code> if no prediction
     * could be made,  but <code>null</code> is never returned.  If the array
     * does contain elements, the first element (element at <code>0</code>) is
     * the first upcoming move, the second element is the second upcoming move,
     * and so on.
     * 
     * @param maxMoves  Maximum number of moves to predict.
     * @return  An array containing the predicted moves.
     */
    PlaneDef[] guessNextMoves(int maxMoves)
    {
        Line dir = currentDirection();
        
        //We can't guess if we don't have at least two moves.  Also return 
        //if maxMoves <= 0. 
        if (dir == null || maxMoves <= 0) 
            return new PlaneDef[0];  //Never return null.
        
        List nextMoves = new ArrayList(maxMoves);
        PlanePoint p;
        PlaneDef pd;
        for (int k = 1; k <= maxMoves; ++k) {  //Iterate maxMoves at most.
            //Get next point in the current direction.
            p = dir.getPoint(k);  //See note.
            if (p.x1 < 0 || SIZE_Z <= p.x1  || 
                    p.x2 < 0 || SIZE_T <= p.x2) break;
            pd = new PlaneDef(PlaneDef.XY, (int) p.x2);
            pd.setZ((int) p.x1);
            
            //Even though dir.getPoint is monotonic, we could be getting a pd
            //equal to the previous one b/c of the above casts to int.  However,
            //this shouldn't happen if navigation is || to the z or t axis.
            if (!nextMoves.contains(pd))  //Never allow duplicates.
                nextMoves.add(pd);
        }
        return (PlaneDef[]) nextMoves.toArray(new PlaneDef[0]);
    }
    /* NOTE: If C is the current move and L the last move, then dir is the
     * line C + ku, u being the unit vector built from LC.  Thus the getPoint
     * method returns C + u, C + 2u, .. , C + mu (m being maxMoves). 
     * For this reason, if navigation is parallel to the z or t axis, then
     * we move to the next integer coordinates along the line in the LC 
     * direction -- casts to int shouldn't change anything in this case. 
     * However, this class will keep on working even with fancy viewers (if
     * you can think of any) that don't navigate in directions parallel to the
     * z or t axis.  The only thing to be careful about in that case is the
     * slope of the line.  If too steep, then even large values of maxMoves
     * could result in no prediction -- b/c of the cast to int we could be
     * getting the same pd all the time.
     */
  
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */ 
    
    List getHistory() { return history; }
    
}
