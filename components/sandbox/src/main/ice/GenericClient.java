package main.ice;

import ome.model.core.ImageRemote;

import main.eis.generic.ServerPrx;
import main.eis.generic.ServerPrxHelper;

public class GenericClient extends Ice.Application
{

    public static void main(String[] args)
    {
        GenericClient app = new GenericClient();
        int status = app.main("Client", args, "config");
        System.exit(status);
    }

    public int run(String[] args)
    {

        ServerPrx engine = ServerPrxHelper.checkedCast(communicator()
                .stringToProxy("server:tcp -p 10000"));
        if (engine == null)
        {
            System.err.println("invalid engine proxy");
            return 1;
        }

        ImageRemote[] ir = engine.query("null");

        // communicator().waitForShutdown();
        communicator().shutdown();

        return 0;
    }

}
