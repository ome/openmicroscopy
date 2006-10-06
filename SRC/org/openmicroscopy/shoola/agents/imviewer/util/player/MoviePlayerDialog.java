/*
 * org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util.player;



//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * A non-modal dialog displaying the various controls to play a movie across
 * z-sections and timepoints.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class MoviePlayerDialog
    extends JDialog
{

    /** Reference to the component controlling the timer. */
    private MoviePlayer     player;
    
    /** The UI delegate. */
    private MoviePlayerUI   uiDelegate;
    
    /** Reference to the parent model. */
    private ImViewer        model;
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        getContentPane().add(uiDelegate);
        pack();
    }
    
    /** Adds a window listener to stop timer if the window is closed. */
    private void initListeners()
    {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            { 
               player.setPlayerState(Player.STOP);
            }});
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the this dialog.
     * @param model Reference to the {@link ImViewer}.
     *              Mustn't be <code>null</code>.
     */
    public MoviePlayerDialog(JFrame owner, ImViewer model)
    {
        super(owner);
        if (model == null) throw new NullPointerException("No model.");
        setTitle("Movie Player: "+model.getImageName());
        this.model = model;
        player = new MoviePlayer(model, this);
        uiDelegate = new MoviePlayerUI(player);
        new MoviePlayerControl(player, uiDelegate);
        initListeners();
        buildGUI();
    }

    /**
     * Swaps the <code>Play</code> and <code>Pause</code> icons depending on the 
     * specified flag.
     * 
     * @param b Pass <code>true</code> to set the <code>Pause</code> icon
     *          <code>false</code> to set the <code>Play</code> icon.
     */
    void setMoviePlay(boolean b)
    { 
        if (uiDelegate != null) uiDelegate.setMoviePlay(b); 
    }
    
    /** 
     * Fires an event to render the plane specified by the z-section and 
     * timepoint.
     */
    void renderImage()
    {
        int z = -1;
        int t = -1;
        switch (player.getMovieIndex()) {
            case MoviePlayer.ACROSS_T:
                z = model.getDefaultZ();
                t = player.getFrameNumberT();
                break;
            case MoviePlayer.ACROSS_Z:
                t = model.getDefaultT();
                z = player.getFrameNumberZ();
                break;
            case MoviePlayer.ACROSS_ZT:
                z = player.getFrameNumberZ();
                t = player.getFrameNumberT();
        }
        if (z == -1 || t == -1) return;
        model.setSelectedXYPlane(z, t);
    }

}
