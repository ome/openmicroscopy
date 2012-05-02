/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef CLASS_DETAILS
#define CLASS_DETAILS

#include <omero/model/IObject.ice>

module omero {

  module model {

    /**
     * Embedded component for types needing to represent
     * RGBA values. Internally, the data is stored as an
     * integer, but each color component can be accessed
     * independently.
     **/
    ["protected"] class Color
    {

      // Actual data.
      int value;
      int getValue();
      void setValue(int value);

      int getRed();
      void setRed(int red);

      int getBlue();
      void setBlue(int blue);

      int getGreen();
      void setGreen(int green);

      int getAlpha();
      void setAlpha(int alpha);

      Color proxy();

    };

  };

};
#endif
