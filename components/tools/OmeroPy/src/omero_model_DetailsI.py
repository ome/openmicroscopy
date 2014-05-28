#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""
import Ice
import IceImport
IceImport.load("omero_model_Details_ice")
_omero = Ice.openModule("omero")
_omero_model = Ice.openModule("omero.model")
__name__ = "omero.model"


class DetailsI(_omero_model.Details):

    def __init__(self, client=None):
        super(DetailsI, self).__init__()
        self.__client = client
        self.__session = None
        if client:
            self.__session = client.getSession(False)

    def getClient(self):
        return self.__client

    def getSession(self):
        return self.__session

    def getEventContext(self):
        return self._event

    def getCallContext(self):
        return self._call

    def getOwner(self):
        return self._owner

    def setOwner(self, value):
        self._owner = value
        pass

    def getGroup(self):
        return self._group

    def setGroup(self, value):
        self._group = value
        pass

    def getCreationEvent(self):
        return self._creationEvent

    def setCreationEvent(self, value):
        self._creationEvent = value
        pass

    def getUpdateEvent(self):
        return self._updateEvent

    def setUpdateEvent(self, value):
        self._updateEvent = value
        pass

    def getPermissions(self):
        return self._permissions

    def setPermissions(self, value):
        self._permissions = value
        pass

    def getExternalInfo(self):
        return self._externalInfo

    def setExternalInfo(self, value):
        self._externalInfo = value
        pass

    def ice_postUnmarshal(self):
        """
        Provides additional initialization once all data loaded
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def ice_preMarshal(self):
        """
        Provides additional validation before data is sent
        Required due to __getattr__ implementation.
        """
        pass  # Currently unused

    def __getattr__(self, attr):
        if attr == "owner":
            return self.getOwner()
        elif attr == "group":
            return self.getGroup()
        elif attr == "creationEvent":
            return self.getCreationEvent()
        elif attr == "updateEvent":
            return self.getUpdateEvent()
        elif attr == "permissions":
            return self.getPermissions()
        elif attr == "externalInfo":
            return self.getExternalInfo()
        else:
            raise AttributeError(attr)

    def __setattr__(self, attr, value):
        if attr.startswith("_"):
            self.__dict__[attr] = value
        else:
            try:
                object.__getattribute__(self, attr)
                object.__setattr__(self, attr, value)
            except AttributeError:
                if attr == "owner":
                    return self.setOwner(value)
                elif attr == "group":
                    return self.setGroup(value)
                elif attr == "creationEvent":
                    return self.setCreationEvent(value)
                elif attr == "updateEvent":
                    return self.setUpdateEvent(value)
                elif attr == "permissions":
                    return self.setPermissions(value)
                elif attr == "externalInfo":
                    return self.setExternalInfo(value)
                else:
                    raise

_omero_model.DetailsI = DetailsI
