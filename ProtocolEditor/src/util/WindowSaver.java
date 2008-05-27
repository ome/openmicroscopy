 /*
 * util.WindowSaver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package util;

//Java imports

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * This code was taken and adapted from:
 * "Swing Hacks", by Joshua Marinacci and Chris Adamson. 
 * Copyright 2005 O'Reilly Media, Inc., 0-596-00907-0
 *
 * This class is a singleton that allows the Size and Position of windows
 * to be saved as configuration properties, so that they can be restored when
 * the same windows are opened again, in a later running of the application.
 * 
 * The windows (JFrames) must be given a unique name, using frame.setName().
 * 
 * This class is added as an AWTEventListener to the 
 * toolKit. On window open events, loadSettings() is called, to 
 * reposition and resize the window, according to any saved properties.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */

public class WindowSaver implements AWTEventListener {
    
	/**
	 * A reference to the unique instance of this class
	 */
    private static WindowSaver saver;
    
    /**
     * A map of all the windows that have their properties saved
     */
    private HashMap framemap;
    
    /**
     * private constructor for the singleton
     */
    private WindowSaver() {
        framemap = new HashMap();
    }
    
    /**
     * A method to get a reference to the unique instance of this class
     * 
     * @return	A reference to the unique instance of this class
     */
    public static WindowSaver getInstance() {
        if(saver == null) {
            saver = new WindowSaver();
        }
        return saver;
    }
    
    /**
     * A method of AWTEventListener interface, fired by window events.
     * Only Window_Opened events are used. 
     * If the event source is a JFrame, loadsettings() is called for 
     * that frame. 
     */
    public void eventDispatched(AWTEvent evt) {
        try {
            if(evt.getID() == WindowEvent.WINDOW_OPENED) {
                ComponentEvent cev = (ComponentEvent)evt;
                if(cev.getComponent() instanceof JFrame) {
                    // p("event: " + evt);
                    JFrame frame = (JFrame)cev.getComponent();
                    loadSettings(frame);
                }
            }
        }catch(Exception ex) {
            p(ex.toString());
        }
    }
    
    /**
     * This is called when a Window (frame) opens (just before displaying). 
     * Configuration properties are loaded (if they exist) and are 
     * used to resize and reposition the window. 
     * If they do not exist, the frame is not moved. 
     * 
     * The parameters are finally added to the frame map. 
     * 
     * @param frame
     * @throws IOException
     */
    public static void loadSettings(JFrame frame) throws IOException {
    	
        String name = frame.getName();
        
        /*
         * Ignore frames that have not had a name set (ie default "frame..")
         */
        if (name.startsWith("frame")) {
        	return;
        }
        
        int defaultX = frame.getX();
        int defaultY = frame.getY();
        int defaultW = frame.getWidth();
        int defaultH = frame.getHeight();
        
        int x = getInt(name+".x",defaultX); 
        int y = getInt(name+".y",defaultY);
        int w = getInt(name+".w",defaultW);
        int h = getInt(name+".h",defaultH);
        
        frame.setLocation(x,y);
        frame.setSize(new Dimension(w,h));
        getInstance().framemap.put(name,frame);
        frame.validate();
    }
    
    /**
     * A handy method for getting a property from the Properties object.
     * Returns a default value if the property value is null (or not an int).
     * 
     * @param props		The Properties object
     * @param name		The name of the property to retrieve
     * @param value		The default value
     * 
     * @return		The integer value of the property
     */
    public static int getInt(String name, int value) {
        String v = PreferencesManager.getPreference(name);
        System.out.println("getInt: " + name + " = " + v);
        if(v == null) {
            return value;
        }
        try {
        	return Integer.parseInt(v);
        } catch (Exception ex) {
        	return value;
        }
    }
    
    /**
     * saveSettings() is called when the application shuts down. 
     * It saves the frame map as configuration properties. 
     * 
     * @throws IOException
     */
    public static void saveSettings() {
        
        Iterator it = getInstance().framemap.keySet().iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            JFrame frame = (JFrame)getInstance().framemap.get(name);
            
            /*
             * Ignore frames that have not had a name set (ie default "frame..")
             */
            if (name.startsWith("frame")) {
            	continue;
            }
            
            PreferencesManager.setPreference(name+".x",""+frame.getX());
            PreferencesManager.setPreference(name+".y",""+frame.getY());
            PreferencesManager.setPreference(name+".w",""+frame.getWidth());
            PreferencesManager.setPreference(name+".h",""+frame.getHeight());
        }
    }
    
    public static void p(String str) {
        System.out.println(str);
    }


    /*
     * main method for testing etc.
     */
    /*
    public static void main(String[] args) throws Exception {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.addAWTEventListener(WindowSaver.getInstance(),
        			AWTEvent.WINDOW_EVENT_MASK);
        
        final JFrame frame = new JFrame("Hack X");
        frame.setName("WSTes.main");
        frame.getContentPane().add(new JButton("a button"));
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    WindowSaver.saveSettings();
                    System.exit(0);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        });
        mb.add(menu);
        frame.setJMenuBar(mb);
        frame.pack();
        frame.show();
    }
    */
    
}

