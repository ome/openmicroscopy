#
# Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#


# -----------------------------------------------------------------------
# Environment variables for use by the server. The values listed here will
# usually be the defaults. These files should be configured by users and then
# run in the current shell before starting OMERO.
# -----------------------------------------------------------------------


# Time until secondary servers startup, in milliseconds.
OMERO_STARTUP_WAIT=10000

# Location for the creation of the OMERO temporary directory.
# If $HOME fails, then $TEMPDIR will be used.
OMERO_TEMPDIR=$HOME

# ONLY set OMERO_HOME if you know what you are doing.
# ----------------------------------------------------
# Alternative installation directory to use.
### OMERO_HOME=/opt/omero
