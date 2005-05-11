/*
 * org.openmicroscopy.shoola.util.ui.TitlePanel
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries

//Application-internal dependencies

/** 
 * A general purpose title panel having a title, sub-title, explanatory text,
 * and graphics.
 * The title, sub-title, and explanatory text are aligned to the left in three
 * horizontal rows and take up as much width as is available.  The grahics is
 * aligned to the right and spawns all three rows.  The title is displayed in
 * a bold font, the sub-title in a normal font, and the explanatory text in an
 * italic font.  The title and sub-title are displayed in a single line label,
 * as the explanatory text is embedded in a multi-line label.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TitlePanel
    extends JPanel
{
    
    /** Default background color. */
    private static final Color  DEFAULT_BG = Color.WHITE;
    
    
    /** The component embedding the panel's title. */
    private JComponent  title;
    
    /** The component embedding the panel's sub-title. */
    private JComponent  subTitle;
    
    /** The component embedding the panel's text. */
    private JComponent  text;
    
    /** The component embedding the panel's graphics. */
    private JComponent  graphx;
    
    /** The horizontal line at the bottom of the panel. */
    private JSeparator  hLine;
    
    
    /**
     * Sets up all sub-components.
     */
    private void buildGUI()
    {
        add(title);
        add(subTitle);
        add(text);
        add(graphx);
        add(hLine);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setLayout(new TitlePanelLayout());
        
        Font f = title.getFont();
        title.setFont(f.deriveFont(Font.BOLD));
        f = subTitle.getFont();
        subTitle.setFont(f.deriveFont(Font.PLAIN));
        f = text.getFont();
        text.setFont(f.deriveFont(Font.ITALIC));
        
        setBackground(DEFAULT_BG);
    }
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param text The explanatory text.
     * @param graphx The component embedding the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, String text, 
                      JComponent graphx)
    {
        this.title = new JLabel(title);
        this.subTitle = new JLabel(subTitle);
        this.text = new MultilineLabel(text);
        this.graphx = (graphx == null ? new JLabel() : graphx);
        hLine = new JSeparator();
        buildGUI();
    }
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param graphx The component embedding the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, JComponent graphx)
    {
        this(title, subTitle, null, graphx);
    }
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param text The explanatory text.
     * @param icon An icon to use as the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, String text, Icon icon)
    {
        this(title, subTitle, text, new JLabel(icon));
    }
    
    /**
     * Creates a new instance.
     * All arguments are optional.
     * 
     * @param title The panel's title.
     * @param subTitle The panel's sub-title.
     * @param icon An icon to use as the panel's graphics.
     */
    public TitlePanel(String title, String subTitle, Icon icon)
    {
        this(title, subTitle, null, new JLabel(icon));
    }
    
    
    /**
     * Lays out the sub-components of this title panel. 
     */
    private class TitlePanelLayout
        implements LayoutManager
    {

        /** Horizontal gap between the text components and the graphics. */
        private static final int    H_GAP = 20;
        
        /** Left indent for the sub-title and text. */
        private static final int    INDENT = 10;
        
        
        /** Lays out the horizontal line. */
        private void layoutHLine()
        {
            int w = getWidth(), h = getHeight();
            Insets i = getInsets();
            Dimension d = hLine.getPreferredSize();
            hLine.setBounds(i.left, h-d.height-i.bottom, 
                            w-i.left-i.right, d.height);
        }
        
        /** 
         * Lays out the graphics component.
         * Assumes the horizontal line has already been laid out. 
         */
        private void layoutGraphx()
        {
            int w = getWidth();
            Insets i = getInsets();
            Dimension d = graphx.getPreferredSize();
            int width = (d.width < w-H_GAP ? d.width : 0);
            graphx.setBounds(w-i.right-width, i.top, width, d.height);
        }
        
        /**
         * Lays out the title component.
         * Assumes the horizontal line and the graphics components have already
         * been laid out.
         */
        private void layoutTitle()
        {
            int w = getWidth();
            Insets i = getInsets();
            Dimension d = title.getPreferredSize();
            title.setBounds(i.left, i.top, 
                            w-i.left-i.right-H_GAP-graphx.getWidth(), d.height);
        }
        
        /**
         * Lays out the sub-title component.
         * Assumes the horizontal line, the graphics, and the title components
         * have already been laid out.
         */
        private void layoutSubTitle()
        {
            Insets i = getInsets();
            Dimension d = subTitle.getPreferredSize();
            subTitle.setBounds(i.left+INDENT, i.top+title.getHeight(), 
                               title.getWidth()-INDENT, d.height);
        }
        
        /**
         * Lays out the text component.
         * Assumes the all the other components have already been laid out.
         */
        private void layoutText()
        {
            Insets i = getInsets();
            int h = getHeight(), 
                y = i.top+title.getHeight()+subTitle.getHeight();
            text.setBounds(i.left+INDENT, y, 
                           title.getWidth()-INDENT, 
                           h-y-hLine.getHeight()-i.bottom);
        }
        
        /** Returns the preferred layout space. */
        public Dimension minimumLayoutSize(Container c) 
        {
            return preferredLayoutSize(c);
        }
        
        /**
         * Returns the amount of space needed to layout all components at
         * their preferred size.
         */
        public Dimension preferredLayoutSize(Container c)  
        {
            Insets i = getInsets();
            Dimension titleD = title.getPreferredSize(), 
                      subTitleD = subTitle.getPreferredSize(),
                      textD = text.getPreferredSize(),
                      graphxD = graphx.getPreferredSize(),
                      hLineD = hLine.getPreferredSize();
            int  w = i.left+i.right+H_GAP+graphxD.width+
                     Math.max(titleD.width, subTitleD.width),
                 h = i.top+i.bottom+hLineD.height+
                     Math.max(titleD.height+subTitleD.height+textD.height, 
                              graphxD.height);
            return new Dimension(w, h);
        }
        
        /** Lays out the components. */
        public void layoutContainer(Container c) 
        {
            layoutHLine();
            layoutGraphx();
            layoutTitle();
            layoutSubTitle();
            layoutText();
        }
        
        /**
         * No-op implementation.
         * Required by {@link LayoutManager}, but not needed here.
         */
        public void addLayoutComponent(String name, Component c) {}
        
        /**
         * No-op implementation.
         * Required by {@link LayoutManager}, but not needed here.
         */
        public void removeLayoutComponent(Component c) {} 
        
    }
	
}
