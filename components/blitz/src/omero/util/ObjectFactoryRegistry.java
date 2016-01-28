/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPI type picked up from the Spring configuration and given a chance to
 * register all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicator}.
 *
 * @see <a href="http://trac.openmicroscopy.org/ome/ticket/6340">Trac ticket #6340</a>
 */
public abstract class ObjectFactoryRegistry {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static abstract class ObjectFactory implements Ice.ObjectFactory {

        private final String id;

        public ObjectFactory(String id) {
            this.id = id;
        }

        public void register(Logger log, Ice.Communicator ic, boolean strict) {
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

    public abstract Map<String, ObjectFactory> createFactories(Ice.Communicator ic);

    public void setIceCommunicator(Ice.Communicator ic) {
        Map<String, ObjectFactory> factories = createFactories(ic);
        for (ObjectFactory of : factories.values()) {
            of.register(log, ic, false);
        }
    }

}
