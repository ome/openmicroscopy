/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.PreviewControlBar 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.rnd;

//Java imports
import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;


//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

/**
 * The lower the controls bar
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
class PreviewControlBar2 extends JPanel {

    /** Space between buttons. */
    static final int SPACE = 3;

    /** Reference to the control. */
    private RendererControl control;

    /**
     * Formats the specified button.
     * 
     * @param b
     *            The button to handle.
     */
    private void formatButton(AbstractButton b) {
        b.setVerticalTextPosition(AbstractButton.BOTTOM);
        b.setHorizontalTextPosition(AbstractButton.CENTER);
        b.setIconTextGap(0);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.setBackground(UIUtilities.BACKGROUND_COLOR);
    }

    /**
     * Returns the tool bar.
     * 
     * @return See above.
     */
    private JToolBar buildToolBar() {
        JToolBar bar = new JToolBar();
        bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        bar.setBorder(null);
        bar.setRollover(true);
        bar.setFloatable(false);

        JButton b = new JButton(control.getAction(RendererControl.RND_MIN_MAX));
        formatButton(b);
        bar.add(b);
        bar.add(Box.createHorizontalStrut(SPACE));

        Action fullRangeAction = control.getAction(RendererControl.RND_ABSOLUTE_MIN_MAX);
        fullRangeAction.setEnabled(control.isIntegerPixelData());
        b = new JButton(fullRangeAction);
        formatButton(b);
        bar.add(b);
        bar.add(Box.createHorizontalStrut(SPACE));
       
        b = new JButton(control.getAction(RendererControl.RND_RESET));
        formatButton(b);
        bar.add(b);

        return bar;
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(UIUtilities.BACKGROUND_COLOR);
        add(buildToolBar());
    }

    /**
     * Creates a new instance.
     * 
     * @param control
     *            Reference to the control.
     * @param model
     *            Reference to the model.
     */
    PreviewControlBar2(RendererControl control) {
        this.control = control;
        buildGUI();
    }

}
