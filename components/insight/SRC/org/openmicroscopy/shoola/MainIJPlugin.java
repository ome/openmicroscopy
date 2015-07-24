/*
 * org.openmicroscopy.shoola.MainIJPlugin 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola;


//Java imports
import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;




//Third-party libraries
import ij.IJ;
import ij.ImageJ;
import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;




//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.PluginInfo;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.event.SaveEvent;
import org.openmicroscopy.shoola.env.init.StartupException;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Starts the application as an <code>ImageJ</code> plugin.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MainIJPlugin
implements PlugIn
{

    /** Minimum version of ImageJ required. */
    private static final String IJ_VERSION = "1.39u";

    /** The title of the splash screens. */
    private static final String TITLE = "Open Microscopy Environment";

    /** Reference to the container.*/
    private Container container;

    /** Builds the component indicating where to download the jar.*/
    private void showMessage(PluginInfo info)
    {
        JEditorPane htmlPane = new JEditorPane("text/html",
                formatMessage(info));
        htmlPane.setEditable(false);
        htmlPane.setOpaque(false);
        htmlPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(
                        e.getEventType()))
                    try {
                        BrowserLauncher.openURL(e.getURL().toString());
                    } catch (IOException exception) {
                        IJ.log(exception.toString());
                    }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(UIUtilities.buildComponentPanelCenter(htmlPane),
                BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton okay = new JButton("OK");
        panel.add(UIUtilities.buildComponentPanelCenter(okay),
                BorderLayout.SOUTH);

        final JDialog frame = new JDialog(IJ.getInstance(), "Warning");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        okay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        UIUtilities.centerAndShow(frame);
    }

    /** 
     * Builds the warning message and indicates where to download the jar.
     * 
     * @param dependencies The dependencies
     * @return See above.
     */
    private String formatMessage(PluginInfo info)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>");
        buffer.append("<p>The plugin requires ");
        buffer.append(info.formatDependencies());
        buffer.append("<br>Download the stable release version from<br>");
        String page = info.getInfo();
        buffer.append("<a href=\""+page+"\">");
        buffer.append(page);
        buffer.append("</a><br>");
        buffer.append("Add ");
        buffer.append(info.getFirstDependency());
        buffer.append(" to the ");
        buffer.append(info.getDirectory());
        buffer.append(" folder and restart ");
        buffer.append(info.getName());
        buffer.append(".</p>");
        buffer.append("</body><html>");
        return buffer.toString();
    }

    /** Notifies that <code>ImageJ</code> is closing.*/
    private void onImageJClosing()
    {
        if (container == null) return;
        try {
            DataServicesFactory.getInstance(container).shutdown(null);
        } catch (Exception e) {
            LogMessage msg = new LogMessage();
            msg.println("Exit Plugin:"+UIUtilities.printErrorText(e));
            if (IJ.debugMode) IJ.log(msg.toString());
            msg.close();
        }
    }

    /** Attaches listeners to the IJ instance.*/
    private void attachListeners()
    {
        ImageJ view = IJ.getInstance();
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onImageJClosing();
            }
        });
        if (view.getMenuBar() != null && view.getMenuBar().getMenuCount() > 0) {
            Menu menu = view.getMenuBar().getMenu(0);
            int count  = menu.getItemCount();
            if (count > 0) {
                MenuItem item = menu.getItem(count-1);
                //Add listener to the quit menu.
                item.addActionListener(new ActionListener() {

                    /** Make sure we shut down the server.*/
                    public void actionPerformed(ActionEvent arg0) {
                        onImageJClosing();
                    }
                });
            }
        }

        if (UIUtilities.isMacOS()) {
            try {
                MacOSMenuHandler handler = new MacOSMenuHandler(view);
                handler.initialize();
                view.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        String name = evt.getPropertyName();
                        if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(
                                name))
                            onImageJClosing();
                    }
                });
            } catch (Throwable e) {
                if (IJ.debugMode)
                    IJ.log("Cannot listen to the Quit action of the menu.");
            }
        }
    }

    /**
     * Runs the application as an <code>ImageJ</code> plugin.
     * @see PlugIn#run(String)
     */
    public void run(String args)
    {
        if (IJ.versionLessThan(IJ_VERSION)) {
            IJ.showMessage(TITLE,
                    "This plugin requires ImageJ\n"+IJ_VERSION+
                    "or later. Your version is "+IJ.getVersion()+
                    "; you will need to upgrade.");
            return;
        }
        String home = "";
        String configFile = null;
        int index = LookupNames.IMAGE_J;
        int save = -1;
        if (args != null) {
            String[] values = args.split(" ");
            List<String> l = new ArrayList<String>();
            for (int i = 0; i < values.length; i++) {
                String v = values[i];
                if (v.startsWith("imageJ")) {
                    String[] k = v.split("=");
                    if (k.length == 2) {
                        if (k[1].equals("import")) {
                            index = LookupNames.IMAGE_J_IMPORT;
                        } else if (k[1].equals("saveRoi")) {
                            save = SaveEvent.ROIS;
                        } else if (k[1].equals("saveResult")) {
                            save = SaveEvent.RESULTS;
                        }
                    }
                } else l.add(v);
            }
            if (l.size() > 0) configFile = l.get(0);
            if (l.size() > 1) home = l.get(1);
        }
        CodeSource src =
                MainIJPlugin.class.getProtectionDomain().getCodeSource();
        File jarFile;
        if (home.length() == 0) {
            try {
                jarFile = new File(src.getLocation().toURI().getPath());
                home = jarFile.getParentFile().getPath();
            } catch (Exception e) {}
        }
        try {
            container = Container.startupInPluginMode(home, configFile, index);
            if (save >=0) {
                container.getRegistry().getEventBus().post(
                        new SaveEvent(LookupNames.IMAGE_J, save));
            }
            attachListeners();
        } catch (StartupException e) {
            showMessage(e.getPlugin());
        }
    }

}
