#!/bin/sh

(automake --version) < /dev/null > /dev/null 2>&1 || {
	echo;
	echo "You must have automake installed to compile OMERO.blitz for C++";
	echo;
	exit;
}

(autoconf --version) < /dev/null > /dev/null 2>&1 || {
	echo;
	echo "You must have autoconf installed to compile OMERO.blitz for C++";
	echo;
	exit;
}

echo "Generating configuration files for OMERO.blitz for C++, please wait...."
echo;

aclocal $ACLOCAL_FLAGS || exit;
autoheader || exit;
automake --add-missing --copy;
libtoolize
autoconf || exit;
automake || exit;
./configure $@
