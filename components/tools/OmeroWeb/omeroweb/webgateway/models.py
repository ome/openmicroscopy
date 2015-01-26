#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webgateway/model.py - django application model description
#
# Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
#
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>
#
# from django.db import models
#
# import omero
#
# class StoredConnection(models.Model):
#     # To make things play good with MSIE, the url size limit is < 2083
#     # (http://support.microsoft.com/kb/q208427/)
#     base_path = models.CharField(max_length=20)
#     config_file = models.CharField(max_length=200, blank=True)
#     username = models.CharField(max_length=200, blank=True)
#     password = models.CharField(max_length=20, blank=True)
#     failcount = models.PositiveIntegerField(default=0)
#     enabled = models.BooleanField(default=True)
#     admin_group = models.CharField(max_length=80, blank=True)
#     annotations = models.TextField(blank=True)
#     site_message = models.TextField(blank=True)
#
# def getBlitzGateway (self, trysuper=True):
#     rv = omero.client_wrapper(
#         self.username, self.password, self,
#         group=trysuper and str(self.admin_group) or None,try_super=trysuper,
#         extra_config=self.config_file)
#    rv.conn = self
#    return rv
#
#  def getProperty (self, key):
#    for e in [x.split(':') for x in self.annotations.split('\n')]:
#      if e[0].strip() == key:
#        if len(e) < 2:
#          return True
#        return (':'.join(e[1:])).strip()
#    return None
#
#  def getMessage (self):
#    return self.site_message
