#!/usr/bin/env python
# Script which is placed into the Docker image to perform pre-start config
# see:
# - https://trello.com/c/rPstbt4z/216-open-ssl-110
# - https://github.com/openmicroscopy/openmicroscopy/pull/5998


from subprocess import checked_call


OMERO = '/opt/omero/server/OMERO.server/bin/omero'
checked_call([OMERO, 'config', 'set', '--', "omero.glacier2.IceSSL.Ciphers",
              "ADH:!LOW:!MD5:!EXP:!3DES:@STRENGTH:@SECLEVEL=0")
