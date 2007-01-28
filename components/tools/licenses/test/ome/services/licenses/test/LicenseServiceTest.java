/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.test;

import junit.framework.TestCase;
import ome.services.licenses.ILicense;
import ome.services.licenses.LicensedServiceFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test( groups = {"client","integration"} )
public class LicenseServiceTest extends TestCase{

    LicensedServiceFactory sf; // strictly client
    ILicense licenseService;
    long total;
    byte[] token;
    
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        
        sf = new LicensedServiceFactory(null, null, null);
        licenseService = sf.getLicenseService();
        licenseService.resetLicenses();
    }
            
    @Test
    public void testAcquireLicenseAutomatically() throws Exception {
        sf.acquireLicense();
        sf.releaseLicense();
    }

    @Test
    public void testAcquireLicenseManually() throws Exception {
        token = licenseService.acquireLicense();
        sf.setLicenseToken(token);
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
