package org.openmicroscopy.shoola.env.ui;

// Java Imports
import java.awt.Component;
import javax.swing.JMenuItem;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public interface TopFrame {

/**  Position a component on the layer of the application internal desktop.
 * 
 *@param    c             component to position
 *@param    position    specified position
 */    
    public void addToDesktop(Component c, int position);
 /* Remove a component form the application internal desktop
  * 
  *@param   c component to be removed
  */
    public void removeFromDesktop(Component c);
 /* add a component to a specified menu 
  *
  *@param menuType  ID which corresponds to specified menu
  *@param item         component to be added
  */
    public void addToMenu(int menuType, JMenuItem item);
/* remove a component from a specified menu 
 *
 *@param menuType  ID which corresponds to specified menu
  *@param item         component to be removed
 */
    public void removeFromMenu(int menuType, JMenuItem item);
}
