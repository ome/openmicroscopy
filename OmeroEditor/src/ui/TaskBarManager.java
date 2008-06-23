/*
 * org.openmicroscopy.shoola.env.ui.TaskBarManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package ui;

//Java imports
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Third-party libraries

//Application-internal dependencies

import omeroCal.model.DBConnectionSingleton;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.MacOSMenuHandler;
import util.WindowSaver;
import xmlMVC.StartupShutdown;

/** 
 * Creates and manages the {@link TaskBarView}.
 * It acts as a controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 5112 $ $Date: 2007-10-29 09:31:15 +0000 (Mon, 29 Oct 2007) $)
 * </small>
 * @since OME2.2
 */
class TaskBarManager
	implements PropertyChangeListener
{

	/** The name of the about file in the config directory. */
	private static final String		ABOUT_FILE = "about.xml";
	
	/** The name of the documentation file in the docs directory. */
	private static final String		DOC_FILE = "insight_index.html";
	
	/** The value of the tag to find. */
	private static final String		A_TAG = "a";
	
	/** Array of supported browsers. */
	private static final String[]	BROWSERS_UNIX;
	
	static {
		BROWSERS_UNIX = new String[6];
		BROWSERS_UNIX[0] = "firefox";
		BROWSERS_UNIX[1] = "opera";
		BROWSERS_UNIX[2] = "konqueror";
		BROWSERS_UNIX[3] = "epiphany";
		BROWSERS_UNIX[4] = "mozilla";
		BROWSERS_UNIX[5] = "netscape";
	}
	

	/** The view this controller is managing. */
	// private TaskBarView				view; 			//JFrame ?
	private JFrame view;
	
	/** Reference to the container. */
//	private Container				container;

	
	/**
	 * Reads the content of the specified file and returns it as a string.
	 * 
	 * @param file	Absolute pathname to the file.
	 * @return See above.
	 */
	private String loadAbout(String file)
	{
		String message;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			StringBuffer buffer = new StringBuffer();
			int number = 0;
			String line;
			while (true) {
                line = in.readLine();
                if (line == null) break;
                //buffer.append("\n");
                if (number != 0) buffer.append(line);
                number++;
            }
			in.close();
            message = buffer.toString();
		} catch (Exception e) {
			message = "Error: About information not found";
		}
		return message;
	}
	
	
	
	/**
	 * The exit action.
	 * Just forwards to the container.
	 */
	private void doExit()
    { 
       // IconManager icons = IconManager.getInstance(container.getRegistry());
      
		/*
        JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		List agents = (List) container.getRegistry().lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		Agent a;
		//Agents termination phase.
		i = agents.iterator();
		Map m;
		List<SaveEventBox> boxes = null;
		SaveEventBox box;
		Iterator k, v;
		String key;
		JPanel item;
		Set values;
		Map<Agent, List> results = new HashMap<Agent, List>();
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			a = agentInfo.getAgent();
			m = a.hasDataToSave();
			if (m != null && m.size() > 0) {
				boxes = new ArrayList<SaveEventBox>();
				k = m.keySet().iterator();
				while (k.hasNext()) {
					key = (String) k.next();
					item = new JPanel();
					item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
					p.add(UIUtilities.setTextFont(key));
					p.add(item);
					values = (Set) m.get(key);
					v = values.iterator();
					while (v.hasNext()) {
						box = new SaveEventBox((RequestEvent) v.next());
						boxes.add(box);
						item.add(box);
					}
				}
				if (boxes != null && boxes.size() != 0) results.put(a, boxes);
			}
		}
		
		MessageBox msg;
		if (results.size() != 0) {
			EventBus bus = container.getRegistry().getEventBus();
			msg = new MessageBox(view, "Exit application", 
        			"Before closing the application, do you want to save" +
        			" data from : ", icons.getIcon(IconManager.QUESTION));
			msg.addCancelButton();
			msg.addBodyComponent(p);
			exitResponses = new HashMap<Agent, Integer>();
			switch (msg.centerMsgBox()) {
				case MessageBox.YES_OPTION:
					i = results.keySet().iterator();
					Integer number;
					while (i.hasNext()) {
						a = (Agent) i.next();
						boxes = results.get(a);
						k = boxes.iterator();
						while (k.hasNext()) {
							box = (SaveEventBox) k.next();
							if (box.isSelected()) {
								bus.post(box.getEvent());
								number = exitResponses.get(a);
								System.err.println("Agent: "+a);
								if (number == null) {
									exitResponses.put(a, new Integer(1));
								} else {
									number = new Integer(number.intValue()+1);
								}
							}
						}
					}
					//container.exit();
					break;
				case MessageBox.NO_OPTION:
					container.exit();
					break;
				case MessageBox.CANCEL:
					break;
			}
		} else {
			msg = new MessageBox(view, "Exit application", 
        			"Do you really want to close the application?", 
        			icons.getIcon(IconManager.QUESTION));
			if (msg.centerMsgBox() == MessageBox.YES_OPTION)
				container.exit();
		}
		*/
		
		int n = JOptionPane.showConfirmDialog(null, "Really Quit?");
		if (n == JOptionPane.YES_OPTION) {
			
			// save settings etc..
			StartupShutdown.prepareForSystemExit();
			
			System.exit(0);

		}
		
       /* MessageBox msg;
        msg = new MessageBox(view, "Exit application", 
    			"Do you really want to close the application?", 
    			icons.getIcon(IconManager.QUESTION));
		if (msg.centerMsgBox() == MessageBox.YES_OPTION)
			container.exit();
		*/
    }
	
	
	/**
	 * Attaches the {@link #doExit() exit} action to all exit buttons and
	 * fires {@link #synchConnectionButtons()} when the window is first open.
	 */
	private void attachOpenExitListeners()
	{
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { doExit(); }
			
		});
		/*
		ActionListener exit = new ActionListener() {		
			public void actionPerformed(ActionEvent ae) { doExit(); }
		};
		*/
		//view.getButton(TaskBarView.EXIT_MI).addActionListener(exit);
		//view.getButton(TaskBarView.EXIT_BTN).addActionListener(exit);
	}
	
	/**
	 * Registers the necessary listeners with the view and also registers with
	 * the event bus.
	 */
	private void attachListeners()
	{
		attachOpenExitListeners();
        String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			try {
				MacOSMenuHandler handler = new MacOSMenuHandler(view);
				handler.initialize();
		    	view.addPropertyChangeListener(this);
			} catch (Throwable t) {
				// TODO: handle exception
			}
        }
     }
	
	/**
	 * Creates this controller along with its view and registers the necessary
	 * listeners with the view.
	 *  
	 * @param c	Reference to the container.
	 */
//	TaskBarManager(Container c) 
	TaskBarManager(JFrame frame)
	{
		//container = c;
		//view = new TaskBarView(IconManager.getInstance(c.getRegistry()));
		view = frame;
		attachListeners();												
	}
	
	/**
	 * Returns the view component.
	 * 
	 * @return	See above.
	 */
	//TaskBar getView() { return view; }

	/**
	 * Reacts to property change fired by the <code>SoftwareUpdateDialog</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(name)) {
			//Registry reg = container.getRegistry();;
			//Object exp = reg.lookup(LookupNames.CURRENT_USER_DETAILS);
			//if (exp == null) container.exit(); //not connected
			//else
				doExit();
		}
	}

}
