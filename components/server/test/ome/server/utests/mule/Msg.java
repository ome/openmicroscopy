/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.mule;

import junit.framework.TestCase;
import ome.parameters.Parameters;
import ome.system.OmeroContext;

import org.mule.extras.spring.events.MuleApplicationEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
class InternalMsg extends ApplicationEvent implements IMsg{
    public InternalMsg(Object source) {
        super(source);
    }
    public Parameters getParameters() {
        return null;
    }
    public String getSession() {
        return null;
    }
}

class ExternalMsg extends MuleApplicationEvent implements IMsg {
    public ExternalMsg(Object message, String endpoint) {
        super(message, endpoint);
    }
    public Parameters getParameters() {
        return null;
    }
    public String getSession() {
        return null;
    }
}