/*
 * org.openmicroscopy.shoola.env.rnd.TestNavigationHistory
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
import java.util.List;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.util.math.geom2D.Line;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;

/** 
 * Unit test for {@link NavigationHistory}.
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
public class TestNavigationHistory
    extends TestCase
{

    private final int           MAX_ENTRIES = 2,  //Used to construct target. 
                                SIZE_Z = 4, 
                                SIZE_T = 4;
    private NavigationHistory   target;  //Object under test. 
    
    private PlaneDef            pd_0_0, pd_0_1, pd_0_2, pd_0_3,
                                        pd_1_1, pd_2_1, pd_3_1;  //(z,t) moves.
    
    
    //Used in tests that verify current direction is calculated properly.
    private void verifyDirection(Line dir, PlanePoint lastMove, PlanePoint curMove)
    {
        assertNotNull("Shouldn't return null if there's more than one move "+
                "in history.", dir);
        assertTrue("Last move should lie on negative half of the current "+
                "navigation direction.", 
                dir.lies(lastMove, false));
        assertTrue("Current move should lie on positive half of the current "+
                "navigation direction.", 
                dir.lies(curMove, true));
    }
    
    public void setUp()
    {
        target = new NavigationHistory(MAX_ENTRIES, SIZE_Z, SIZE_T);
        pd_0_0 = new PlaneDef(PlaneDef.XY, 0);
        pd_0_1 = new PlaneDef(PlaneDef.XY, 1);
        pd_0_2 = new PlaneDef(PlaneDef.XY, 2);
        pd_0_3 = new PlaneDef(PlaneDef.XY, 3);
        pd_1_1 = new PlaneDef(PlaneDef.XY, 1);
        pd_1_1.setZ(1);
        pd_2_1 = new PlaneDef(PlaneDef.XY, 1);
        pd_2_1.setZ(2);
        pd_3_1 = new PlaneDef(PlaneDef.XY, 1);
        pd_3_1.setZ(3);
    }

    public void testNavigationHistoryIllegalArgs()
    {
        try {
            new NavigationHistory(2, -4, 4);
            fail("Shoud only accept positive sizeZ.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new NavigationHistory(2, 0, 4);
            fail("Shoud only accept positive sizeZ.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new NavigationHistory(2, 4, -1);
            fail("Shoud only accept positive sizeT.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new NavigationHistory(2, 4, 0);
            fail("Shoud only accept positive sizeT.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testNavigationHistory()
    {
        assertEquals("MAX_ENTRIES should be set to what specified to "+
                "constructor if >= 2.", 
                MAX_ENTRIES, target.MAX_ENTRIES);
        assertEquals("History size should always be 0 after construction.", 
                0, target.getHistory().size());
        assertEquals("SIZE_T should be set to what specified to constructor "+
                "if > 0.", 
                SIZE_T, target.SIZE_T);
        assertEquals("SIZE_Z should be set to what specified to constructor "+
                "if > 0.", 
                SIZE_Z, target.SIZE_Z);
        target = new NavigationHistory(-1, 4, 4);
        assertEquals("MAX_ENTRIES should be set to 2 if what specified to "+
                "constructor is < 2.", 
                2, target.MAX_ENTRIES);
    }

    public void testAddMoveBadArgs()
    {
        try {
            target.addMove(null);
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        try {
            target.addMove(new PlaneDef(PlaneDef.XZ, 0));
            fail("Shouldn't accept XZ plane.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            target.addMove(new PlaneDef(PlaneDef.ZY, 0));
            fail("Shouldn't accept ZY plane.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            target.addMove(new PlaneDef(PlaneDef.XY, SIZE_T));
            fail("Shouldn't accept a plane with a t greater than SIZE_T-1.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            PlaneDef pd = new PlaneDef(PlaneDef.XY, 0);
            pd.setZ(SIZE_Z);
            target.addMove(pd);
            fail("Shouldn't accept a plane with a z greater than SIZE_Z-1.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testAddMoveCloning()
    {
        List history = target.getHistory();
        target.addMove(pd_0_1);  //(z=0, t=1)
        assertNotSame("Should have cloned the plane before adding.", 
                pd_0_1, history.get(0));
    }
    
    public void testAddMoveSameMove()
    {
        List history = target.getHistory();
        target.addMove(pd_0_1);  //(z=0, t=1)
        target.addMove(pd_0_1);  //(z=0, t=1) again
        assertEquals("Shouldn't add same move twice in a row.", 
                1, history.size());
    }
    
    public void testAddMove()
    {
        List history = target.getHistory();
        
        target.addMove(pd_0_0);  //(z=0, t=0)
        assertEquals("Should have added a move.", 1, history.size());
        assertEquals("Added wrong move.", pd_0_0, history.get(0));
        
        target.addMove(pd_0_1);  //(z=0, t=1)
        assertEquals("Should have added a move.", 2, history.size());
        assertEquals("Moves should be kept in the same order as they were "+
                "added.", pd_0_0, history.get(0));
        assertEquals("Added wrong move.", pd_0_1, history.get(1));
        
        target.addMove(pd_0_2);  //(z=0, t=2)
        assertEquals("History shouldn't grow bigger than MAX_ENTRIES.", 
                2, history.size());
        assertEquals("Oldest move should be deleted to make room for new one.", 
                pd_0_1, history.get(0));
        assertEquals("Added wrong move.", pd_0_2, history.get(1));
    }

    
    public void testCurrentDirectionUndef()
    {
        assertNull("Should return null if there are no moves in history.", 
                target.currentDirection());
        target.addMove(pd_0_1);  //(z=0, t=1)
        assertNull("Should return null if there's less than two moves in "+
                "history.", target.currentDirection());
    }
    
    public void testCurrentDirectionTForward()
    {
        target.addMove(pd_0_1);  //(z=0, t=1)
        target.addMove(pd_0_2);  //(z=0, t=2)
        PlanePoint L = new PlanePoint(pd_0_1.getZ(), pd_0_1.getT()),  //Last move.
                C = new PlanePoint(pd_0_2.getZ(), pd_0_2.getT());  //Current move.
        Line dir = target.currentDirection();
        
        verifyDirection(dir, L, C);
    }
    
    public void testCurrentDirectionTBackward()
    {
        target.addMove(pd_0_2);  //(z=0, t=2)
        target.addMove(pd_0_1);  //(z=0, t=1)
        PlanePoint L = new PlanePoint(pd_0_2.getZ(), pd_0_2.getT()),  //Last move.
                C = new PlanePoint(pd_0_1.getZ(), pd_0_1.getT());  //Current move.
        Line dir = target.currentDirection();
        
        verifyDirection(dir, L, C);
    }
    
    public void testCurrentDirectionZForward()
    {
        target.addMove(pd_0_1);  //(z=0, t=1)
        target.addMove(pd_1_1);  //(z=1, t=1)
        PlanePoint L = new PlanePoint(pd_0_1.getZ(), pd_0_1.getT()),  //Last move.
                C = new PlanePoint(pd_1_1.getZ(), pd_1_1.getT());  //Current move.
        Line dir = target.currentDirection();
        
        verifyDirection(dir, L, C);
    }
    
    public void testCurrentDirectionZBackward()
    {
        target.addMove(pd_1_1);  //(z=1, t=1)
        target.addMove(pd_0_1);  //(z=0, t=1)
        PlanePoint L = new PlanePoint(pd_1_1.getZ(), pd_1_1.getT()),  //Last move.
                C = new PlanePoint(pd_0_1.getZ(), pd_0_1.getT());  //Current move.
        Line dir = target.currentDirection();
        
        verifyDirection(dir, L, C);
    }

    public void testGuessNextMovesWhenUndef()
    {
        PlaneDef[] moves = target.guessNextMoves(1);
        assertEquals("Should return a 0-length array if no guess could be "+
                "made (no history).", 0, moves.length);
        target.addMove(pd_0_0);
        moves = target.guessNextMoves(2);
        assertEquals("Should return a 0-length array if no guess could be "+
                "made (less than 2 entries in history).", 0, moves.length);
        moves = target.guessNextMoves(0);
        assertEquals("Should return a 0-length array if no guess could be "+
                "made (non-positive argument).", 0, moves.length);
    }
    
    public void testGuessNextMovesWhenTForward()
    {
        target.addMove(pd_0_0);  //(z=0, t=0)
        target.addMove(pd_0_1);  //(z=0, t=1)
        
        PlaneDef[] moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed two moves (last move was "+
                "two timepoints away from SIZE_T).", 2, moves.length);
        assertEquals("Guessed wrong move.", pd_0_2, moves[0]);
        assertEquals("Guessed wrong move.", pd_0_3, moves[1]);
        
        target.addMove(pd_0_2);  //(z=0, t=2) 
        moves = target.guessNextMoves(1);
        assertEquals("Could only have guessed one move (last move was "+
                "second last timepoint).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_3, moves[0]);
        
        moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed one move (last move was "+
                "second last timepoint).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_3, moves[0]);
        
        target.addMove(pd_0_3);  //(z=0, t=3)
        moves = target.guessNextMoves(1);
        assertEquals("Couldn't have guessed any move (last move was "+
                "last timepoint).", 0, moves.length);
    }
    
    public void testGuessNextMovesWhenTBackward()
    {
        target.addMove(pd_0_3);  //(z=0, t=3)
        target.addMove(pd_0_2);  //(z=0, t=2)
        
        PlaneDef[] moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed two moves (last move was "+
                "two timepoints away from 0).", 2, moves.length);
        assertEquals("Guessed wrong move.", pd_0_1, moves[0]);
        assertEquals("Guessed wrong move.", pd_0_0, moves[1]);
        
        target.addMove(pd_0_1);  //(z=0, t=1)
        moves = target.guessNextMoves(1);
        assertEquals("Could only have guessed one move (last move was "+
                "second timepoint).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_0, moves[0]);
        
        moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed one move (last move was "+
                "second timepoint).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_0, moves[0]);
        
        target.addMove(pd_0_0);  //(z=0, t=0)
        moves = target.guessNextMoves(1);
        assertEquals("Couldn't have guessed any move (last move was "+
                "first timepoint).", 0, moves.length);
    }
    
    public void testGuessNextMovesWhenZUp()
    {
        target.addMove(pd_0_1);  //(z=0, t=1)
        target.addMove(pd_1_1);  //(z=1, t=1)
        
        PlaneDef[] moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed two moves (last move was "+
                "two z away from SIZE_Z).", 2, moves.length);
        assertEquals("Guessed wrong move.", pd_2_1, moves[0]);
        assertEquals("Guessed wrong move.", pd_3_1, moves[1]);
        
        target.addMove(pd_2_1);  //(z=2, t=1)
        moves = target.guessNextMoves(1);
        assertEquals("Could only have guessed one move (last move was "+
                "second last z).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_3_1, moves[0]);
        
        moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed one move (last move was "+
                "second last z).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_3_1, moves[0]);
        
        target.addMove(pd_3_1);  //(z=3, t=1)
        moves = target.guessNextMoves(1);
        assertEquals("Couldn't have guessed any move (last move was "+
                "last z).", 0, moves.length);
    }
    
    public void testGuessNextMovesWhenZDown()
    {
        target.addMove(pd_3_1);  //(z=3, t=1)
        target.addMove(pd_2_1);  //(z=2, t=1)
        
        PlaneDef[] moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed two moves (last move was "+
                "two z away from 0).", 2, moves.length);
        assertEquals("Guessed wrong move.", pd_1_1, moves[0]);
        assertEquals("Guessed wrong move.", pd_0_1, moves[1]);
        
        target.addMove(pd_1_1);  //(z=1, t=1)
        moves = target.guessNextMoves(1);
        assertEquals("Could only have guessed one move (last move was "+
                "second z).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_1, moves[0]);
        
        moves = target.guessNextMoves(2);
        assertEquals("Could only have guessed one move (last move was "+
                "second z).", 1, moves.length);
        assertEquals("Guessed wrong move.", pd_0_1, moves[0]);
        
        target.addMove(pd_0_1);  //(z=0, t=1)
        moves = target.guessNextMoves(1);
        assertEquals("Couldn't have guessed any move (last move was "+
                "first z).", 0, moves.length);
    }
    

}
