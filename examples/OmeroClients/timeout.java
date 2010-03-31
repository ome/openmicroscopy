import omero.*;
import omero.api.*;
import omero.model.*;
import omero.sys.*;
public class timeout {

    static int IDLETIME = 5;
    static client c;
    static ServiceFactoryPrx s;

    public static void main(String[] args) throws Exception {

	final int idletime = args.length > 1 ? Integer.parseInt(args[0]) : IDLETIME;

	c = new client(args);
	s = c.createSession();
	System.out.println(s.getAdminService().getEventContext().sessionUuid);

	final RenderingEnginePrx re = s.createRenderingEngine(); // for keep alive

	class Run extends Thread {
	    public boolean stop = false;
		public void run() {
		while ( ! stop ) {
		    try {
			Thread.sleep(idletime*1000L);
		    } catch (Exception e) {
			// ok
		    }
		    System.out.println(System.currentTimeMillis() + " calling keep alive");
		    try {
                        // Currently, passing a null or empty array to keepAllAlive
                        // would suffice. For future-proofing, however, it makes sense
                        // to pass stateful services.
			s.keepAllAlive(new ServiceInterfacePrx[]{re});
		    } catch (Exception e) {
			c.closeSession();
			throw new RuntimeException(e);
		    }
		}
	    }
	}

	final Run run = new Run();

	class Stop extends Thread {
	    public void run() {
		run.stop = true;
	    }
	}

	Runtime.getRuntime().addShutdownHook(new Stop());
	run.start();

    }
}
