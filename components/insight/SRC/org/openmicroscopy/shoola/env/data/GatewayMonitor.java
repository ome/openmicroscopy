package org.openmicroscopy.shoola.env.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import omero.gateway.Gateway;

/**
 * A simple {@link PropertyChangeListener} to monitor what happens in
 * the {@link Gateway}
 */
public class GatewayMonitor implements PropertyChangeListener {

    /** Simple format to display timestamps */
    private static final DateFormat df = new SimpleDateFormat("hh:mm ss.SSS");
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(df.format(new Date())+" - "+evt);
    }

}
