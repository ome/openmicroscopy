package org.openmicroscopy.shoola.env.ui;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public interface UserNotifier {
    public void notifyError(String title, String summary, Exception detail);
    public void notifyError(String title, String message);
    public void notifyInfo(String title, String message);
    public void notifyWarning(String title, String message);

    
}
