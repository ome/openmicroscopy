package ome.services.licenses.tasks;

/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

import java.util.Properties;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.meta.Experimenter;
import ome.services.blitz.tasks.BlitzTask;
import ome.services.licenses.ILicense;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.SimpleTask;
import omero.constants.LICENSESERVICE;
import omero.licenses.ILicensePrx;
import omero.licenses.ILicensePrxHelper;

/**
 * {@link BlitzTask} which provides access to the
 * {@link ILicense license service}, and can be used to reset the licenses in
 * the application server or in blitz.
 * 
 * name, first name, and last name, and optionally with the given email, middle
 * name, institution, and email.
 * 
 * Understands no parameters other than those of the super class.
 * 
 * Must be logged in as an administrator. See {@link Configuration} on how to do
 * this.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SimpleTask
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2007-01-24 17:23:09 +0100 (Wed, 24 Jan 2007) $")
@RevisionNumber("$Revision: 1208 $")
public class ResetLicensesTask extends BlitzTask {

    /**
     * Enumeration of the string values which will be used directly by
     * {@link ResetLicensesTask}.
     */
    public enum Keys {
        // none
    }

    /** Delegates to super */
    public ResetLicensesTask(ServiceFactory sf, Properties p) {
        super(sf, p);
    }

    /** Delegates to super */
    public ResetLicensesTask(omero.client sf, Properties p) {
        super(sf, p);
    }

    /**
     * Performs the actual {@link Experimenter} creation.
     */
    @Override
    public void doTask() {
        super.doTask(); // logs

        if (useBlitz) {
            try {
                final omero.client isf = getBlitzServiceFactory();
                final Ice.ObjectPrx prx = isf.getServiceFactory().getByName(
                        LICENSESERVICE.value);
                final ILicensePrx lic = ILicensePrxHelper.uncheckedCast(prx);
                lic.resetLicenses();
            } catch (omero.ServerError se) {
                throw new RuntimeException(se);
            }
        } else {
            final ServiceFactory sf = getServiceFactory();
            final ILicense licenseService = sf
                    .getServiceByClass(ILicense.class);
            licenseService.resetLicenses();
        }
        getLogger().info("Reset all licenses.");

    }

}
