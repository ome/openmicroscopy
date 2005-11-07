using System;
using test;
using ome.model.core;
public class Client
{
    public static void Main(string[] args)
    {
        int status = 0;
        Ice.Communicator ic = null;
    try {
        ic = Ice.Util.initialize(ref args);
        Ice.ObjectPrx obj = ic.stringToProxy(
                "T:default -p 10000 -h 127.0.0.1");
        TPrx printer
                = TPrxHelper.checkedCast(obj);
        if (printer == null)
            throw new ApplicationException("Invalid proxy");
        Roi5DRemote r5 = printer.getRoi5D();
        Roi4DRemote r4 = r5.roi4ds[0];
        Roi4DRemote r4_ = r5.roi4ds[1];
        Console.WriteLine(r4.roi3ds[0].GetHashCode());
        Console.WriteLine(r4_.roi3ds[0].GetHashCode());
    } catch (Exception e) {
        Console.Error.WriteLine(e);
        status = 1;
    }
    if (ic != null) {
        // Clean up
        //
        try {
            ic.destroy();
        } catch (Exception e) {
            Console.Error.WriteLine(e);
            status = 1;
        }
    }
    Environment.Exit(status);
  }
}
