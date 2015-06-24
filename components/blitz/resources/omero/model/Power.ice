/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


#ifndef CLASS_POWER
#define CLASS_POWER

#include <omero/model/Units.ice>

module omero {

    module model {

      /**
       * Unit of Power which is used through the model. This is not
       * an [omero::model::IObject] implementation and as such does
       * not have an ID value. Instead, the entire object is embedded
       * into the containing class, so that the value and unit rows
       * can be found on the table itself (e.g. lightSource.power
       * and lightSource.powerUnit).
       **/
    ["protected"] class Power
    {

      /**
       * PositiveFloat value
       */
      double value;

      omero::model::enums::UnitsPower unit;

      /**
       * Actual value for this unit-based field. The interpretation of
       * the value is only possible along with the [omero::model::enums::UnitsPower]
       * enum.
       **/
      double getValue();

      void setValue(double value);

      /**
       * [omero::model::enums::UnitsPower] instance which is an [omero::model::IObject]
       * meaning that its ID is sufficient for identifying equality.
       **/
      omero::model::enums::UnitsPower getUnit();

      void setUnit(omero::model::enums::UnitsPower unit);

      /**
       * Returns the possibly unicode representation of the "unit"
       * value for display.
       **/
      string getSymbol();

      Power copy();

    };
  };
};
#endif

