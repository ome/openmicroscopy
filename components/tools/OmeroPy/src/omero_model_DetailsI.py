"""
/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""
import omero.model
import OMERO_Model_Details_ice
import omero_model_PermissionsI
__name__ = "omero.model"
class DetailsI(omero.model.Details):

      class DetailsI_generator:
          def __iter__(self):
              return self
          def next(self):
              return DetailsI()

      @classmethod
      def generator(cls):
          return cls.DetailsI_generator()

      def __init__(self):
          self.permissions = omero.model.PermissionsI()

      def getOwner(self):
          return self.owner

      def setOwner(self, _owner):
          self.owner = value
          pass

      def getGroup(self):
          return self.group

      def setGroup(self, _group):
          self.group = value
          pass

      def getCreationEvent(self):
          return self.creationEvent

      def setCreationEvent(self, _creationEvent):
          self.creationEvent = value
          pass

      def getUpdateEvent(self):
          return self.updateEvent

      def setUpdateEvent(self, _updateEvent):
          self.updateEvent = value
          pass

      def getPermissions(self):
          return self.permissions

      def setPermissions(self, _permissions):
          self.permissions = value
          pass

      def getExternalInfo(self):
          return self.externalInfo

      def setExternalInfo(self, _externalInfo):
          self.externalInfo = value
          pass

omero.model.DetailsI = DetailsI
