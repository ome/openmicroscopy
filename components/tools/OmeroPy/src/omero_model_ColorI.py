"""
/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""
import Ice, IceImport
IceImport.load("omero_model_Color_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"
class ColorI(_omero_model.Color):

      class ColorI_generator:
          def __iter__(self):
              return self
          def next(self):
              return ColorI()

      def generator(cls):
          return cls.ColorI_generator()
      generator = classmethod(generator)

      def __init__(self):
          super(ColorI, self).__init__()

      def getValue(self):
          return self._value

      def setValue(self, value):
          self._value = value
          pass

      def ice_postUnmarshal(self):
          """
          Provides additional initialization once all data loaded
          Required due to __getattr__ implementation.
          """
          pass # Currently unused


      def ice_preMarshal(self):
          """
          Provides additional validation before data is sent
          Required due to __getattr__ implementation.
          """
          pass # Currently unused

      def __getattr__(self, attr):
        if attr == "value":
            return self.getValue()
        elif attr == "red":
            return self.getRed()
        elif attr == "blue":
            return self.getBlue()
        elif attr == "green":
            return self.getGreen()
        elif attr == "alpha":
            return self.getAlpha()
        else:
            raise AttributeError(attr)

      def  __setattr__(self, attr, value):
        if attr.startswith("_"):
            self.__dict__[attr] = value
        else:
            try:
                object.__getattribute__(self, attr)
                object.__setattr__(self, attr, value)
            except AttributeError:
                if attr == "value":
                    return self.setValue(value)
                elif attr == "red":
                    return self.setRed(value)
                elif attr == "blue":
                    return self.setBlue(value)
                elif attr == "green":
                    return self.setGreen(value)
                else:
                    raise

_omero_model.ColorI = ColorI
