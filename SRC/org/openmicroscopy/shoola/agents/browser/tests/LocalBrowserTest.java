/*
 * org.openmicroscopy.shoola.agents.browser.tests.LocalBrowserTest
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.tests;

import java.awt.Container;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.BrowserTopModel;
import org.openmicroscopy.shoola.agents.browser.BrowserView;
import org.openmicroscopy.shoola.agents.browser.ThumbnailSourceModel;
import org.openmicroscopy.shoola.agents.browser.layout.PlateLayoutMethod;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame;
import org.openmicroscopy.shoola.agents.browser.ui.PaletteFactory;
import org.openmicroscopy.shoola.agents.browser.ui.StatusBar;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class LocalBrowserTest
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setSize(800,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        LocalBrowserGUI gui = new LocalBrowserGUI();
        
        Container container = frame.getContentPane();
        container.add(gui);
        
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserManager manager = env.getBrowserManager();
        
        LocalTSMLoader loader = new LocalTSMLoader();
        ThumbnailSourceModel tsm = loader.getModel();
        
        BrowserTopModel model = new BrowserTopModel();
        
        BrowserModel bm = new BrowserModel();

        model.addPalette("Modes",PaletteFactory.getMainPalette(bm,model));
        model.addPalette("Overlays",PaletteFactory.getPaintModePalette(bm,model));
        bm.setLayoutMethod(new PlateLayoutMethod(8,12));
        BrowserView view = new BrowserView(bm,model);
        
        BrowserController controller = new BrowserController(bm,view);
        StatusBar bar = new StatusBar();
        controller.setStatusView(bar);
        
		BrowserInternalFrame bif = new BrowserInternalFrame(controller);
		gui.addBrowser(bif);
        controller.setDataModel(tsm);
        // controller.displayCurrentDataModel(); // wait until fixed to decomment
        
        frame.show();
    }
}