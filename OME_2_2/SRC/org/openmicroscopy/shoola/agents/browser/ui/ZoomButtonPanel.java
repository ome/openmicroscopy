/*
 * org.openmicroscopy.shoola.agents.browser.ZoomButtonPanel
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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;

/**
 * Contains the controls for a zoom panel (extracted out of the BIF code to
 * make it more portable)
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ZoomButtonPanel extends JPanel
{
    private Timer currentTimer;
    private TimerTask currentTimerTask;
    private IconManager manager;
    
    private JTextField zoomTextField;
    
    private double zoomLevel;
    private double minZoomLevel;
    private double maxZoomLevel = 2.0;
    private boolean running = false;
    private NumberFormat percentFormat = NumberFormat.getPercentInstance();
    
    private BrowserView view;
    
    public ZoomButtonPanel(BrowserView embeddedView)
    {
        currentTimer = new Timer();
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        manager = env.getIconManager();
        
        JButton zoomOutButton =
            new JButton(manager.getSmallIcon(IconManager.ZOOM_OUT));

        // adjust percent format to not display fractional zoom
        percentFormat.setMaximumFractionDigits(0);
        this.view = embeddedView;
            
        zoomOutButton.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent me)
            {
                running = true;
                view.setZoomToScale(false);
                currentTimerTask = new TimerTask()
                {
                    public void run()
                    {
                        if(!running) running = true;
                        if(zoomLevel == minZoomLevel)
                        {
                            running = false;
                            cancel();
                        }
                        else if(zoomLevel - 0.04 > minZoomLevel)
                        {
                            zoomLevel -= 0.04;
                            view.setZoomLevel(zoomLevel);
                        }
                        else
                        {
                            running = false;
                            zoomLevel = minZoomLevel;
                            view.setZoomLevel(zoomLevel);
                            cancel();
                        }
                    }
                };
                
                currentTimer.schedule(currentTimerTask,200,50);
            }
            
            public void mouseReleased(MouseEvent me)
            {
                if(running)
                {
                    currentTimerTask.cancel();
                    running = false;
                } 
            }
            
            public void mouseClicked(MouseEvent me)
            {
                if(zoomLevel - 0.04 > minZoomLevel)
                {
                    zoomLevel -= 0.04;
                    view.setZoomLevel(zoomLevel);
                }
                else
                {
                    zoomLevel = minZoomLevel;
                    view.setZoomLevel(zoomLevel);
                }
            }
        });
        
        zoomTextField = new JTextField(5);
        zoomTextField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                getZoomFromText();
            }
        });
        
        zoomTextField.addFocusListener(new FocusAdapter()
        {
            public void focusLost(FocusEvent fe)
            {
                getZoomFromText();
            }
        });
        
        JButton zoomInButton =
            new JButton(manager.getSmallIcon(IconManager.ZOOM_IN));
        
        zoomInButton.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent me)
            {
                running = true;
                view.setZoomToScale(false);
                currentTimerTask = new TimerTask()
                {
                    public void run()
                    {
                        if(!running) running = true;
                        if(zoomLevel == maxZoomLevel)
                        {
                            running = false;
                            cancel();
                        }
                        else if(zoomLevel + 0.04 < maxZoomLevel)
                        {
                            zoomLevel += 0.04;
                            view.setZoomLevel(zoomLevel);
                            view.repaint();
                        }
                        else
                        {
                            running = false;
                            zoomLevel = maxZoomLevel;
                            view.setZoomLevel(zoomLevel);
                            view.repaint();
                            cancel();
                        }
                    }
                };
                currentTimer.schedule(currentTimerTask,200,50);
            }
            
            
            public void mouseReleased(MouseEvent me)
            {
                if(running) 
                {
                    running = false;
                    currentTimerTask.cancel();
                } 
            }
            
            public void mouseClicked(MouseEvent me)
            {
                if(zoomLevel + 0.04 < maxZoomLevel)
                {
                    zoomLevel += 0.04;
                    view.setZoomLevel(zoomLevel);
                }
                else
                {
                    zoomLevel = maxZoomLevel;
                    view.setZoomLevel(zoomLevel);
                }
            }
        });
        
        JButton zoomFitButton =
            new JButton(manager.getSmallIcon(IconManager.ZOOM_FIT));
        
        zoomFitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                view.setZoomToScale(true);
            }
        });
        
        view.addZoomParamListener(new ZoomParamListener()
        {
            public void minZoomLevelChanged(double level)
            {
                minZoomLevel = level;
            }
            
            public void maxZoomLevelChanged(double level)
            {
                maxZoomLevel = level;
            }
            
            public void zoomLevelChanged(double level)
            {
                if(!running) {
                    zoomLevel = level;
                }
                zoomTextField.setText(percentFormat.format(level));
            }
        });
        
        setLayout(new FlowLayout());
        add(zoomOutButton);
        add(zoomTextField);
        add(zoomInButton);
        add(zoomFitButton);
        zoomOutButton.setToolTipText("Zoom Out");
        zoomInButton.setToolTipText("Zoom In");
        zoomFitButton.setToolTipText("Zoom to Fit");
    }
    
    private void getZoomFromText()
    {
        try
        {
            double desiredLevel =
                percentFormat.parse(zoomTextField.getText()).doubleValue();
                
            if(desiredLevel > maxZoomLevel)
            {
                view.setZoomToScale(false);
                view.setZoomLevel(maxZoomLevel);
            }
            else if(desiredLevel < minZoomLevel)
            {
                view.setZoomToScale(false);
                view.setZoomLevel(minZoomLevel);
            }
            else
            {
                view.setZoomToScale(false);
                view.setZoomLevel(desiredLevel);
            }
        }
        catch(ParseException e)
        {
            zoomTextField.setText(percentFormat.format(zoomLevel));
            zoomTextField.selectAll();
        }
    }
}
