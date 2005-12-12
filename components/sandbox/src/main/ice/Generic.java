package main.ice;

public class Generic extends Ice.Application
{

    public static void main(String[] args)
    {
        Generic app = new Generic();
        int status = app.main("Server", args, "config");
        System.exit(status);
    }

    public int run(String[] args)
    {

        Ice.ObjectAdapter adapter = communicator()
                .createObjectAdapterWithEndpoints("engine", "tcp -p 10000");

        ServerI server = new ServerI();
        adapter.add(server, Ice.Util.stringToIdentity("server"));
        adapter.activate();

        try
        {
            communicator().waitForShutdown();
        } catch (Exception e)
        {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}
