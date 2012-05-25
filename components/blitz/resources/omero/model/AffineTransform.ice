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
     * affine transforms. Internally, the data is stored as
     * 6 doubles.
     **/
    ["protected"] class AffineTransform
    {

      double a00;
      double getA00();
      void setA00(double a00);

      double a01;
      double getA01();
      void setA01(double a01);

      double a10;
      double getA10();
      void setA10(double a10);

      double a11;
      double getA11();
      void setA11(double a11);

      double a02;
      double getA02();
      void setA02(double a02);

      double a12;
      double getA12();
      void setA12(double a12);

      AffineTransform proxy();

    };

  };

};
#endif
