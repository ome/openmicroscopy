package mono;

import ome.model.core.Roi4DRemote;
import ome.model.core.Roi5DRemote;

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
            Roi4DRemote r4 = (Roi4DRemote) r5.roi4ds.get(0);
            Roi4DRemote r4_ = (Roi4DRemote) r5.roi4ds.get(0);
            
            System.out.println(r4.roi3ds);
            System.out.println(r4_.roi3ds);
            
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
