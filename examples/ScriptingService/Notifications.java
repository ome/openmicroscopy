
import omero.api.*;
import omero.model.*;
import omero.grid.*;

public class Notifications {

    public final static String SCRIPT = "" +
    "import omero\n" +
    "import omero.scripts as s\n" +
    "s.client(\"name\")\n";
    // does nothing
    public static void main(String args[]) throws Exception{

        long launched = System.currentTimeMillis();
        omero.client client = new omero.client(args);
        try {
            ServiceFactoryPrx sf = client.createSession();
            IScriptPrx scriptService = sf.getScriptService();
            long id = scriptService.uploadOfficialScript(
                String.format("/examples/%s.py", java.util.UUID.randomUUID()), SCRIPT);
            ScriptProcessPrx proc = scriptService.runScript(id, null, null);
            ProcessCallbackI cb = new ProcessCallbackI(client, proc);
            launched = System.currentTimeMillis();
            while (null == cb.block(500)) {
                if (10000 < (System.currentTimeMillis() - launched)) {
                    throw new RuntimeException("Too long!");
                }
            }
        } finally {
            System.out.println("Finished in (ms): " + (System.currentTimeMillis() - launched));
            client.closeSession();
        }
   }
}
