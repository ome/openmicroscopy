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

files="NEWS INSTALL README AUTHORS ChangeLog COPYING"
for f in $files; do
  [ -e "$f" ] || touch $f
done
aclocal $ACLOCAL_FLAGS -I.. || exit;
autoheader -I.. || exit;
automake --add-missing --copy;
libtoolize
autoconf -I.. || exit;
automake || exit;
./configure "$@"
