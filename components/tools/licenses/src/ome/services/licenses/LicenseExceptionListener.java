/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses;

import ome.services.blitz.util.ConvertToBlitzExceptionMessage;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Listens for {@link ConvertToBlitzExceptionMessage} sent by
 * {@link ome.services.blitz.util.IceMethodInvoker} and converts
 * {@link LicenseException} instances to
 * {@link omero.licenses.NoAvailableLicenseException} et al.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class LicenseExceptionListener implements ApplicationListener {

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ConvertToBlitzExceptionMessage) {
			ConvertToBlitzExceptionMessage msg = (ConvertToBlitzExceptionMessage) event;
			if (msg.from instanceof NoAvailableLicensesException) {
				omero.licenses.NoAvailableLicenseException nale = new omero.licenses.NoAvailableLicenseException();
				nale.reason = msg.from.getMessage();
				msg.to = nale;
			}
		}
	}
}
