package cmd;


/** 
 * Interface that all commands linked to an 
 * <code>ProtocolEditorAction</code> must implement. 
 *
 */
public interface ActionCmd
{

    /** Executes the command. */
    public void execute();
    
}

