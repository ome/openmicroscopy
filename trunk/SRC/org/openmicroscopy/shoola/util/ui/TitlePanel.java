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
import java.awt.Rectangle;
import java.awt.geom.Area;
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
    private JComponent      title;
    
    /** The component embedding the panel's sub-title. */
    private JComponent      subTitle;
    
    /** The component embedding the panel's text. */
    private JComponent      text;
    
    /** The horizontal line at the bottom of the panel. */
    private JSeparator      hLine;
    
    /** The component embedding the panel's graphics. */
    protected JComponent    graphx;
    
    /** Builds and lays out the GUI. */
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
        setOpaque(true);
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
     * Replaces the {@link #graphx} by the specified component.
     * 
     * @param c The component to set as icon.
     */
    public void setIconComponent(JComponent c)
    {
        if (c == null)
            throw new IllegalArgumentException("No component no set.");
        remove(graphx);
        graphx = c;
        add(graphx);
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
        
        /**
         * The area that is currently available to lay out a component.
         * When the layout starts, this is set to be the title panel minus
         * its border.  Every time a component is laid out, these bounds are
         * updated to subtract the area occupied by that component.
         */
        private Rectangle   curLayoutBounds;
         
        /**
         * Utility method to subtract <code>r</code> to the 
         * {@link #curLayoutBounds}.
         *
         * @param r The rectangle to subtract.
         */
        private void updateLayoutBounds(Rectangle r)
        {
            Area a = new Area(curLayoutBounds), b = new Area(r);
            a.subtract(b);
            curLayoutBounds = a.getBounds();
        }
        
        /**
         * Tells whether we should layout the given component.
         * As a convenience, we set the component to occupy no space (thus
         * effectively hiding) if it shouldn't be laid out.
         * 
         * @param c The component to layout.
         * @return <code>true</code> if the component should be laid out,
         *         <code>false</code> otherwise.
         */
        private boolean shouldLayout(JComponent c)
        {
            if (curLayoutBounds.width*curLayoutBounds.height <= 0) {
                c.setBounds(0, 0, 0, 0);
                return false;
            }
            return true;
        }
        
        /**
         * Utility method to calculate the actual layout height of a component.
         * 
         * @param preferredHeight The component's preferred height.
         * @return The actual height to use for layout.
         */
        private int getActualHeight(int preferredHeight)
        {
            return (preferredHeight < curLayoutBounds.height ? 
                    preferredHeight : curLayoutBounds.height);
        }
        
        /** 
         * Lays out the horizontal line.
         * This separator is placed at the very bottom of the panel on top
         * of the space allocated for the border.  We force it to fit into
         * the bottom insets of the border. 
         */
        private void layoutHLine()
        {
            int h = getHeight();
            Insets i = getInsets();
            Dimension d = hLine.getPreferredSize();
            int height = (d.height < i.bottom ? d.height : i.bottom-1);
            hLine.setBounds(0, h-height, getWidth(), height);
        }
        
        /** Lays out the graphics component. */
        private void layoutGraphx()
        {
            if (!shouldLayout(graphx)) return;
            Dimension d = graphx.getPreferredSize();
            Rectangle r;
            if (d.width+H_GAP < curLayoutBounds.width) {  //Can fit.
                r = new Rectangle(curLayoutBounds.x+
                                    curLayoutBounds.width-d.width-H_GAP,
                                  curLayoutBounds.y, 
                                  d.width+H_GAP, curLayoutBounds.height);
                graphx.setBounds(curLayoutBounds.x+
                                    curLayoutBounds.width-d.width, 
                                 curLayoutBounds.y,
                                 d.width, getActualHeight(d.height));
            } else {  //Hide.
                r = new Rectangle(0, 0, 0 , 0);
                graphx.setBounds(r);
            }
            updateLayoutBounds(r);
        }
        
        /** Lays out the title component. */
        private void layoutTitle()
        {
            if (!shouldLayout(title)) return;
            int height = getActualHeight(title.getPreferredSize().height);
            title.setBounds(curLayoutBounds.x, curLayoutBounds.y, 
                            curLayoutBounds.width, height);
            updateLayoutBounds(title.getBounds());
        }
        
        /** Lays out the sub-title component. */
        private void layoutSubTitle()
        {
            int height = getActualHeight(subTitle.getPreferredSize().height);
            Rectangle r = new Rectangle(curLayoutBounds.x, curLayoutBounds.y,
                                        curLayoutBounds.width, height);
            subTitle.setBounds(r.x+INDENT, r.y, r.width-INDENT, r.height);
            updateLayoutBounds(r);
        }
        
        /** Lays out the text component. */
        private void layoutText()
        {
            Rectangle r = new Rectangle(curLayoutBounds);
            text.setBounds(r.x+INDENT, r.y, r.width-INDENT, r.height);
            updateLayoutBounds(r);
        }
        
        /** 
         * Returns the preferred layout space. 
         * @see LayoutManager#minimumLayoutSize(Container)
         */
        public Dimension minimumLayoutSize(Container c) 
        {
            return preferredLayoutSize(c);
        }
        
        /**
         * Returns the amount of space needed to layout all components at
         * their preferred size.
         * @see LayoutManager#preferredLayoutSize(Container)
         */
        public Dimension preferredLayoutSize(Container c)  
        {
            Insets i = getInsets();
            Dimension titleD = title.getPreferredSize(), 
                      subTitleD = subTitle.getPreferredSize(),
                      textD = text.getPreferredSize(),
                      graphxD = graphx.getPreferredSize();
            int  w = i.left+i.right+H_GAP+graphxD.width+
                     Math.max(titleD.width, subTitleD.width),
                 h = i.top+i.bottom+
                     Math.max(titleD.height+subTitleD.height+textD.height, 
                              graphxD.height);
            return new Dimension(w, h);
        }
        
        /** 
         * Lays out the components. 
         * 
         * @param c The container to lay out.
         */
        public void layoutContainer(Container c) 
        {
            Insets i = getInsets();
            int availW = getWidth()-i.left-i.right,
                availH = getHeight()-i.top-i.bottom;
            curLayoutBounds = new Rectangle(i.left, i.top, availW, availH);
            layoutHLine();
            layoutGraphx();
            layoutTitle();
            layoutSubTitle();
            layoutText();
        }   
        
        /**
         * Required by {@link LayoutManager}, but no-op implementation in our 
         * case.
         * @see LayoutManager#addLayoutComponent(String, Component)
         */
        public void addLayoutComponent(String name, Component c) {}
        
        /**
         * Required by {@link LayoutManager}, but no-op implementation in our 
         * case.
         * @see LayoutManager#removeLayoutComponent(Component)
         */
        public void removeLayoutComponent(Component c) {} 
        
    }

}
