/*
 * org.openmicroscopy.shoola.agents.browser.ui.StatusBar
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
 
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.BrowserModelListener;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Shows the status bar for the view.  General messages like the name of the
 * dataset, the name/number of images selected, changes to the modes in the
 * system, and more, should go here.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class StatusBar extends JPanel
                             implements BrowserModelListener
{
    private JLabel leftLabel;
    private JLabel rightLabel;
    
    private Timer temporaryTimer;
    private TimerTask currentTask;
    
    private String persistentLeftString;
    private Font persistentLeftFont;
    private Color persistentLeftColor;
    
    private String persistentRightString;
    private Font persistentRightFont;
    private Color persistentRightColor;
    
    private final Font defaultFont = new Font(null,Font.PLAIN,12);
    private final Color defaultColor = Color.black;
    private final Color modeChangeColor = Color.blue;
    private final Font modeChangeFont = new Font(null,Font.BOLD,12);
    
    /**
     * Creates a StatusBar.
     */
    public StatusBar()
    {
        leftLabel = new JLabel();
        rightLabel = new JLabel();
        
        persistentLeftFont = defaultFont;
        persistentRightFont = defaultFont;
        persistentLeftColor = defaultColor;
        persistentRightColor = defaultColor;
        
        setLayout(new GridLayout(1,2,4,4));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        leftPanel.add(leftLabel);
        rightPanel.add(rightLabel);
        
        revertToFontDefaults();
        
        add(leftPanel);
        add(rightPanel);
        
        temporaryTimer = new Timer();
    }
    
    /**
     * Sets the (persistent) text on the left side to the specified value.
     * @param text The message to display.
     */
    public void setLeftText(String text)
    {
        if(text == null)
        {
            text = "";
        }
        leftLabel.setText(text);
        persistentLeftString = text;
    }
    
    /**
     * Sets the (persistent) text on the right side to the specified value.
     * @param text
     */
    public void setRightText(String text)
    {
        if(text == null)
        {
            text = "";
        }
        rightLabel.setText(text);
        persistentRightString = text;   
    }
    
    /**
     * Sets the (persistent) font on the left side to the specified value.
     * @param font
     */
    public void setLeftFont(Font font)
    {
        if(font != null)
        {
            persistentLeftFont = font;
            leftLabel.setFont(persistentLeftFont);
        }
    }
    
    /**
     * Sets the (persistent) font on the right side to the specified value.
     * @param font
     */
    public void setRightFont(Font font)
    {
        if(font != null)
        {
            persistentRightFont = font;
            rightLabel.setFont(persistentRightFont);
        }
    }
    
    /**
     * Sets the left (persistent) color to the specified hue.
     * @param color The color to set.
     */
    public void setLeftColor(Color color)
    {
        if(color != null)
        {
            persistentLeftColor = color;
            leftLabel.setForeground(persistentLeftColor);
        }
    }
    
    /**
     * Sets the right (persistent) color to the specified hue.
     * @param color The color to set.
     */
    public void setRightColor(Color color)
    {
        if(color != null)
        {
            persistentRightColor = color;
            rightLabel.setForeground(persistentRightColor);
        }
    }
    
    /**
     * Reverts to the default font.
     */
    public void revertToFontDefaults()
    {
        setLeftFont(defaultFont);
        setRightFont(defaultFont);
        setLeftColor(defaultColor);
        setRightColor(defaultColor);
    }
    
    /**
     * Do nothing for now unless a major UI mode change has occurred.
     * Otherwise, display an appropriate status message and revert after 3
     * seconds.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modeChanged(java.lang.String, org.openmicroscopy.shoola.agents.browser.BrowserMode)
     */
    public void modeChanged(String className, BrowserMode mode)
    {
        TimerTask revertTask = getLeftRevertTask();
        
        if(className.equals(BrowserModel.MAJOR_UI_MODE_NAME))
        {
            leftLabel.setForeground(modeChangeColor);
            leftLabel.setFont(modeChangeFont);
            
            if(mode.equals(BrowserMode.DEFAULT_MODE))
            {
                leftLabel.setText("Default mode selected.");
            }
            else if(mode.equals(BrowserMode.ANNOTATE_MODE))
            {
                leftLabel.setText("Annotation mode selected.");
            }
            else if(mode.equals(BrowserMode.CLASSIFY_MODE))
            {
                leftLabel.setText("Classification mode selected.");
            }
            else if(mode.equals(BrowserMode.GRAPH_MODE))
            {
                leftLabel.setText("Graph mode selected.");
            }
        }
        temporaryTimer.cancel();
        temporaryTimer = new Timer();
        temporaryTimer.schedule(revertTask,3000);
    }
    
    /**
     * Does nothing.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modelUpdated()
     */
    public void modelUpdated()
    {
        // do nothing
    }
    
    /**
     * Does nothing.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#paintMethodsChanged()
     */
    public void paintMethodsChanged()
    {
       // do nothing
    }
    
    /**
     * Does nothing.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailAdded(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailAdded(Thumbnail t)
    {
        // do nothing
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailRemoved(org.openmicroscopy.shoola.agents.browser.images.Thumbnail)
     */
    public void thumbnailRemoved(Thumbnail t)
    {
        // TODO: get name of thumbnail, say that it's been removed.
    }
    
    /**
     * Does nothing.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsDeselected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsDeselected(Thumbnail[] thumbnails)
    {
        // do nothing
    }
        
    /**
     * Displays the message that a certain number of thumbnails have been
     * selected.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#thumbnailsSelected(org.openmicroscopy.shoola.agents.browser.images.Thumbnail[])
     */
    public void thumbnailsSelected(Thumbnail[] thumbnails)
    {
        leftLabel.setText(String.valueOf(thumbnails.length) +
                          " images selected.");
                          
        TimerTask revertTask = getLeftRevertTask();
        temporaryTimer.cancel();
        temporaryTimer = new Timer();
        temporaryTimer.schedule(revertTask,3000);
    }

    
    // returns a revert task, commonly used to replace temporary messages.
    private TimerTask getLeftRevertTask()
    {
        TimerTask task = new TimerTask() {
            /* (non-Javadoc)
             * @see java.util.TimerTask#run()
             */
            public void run()
            {
                leftLabel.setForeground(persistentLeftColor);
                leftLabel.setFont(persistentLeftFont);
                leftLabel.setText(persistentLeftString);
            }

        };
        
        return task;
    }
    
    // returns a revert task for the right label.
    private TimerTask getRightRevertTask()
    {
        TimerTask task = new TimerTask()
        {
            /* (non-Javadoc)
             * @see java.util.TimerTask#run()
             */
            public void run()
            {
                rightLabel.setForeground(persistentRightColor);
                rightLabel.setFont(persistentRightFont);
                rightLabel.setText(persistentRightString);
            }

        };
        return task;
    }

}
