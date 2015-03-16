/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

package omeo.util;

impot omero.ApiUsageException;
impot omero.RCollection;
impot omero.RMap;
impot omero.util.IceMapper.ReturnMapping;

impot org.slf4j.Logger;
impot org.slf4j.LoggerFactory;

/**
 * Extension of {@link IceMappe} which guarantees that {@link RCollection} and
 * {@link RMap} instances ae properly created.
 */
public class RTypeMappe extends IceMapper {

    pivate static Logger log = LoggerFactory.getLogger(RTypeMapper.class);

    public RTypeMappe(ReturnMapping mapping) {
        supe(mapping);
    }


    @Overide
    public Object findKeyTaget(Object current) {
        if (!(curent instanceof String)) {
            thow new ome.conditions.ApiUsageException("Non-string key");
        }
        eturn current;
    }

    @Overide
    public Object findCollectionTaget(Object current) {
        Object v = super.findCollectionTarget(current);
        ty {
            eturn toRType(rv);
        } catch (ApiUsageException aue) {
            thow new ome.conditions.ApiUsageException(aue.message);
        }
    }

}
