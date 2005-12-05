package mono;

import ome.model.roi.Roi5DRemote;

public class Client {
    public static void
    main(String[] args)
    {
        int status = 0;
        Ice.Communicator ic = null;
        try {
            ic = Ice.Util.initialize(args);
            Ice.ObjectPrx base = ic.stringToProxy(
                    "T:default -p 10000");
            mono.TPrx printer
                = mono.TPrxHelper.checkedCast(base);
            if (printer == null)
                throw new Error("Invalid proxy");
        
            Roi5DRemote r5 = printer.getRoi5D();

            System.out.println(r5);
            
        } catch (Ice.LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }
        if (ic != null) {
            // Clean up
            //
            try {
                ic.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
      }
    }
