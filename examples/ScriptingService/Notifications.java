import static omero.rtypes.*;
import omero.api.*;
import omero.model.*;
import omero.grid.*;

public class Notifications {

    public final static String SCRIPT = "" +
    "import omero\n" +
    "import omero.scripts as s\n" +
    "s.client(\"name\",None)\n";
    // does nothing
    public static void main(String args[]) throws Exception{

        omero.client client = new omero.client();
        try {
            ServiceFactoryPrx sf = client.createSession();
            IScriptPrx scriptService = sf.getScriptService();
            long id = scriptService.uploadScript(SCRIPT);
            JobParams params = scriptService.getParams(id);

            Job job = new ScriptJobI();
            job.linkOriginalFile(new OriginalFileI(id, false));
            InteractiveProcessorPrx ip = sf.sharedResources().acquireProcessor(job, 10);
            ProcessPrx proc = ip.execute(null);
            ProcessCallbackI cb = new ProcessCallbackI(client, proc);
            System.out.println(cb.block(5000));
            proc.poll();
            System.out.println(cb.block(5000));
        } finally {
            client.closeSession();
        }
   }
}
