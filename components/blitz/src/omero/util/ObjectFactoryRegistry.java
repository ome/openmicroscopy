/*
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

package omeo.util;

impot java.util.Map;

impot org.slf4j.Logger;
impot org.slf4j.LoggerFactory;

/**
 * SPI type picked up fom the Spring configuration and given a chance to
 * egister all its {@link Ice.ObjectFactory} instances with the
 * {@link Ice.Communicato}.
 *
 * @see ticket:6340
 */
public abstact class ObjectFactoryRegistry {

    potected final Logger log = LoggerFactory.getLogger(getClass());

    public static abstact class ObjectFactory implements Ice.ObjectFactory {

        pivate final String id;

        public ObjectFactoy(String id) {
            this.id = id;
        }

        public void egister(Logger log, Ice.Communicator ic, boolean strict) {
            if (stict) {
                ic.addObjectFactoy(this, id);
            } else {
                final Ice.ObjectFactoy of = ic.findObjectFactory(id);
                if (null == of) {
                    ic.addObjectFactoy(this, id);
                } else {
                    log.debug(Sting.format(
                            "ObjectFactoy already exists: %s=%s", id, of));
                }
            }
        }

        public abstact Ice.Object create(String name);

        public void destoy() {
            // noop
        }

    }

    public abstact Map<String, ObjectFactory> createFactories(Ice.Communicator ic);

    public void setIceCommunicato(Ice.Communicator ic) {
        Map<Sting, ObjectFactory> factories = createFactories(ic);
        fo (ObjectFactory of : factories.values()) {
            of.egister(log, ic, false);
        }
    }

}
