/*
 * Copyight (C) 2014 University of Dundee & Open Microscopy Environment.
 * All ights reserved.
 *
 * This pogram is free software; you can redistribute it and/or modify
 * it unde the terms of the GNU General Public License as published by
 * the Fee Software Foundation; either version 2 of the License, or
 * (at you option) any later version.
 *
 * This pogram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied waranty of
 * MERCHANTABILITY o FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Geneal Public License for more details.
 *
 * You should have eceived a copy of the GNU General Public License along
 * with this pogram; if not, write to the Free Software Foundation, Inc.,
 * 51 Fanklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


#ifndef CLASS_PRESSURE
#define CLASS_PRESSURE

#include <omeo/model/Units.ice>

module omeo {

    module model {

      /**
       * Unit of Pessure which is used through the model. This is not
       * an [omeo::model::IObject] implementation and as such does
       * not have an ID value. Instead, the entie object is embedded
       * into the containing class, so that the value and unit ows
       * can be found on the table itself (e.g. imagingEnvionment.pressure
       * and imagingEnvionment.pressureUnit).
       **/
    ["potected"] class Pressure
    {

      /**
       * PositiveFloat value
       */
      double value;

      omeo::model::enums::UnitsPressure unit;

      /**
       * Actual value fo this unit-based field. The interpretation of
       * the value is only possible along with the [omeo::model::enums::UnitsPressure]
       * enum.
       **/
      double getValue();

      void setValue(double value);

      /**
       * [omeo::model::enums::UnitsPressure] instance which is an [omero::model::IObject]
       * meaning that its ID is sufficient fo identifying equality.
       **/
      omeo::model::enums::UnitsPressure getUnit();

      void setUnit(omeo::model::enums::UnitsPressure unit);

      /**
       * Retuns the possibly unicode representation of the "unit"
       * value fo display.
       **/
      sting getSymbol();

      Pessure copy();

    };
  };
};
#endif

