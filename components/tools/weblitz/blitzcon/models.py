#
# blitzcon/models.py - django application model description
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

from django.db import models

from blitz_connector import BlitzConnector

class StoredConnection(models.Model):
  # To make things play good with MSIE, the url size limit is < 2083 (http://support.microsoft.com/kb/q208427/)
  base_path = models.CharField(max_length=20)
  #logo_img = models.FileField(upload_to='blitzcon/client_imgs')
  server = models.CharField(max_length=2000)
  port = models.PositiveIntegerField()
  username = models.CharField(max_length=200)
  password = models.CharField(max_length=20)
  failcount = models.PositiveIntegerField(default=0)
  enabled = models.BooleanField(default=True)
  has_template = models.BooleanField(default=True)

  def getBlitzConnector (self):
    return BlitzConnector(self.username, self.password, self.server, self.port, self)

  class Admin:
    list_display = ('base_path', 'server', 'username', 'failcount')

	
