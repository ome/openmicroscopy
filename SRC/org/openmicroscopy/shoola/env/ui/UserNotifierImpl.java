package org.openmicroscopy.shoola.env.ui;

//Java imports
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/** Acts as a centralized place where errors are collected and then notified to the user. 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public class UserNotifierImpl 
    implements UserNotifier {
    
    private static final String     DEFAULT_ERROR_TITLE = "Error";
    private static final String     DEFAULT_ERROR_SUMMARY = "Sorry, an error occurred";
    
    private static final String     DEFAULT_INFO_TITLE = "Info";
    private static final String     DEFAULT_INFO_MESSAGE = "Message Info";
    private static final String     DEFAULT_WARNING_TITLE = "Warning";
    private static final String     DEFAULT_WARNING_MESSAGE = "Message "; 
    private TopFrameImpl    topFrame;
    
    /** Creates a new instance of UserNotifierImpl */
    public UserNotifierImpl(TopFrameImpl topFrame) {
        this.topFrame = topFrame;
    }
    // TODO: to be modified. Display message using a JDialog:
    // will implement code soon
    
/** Implemented as specified by {@linkUserNotifier}.
 */       
    public void notifyError(String title, String summary, Exception detail) {
        if( title == null  ||  title.length() == 0 )
            title = DEFAULT_ERROR_TITLE;
        StringBuffer    buf = new StringBuffer();
        if( summary == null  ||  summary.length() == 0 )   
            buf.append(DEFAULT_ERROR_SUMMARY);
        else    buf.append(summary);
        String  d = detail==null ? null : detail.getMessage();
        if( d != null  &&  d.length() != 0 ) {
            buf.append(":\n\n");
            buf.append(d);
        }
        // to be modified
        JOptionPane.showMessageDialog(topFrame, buf.toString(), title, 
                                                    JOptionPane.ERROR_MESSAGE);
    }
/** Implemented as specified by {@linkUserNotifier}.
 */     
    public void notifyError(String title, String summary) {
        notifyError(title, summary, null);
    }
/** Implemented as specified by {@linkUserNotifier}.
 */ 
    public void notifyInfo(String title, String message) {  
        if( title == null  ||  title.length() == 0 )
            title = DEFAULT_INFO_TITLE;
        StringBuffer    buf = new StringBuffer();
        if( message == null  ||  message.length() == 0 )   
            buf.append(DEFAULT_INFO_MESSAGE);
        else    buf.append(message);
        // to be modified
        JOptionPane.showMessageDialog(topFrame, buf.toString(), title, 
                                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
/** Implemented as specified by {@linkUserNotifier}.
 */ 
    public void notifyWarning(String title, String message) {
        if( title == null  ||  title.length() == 0 )
            title = DEFAULT_WARNING_TITLE;
        StringBuffer    buf = new StringBuffer();
        if( message == null  ||  message.length() == 0 )   
            buf.append(DEFAULT_WARNING_MESSAGE);
        else    buf.append(message);
        // to be modified
        JOptionPane.showMessageDialog(topFrame, buf.toString(), title, 
                                                    JOptionPane.WARNING_MESSAGE);
    }    

    
}
