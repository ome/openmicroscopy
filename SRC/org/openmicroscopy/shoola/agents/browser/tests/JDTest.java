/*
 * org.openmicroscopy.shoola.agents.browser.tests.JDTest
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

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version
 * @since
 */
public class JDTest
{
    public static void main(String[] args)
    {
        try
        {
          UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
        }
        catch(Exception e)
        { 
          System.out.println("Unable to load the Liquid L&F:");
          e.printStackTrace();
          System.out.println("Default L&F will be used.");
        }
        JFrame frame = new JDFrame();
        frame.show();
    }
}

class JDFrame extends JFrame
{
    public JDFrame()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setTitle("Your mom");
        
        JPanel desktopPanel = new JPanel();
        JDesktopPane pane = new JDesktopPane();
        pane.setSize(800,600);
        desktopPanel.setLayout(new BorderLayout());
        desktopPanel.add(pane,BorderLayout.CENTER);
        
        JInternalFrame frame = new JInternalFrame();
        JPanel internalPanel = new JPanel();
        internalPanel.add(new JButton("Hello"));
        frame.getContentPane().add(internalPanel);
        
        frame.setBounds(100,100,200,100);
        frame.setIconifiable(true);
        frame.setResizable(false);
        frame.setMaximizable(false);
        frame.setClosable(false);
        
        frame.show();
        
        pane.add(frame,new Integer(1));
        
        Container container = getContentPane();
        container.add(desktopPanel,"Center");
    }
}