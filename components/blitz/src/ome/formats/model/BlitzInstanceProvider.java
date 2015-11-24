/*
 * ome.formats.model.BlitzInstanceProvider
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.model;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import ome.formats.enums.EnumerationProvider;
import ome.formats.model.handler.ModelObjectHandlerFactory;
import omero.model.IObject;

/**
 * An instance provider which uses reflection to fulfill the contract of an
 * InstanceProvider. Its main feature is the delegation of class specific
 * logic to handlers. OME data model enumeration not-null constraints, for
 * example.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class BlitzInstanceProvider implements InstanceProvider {

  /** Model object handler factory. */
  private ModelObjectHandlerFactory modelObjectHandlerFactory;

  /** Constructor cache. */
  private Map<Class<? extends IObject>,
              Constructor<? extends IObject>> constructorCache =
      new HashMap<Class<? extends IObject>, Constructor<? extends IObject>>();

  /**
   * Default constructor.
   * @param enumProvider Enumeration provider we are to use.
   */
  public BlitzInstanceProvider(EnumerationProvider enumProvider) {
    modelObjectHandlerFactory = new ModelObjectHandlerFactory(enumProvider);
  }

  /* (non-Javadoc)
   * @see ome.formats.model.InstanceProvider#getInstance(java.lang.Class)
   */
  public <T extends IObject> T getInstance(Class<T> klass)
      throws ModelException {
    try {
      Constructor<T> constructor = getConstructor(klass);
      IObject o = constructor.newInstance();
      return (T) modelObjectHandlerFactory.getHandler(klass).handle(o);
    } catch (Exception e) {
      String m = "Unable to instantiate object.";
      throw new ModelException(m, klass, e);
    }
  }

  /**
   * Retrieves a constructor for a given class from the constructor cache if
   * possible.
   * @param klass Class to retrieve a constructor for.
   * @return See above.
   * @throws ModelException If there is an error retrieving the constructor.
   */
  private <T extends IObject> Constructor<T> getConstructor(Class<T> klass)
      throws ModelException {
    Constructor<? extends IObject> constructor = constructorCache.get(klass);
    if (constructor == null) {
      try {
        Class<T> concreteClass =
            (Class<T>) Class.forName(klass.getName() + "I");
        constructor = concreteClass.getDeclaredConstructor();
        constructorCache.put(klass, constructor);
      } catch (Exception e) {
        String m = "Unable to retrieve constructor.";
        throw new ModelException(m, klass, e);
      }
    }
    return (Constructor<T>) constructor;
  }

}
