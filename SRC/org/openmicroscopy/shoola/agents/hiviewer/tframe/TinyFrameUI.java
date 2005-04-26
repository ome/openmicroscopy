/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrameUI
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;

//Third-party libraries

//Application-internal dependencies

/** 
 * The UI delegate for the {@link TinyFrame}.
 * A delegate can't be shared among different instances of {@link TinyFrame} 
 * and has a life-time dependency with its owning frame.
 * (We don't share a delegate because we inherit from
 * {@link javax.swing.plaf.basic.BasicInternalFrameUI}, which can't be shared.)
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TinyFrameUI
    extends BasicInternalFrameUI
{
    
    /** The margin of the frame's border. */
    public static final int    BORDER_MARGIN = 2;
    
    /** The thickness of the frame's border. */
    public static final int    BORDER_THICKNESS = 1;
    
    /** The thickness of the frame's scrollbars. */
    public static final int    SCROLLBAR_THICKNESS = 8;
    
    /** The color of the frame's border. */
    public static final Color  BORDER_COLOR = new Color(99, 130, 191);
    
    /** 
     * The highlight color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color  INNER_BORDER_HIGHLIGHT = 
                                                    new Color(240, 240, 240);
    
    /** 
     * The shadow color to use for the inner border surrounding the
     * frame's contents.
     */
    public static final Color  INNER_BORDER_SHADOW = new Color(200, 200, 200);
    
    /** The color of the desktop pane. */
    public static final Color  DESKTOP_COLOR = new Color(250, 253, 255);
  
    
    /** The scroll pane that contains the internal desktop. */
    private JScrollPane dskDecorator;

    /** The frame that owns this UI delegate. */
    protected TinyFrame frame;
    
    /** The component that draws the frame's title bar. */
    protected TitleBar  titleBar;
    
    
    /**
     * Creates a new UI delegate for the specified <code>frame</code>.
     * 
     * @param frame The frame that will own this UI delegate.  
     *              Mustn't be <code>null</code>.
     */
    TinyFrameUI(TinyFrame frame)
    {
        super(frame);
        if (frame == null) throw new NullPointerException("No frame.");
        this.frame = frame;
        titleBar = new TitleBar(frame.getTitle());
        makeBorders();
        frame.setOpaque(false);
    }
    
    /**
     * Decorates the frame's internal desktop with a scroll pane.
     * 
     * @param dp The internal desktop.
     * @return A scroll pane enclosing <code>dp</code>.
     */
    JScrollPane decorateDesktopPane(JDesktopPane dp)
    {
        dp.setBackground(DESKTOP_COLOR);

        dskDecorator = new JScrollPane(dp);
        dskDecorator.setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED,
                INNER_BORDER_HIGHLIGHT, INNER_BORDER_SHADOW)); 
        
        dskDecorator.getHorizontalScrollBar().setPreferredSize(
                new Dimension(100, SCROLLBAR_THICKNESS));
        dskDecorator.getVerticalScrollBar().setPreferredSize(
                new Dimension(SCROLLBAR_THICKNESS, 100));
        //NOTE: Makes the scrollars tiny.  100 is arbitrary and shouldn't 
        //matter in the end.  SCROLLBAR_THICKNESS should be respected though.
        
        return dskDecorator;
    }
    
    /**
     * Returns the title bar component.
     * 
     * @return See above.
     */
    JComponent getTitleBar() { return titleBar; }
    
    /**
     * Attaches the <code>controller</code> to the sizing button.
     * 
     * @param controller An instance of {@link FrameControl}.
     */
    void attachActionListener(ActionListener controller)
    {
        titleBar.sizeButton.addActionListener(controller);
    }
    
    /**
     * Updates and repaints the title bar.
     */
    void updateTitleBar() 
    { 
        titleBar.update(frame.getTitle(), frame.getHighlight()); 
    }
    
    /**
     * Repaints the frame according to whether the frame is currently 
     * collapsed or expanded.
     */
    void updateCollapsedState()
    {
        if (frame.isCollapsed()) {
            frame.setSize(frame.getWidth(), TitleBar.HEIGHT+
                    2*(BORDER_THICKNESS+BORDER_MARGIN));  
            //For example, if BORDER_THICKNESS=1, then +2 will make the border 
            //bottom show as the border thickness is 1, so we need 1px at the 
            //top and 1px at the bottom.  (Otherwise the border and the last 
            //px line of the title bar won't show.)
            titleBar.sizeButton.setActionType(SizeButton.EXPAND);
        } else {
            frame.moveToFront();
            frame.setSize(frame.getRestoreSize());
            titleBar.sizeButton.setActionType(SizeButton.COLLAPSE);
        }
    }
    
    /**
     * Returns the size this widget should have to fully display the internal
     * desktop at its preferred size.
     * 
     * @return See above.
     */
    Dimension getIdealSize()
    {
        Dimension sz = new Dimension();
        Dimension internalDesktopSz = frame.getContentPane().getPreferredSize();
        Insets scrollPaneInsets = dskDecorator.getInsets(),
                frameInsets = frame.getInsets();
        sz.width = frameInsets.left + scrollPaneInsets.left + 
                    internalDesktopSz.width + 
                    scrollPaneInsets.right + frameInsets.right;
        sz.height = frameInsets.top + 
                    TitleBar.HEIGHT + 
                    scrollPaneInsets.top +
                    internalDesktopSz.height + 
                    scrollPaneInsets.bottom +
                    frameInsets.bottom;
        return sz;
    }
 
    /**
     * Creates and sets the frame and its content's borders.
     */
    protected void makeBorders()
    {
        Object x = frame.getContentPane();
        if (x instanceof JComponent)
            ((JComponent) x).setBorder(BorderFactory.createEmptyBorder());
        frame.setBorder(new FrameBorder(BORDER_COLOR, DESKTOP_COLOR, 
                                        BORDER_MARGIN));
    }
    
    /**
     * Overriden to have the superclass use our {@link #titleBar}.
     */
    protected JComponent createNorthPane(JInternalFrame f) { return titleBar; }
    
    /**
     * Overridden to create a mananger that returns the correct minimum size.
     */
    protected LayoutManager createLayoutManager()
    {
        return new InternalFrameLayout() {
            public Dimension minimumLayoutSize(Container c) 
            {
                Dimension d = titleBar.getMinimumSize();
                Insets i = frame.getInsets();
                d.width += i.left+i.right;
                d.height += i.top+i.bottom;
                return d;
            }    
        };
    }
    //NOTE: InternalFrameLayout returns the correct size only if the title bar
    //is an instance of BasicInternalFrameTitlePane -- see code in superclass.
    
}
