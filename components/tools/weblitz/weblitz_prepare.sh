#!/bin/bash

#
# weblitz_nginx_prepare.sh - basic weblitz environment bootstrapping
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

mkdir -p .build

cd .build

############
## PySQLite
############
wget http://initd.org/pub/software/pysqlite/releases/2.3/2.3.5/pysqlite-2.3.5.tar.gz
tar xvzf pysqlite-2.3.5.tar.gz
cd pysqlite-2.3.5
python setup.py build

cp -r build/lib*/pysqlite2 ../../

cd ../../

# Django application server
svn co -r6652 http://code.djangoproject.com/svn/django/trunk/django django

# This will ask a few questions
#python manage.py syncdb

