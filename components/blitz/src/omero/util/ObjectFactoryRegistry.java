/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SPI type picked up from the Spring configuration and given a chance to
 * register all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicator}.
 *
 * @see ticket:6340
 */
public abstract class ObjectFactoryRegistry {

    protected final Log log = LogFactory.getLog(getClass());

    public static abstract class ObjectFactory implements Ice.ObjectFactory {

        private final String id;

        public ObjectFactory(String id) {
            this.id = id;
        }

        public void register(Log log, Ice.Communicator ic, boolean strict) {
            if (strict) {
                ic.addObjectFactory(this, id);
            } else {
                final Ice.ObjectFactory of = ic.findObjectFactory(id);
                if (null == of) {
                    ic.addObjectFactory(this, id);
                } else {
                    log.debug(String.format(
                            "ObjectFactory already exists: %s=%s", id, of));
                }
            }
        }

        public abstract Ice.Object create(String name);

        public void destroy() {
            // noop
        }

    }

    public abstract Map<String, ObjectFactory> createFactories();

    public void setIceCommunicator(Ice.Communicator ic) {
        Map<String, ObjectFactory> factories = createFactories();
        for (ObjectFactory of : factories.values()) {
            of.register(log, ic, false);
        }
    }

}
