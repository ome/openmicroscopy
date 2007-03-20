/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.test;

import junit.framework.TestCase;
import ome.services.icy.client.IceServiceFactory;
import ome.services.licenses.LicensedServiceFactory;
import omero.api.ServiceInterfacePrx;
import omero.licenses.ILicense;
import omero.licenses.ILicensePrx;
import omero.licenses.ILicensePrxHelper;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test( groups = {"client","integration","blitz"} )
public class BlitzLicenseTest extends TestCase{

    IceServiceFactory ice;
    ILicensePrx licenseService;
    byte[] token;
    
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        
        ice = new IceServiceFactory(null,null,null);
        ice.createSession();
        ServiceInterfacePrx prx = ice.getProxy().getByName("omero.licenses.ILicense");
        licenseService = ILicensePrxHelper.uncheckedCast(prx);
        licenseService.resetLicenses();
    }
            
    @Test
    public void testAcquireLicenseAutomatically() throws Exception {
        // should be done for us.
        ice.getConfigService(null).getServerTime();
    }

    @Test
    public void testAcquireLicenseManually() throws Exception {
        token = licenseService.acquireLicense();
        fail("NYI"); // sf.setLicenseToken(token);
        licenseService.releaseLicense(token);
    }
    
    @Test
    public void testReset() throws Exception {
        
        long totalA = licenseService.getTotalLicenseCount();
        long availA  = licenseService.getAvailableLicenseCount();
        
        token = licenseService.acquireLicense();
        
        long totalB = licenseService.getTotalLicenseCount();
        long availB = licenseService.getAvailableLicenseCount();
        
        licenseService.resetLicenses();
        
        long totalC = licenseService.getTotalLicenseCount();
        long availC = licenseService.getAvailableLicenseCount();
        
        assertTrue(totalA == totalB && totalB == totalC);
        assertTrue(availA == totalA && totalC == availC);
        assertTrue(availB == totalB - 1);
        
    }
    
    @Test
    public void testTimeouts() {
        fail("Not implemented.");
    }
   
}
