dnl Init Autoconf
AC_INIT([blitz], [0.1], [ome-devel@lists.openmicroscopy.org.uk])

dnl Version number
AC_DEFINE(VERSION_MAJOR, 0, [Major part of the version number])
AC_DEFINE(VERSION_MINOR, 1, [Minor part of the version number])

dnl Our Autoconf prerequisite, needed for AC_SYS_LARGEFILE
AC_PREREQ(2.50)

dnl Yes, AM_CONFIG_HEADER is correct for our purposes. Things will still work
dnl fine but the M4 may not get built correctly if Automake is earlier than
dnl version 1.6.0. Let me know if you have any weirdness.
dnl -- Chris Allan <callan@blackcat.ca>
AM_CONFIG_HEADER(config.h)

dnl Configurable Ice locations
AC_ARG_WITH(ice,
            AC_HELP_STRING(
                [--with-ice=PREFIX],
                [location of the Ice PREFIX. (ex. /opt/Ice-3.2.0)]),
            [CPPFLAGS="-I$withval/include $CPPFLAGS"
              LDFLAGS="-L$withval/lib -L$withval/lib64 $LDFLAGS"
	     AC_SUBST(SLICE_DIR,"$withval/slice")
             AC_MSG_NOTICE(using Ice include directory... $withval/include)
             AC_MSG_NOTICE(using Ice slice directory... $withval/slice)
             AC_MSG_NOTICE(using Ice library directory... $withval/lib)],
	     AC_SUBST(SLICE_DIR,"/usr/share/Ice/slice"))

dnl Automake time
AM_INIT_AUTOMAKE(AC_PACKAGE_NAME, AC_PACKAGE_VERSION)

dnl Checks for programs.
AC_PROG_CC
AC_PROG_CXX
AC_PROG_INSTALL
AC_PROG_MAKE_SET
AC_PROG_LIBTOOL

dnl Do it early just in case any weird checks require it...
dnl ----------------------------------------------------------
dnl Ripped gloriously from GAIM (http://gaim.sourceforge.net/)
dnl -- gaim/configure.ac --
dnl Thanks to robflynn and Chipx86
dnl ----------------------------------------------------------
case "$host_os" in
	darwin*)
		AC_MSG_CHECKING([for fink])
		if test -d "/sw"; then
			AC_MSG_RESULT([found, adding /sw to search paths])
			CPPFLAGS="$CPPFLAGS -I/sw/include"
			LDFLAGS="$LDFLAGS -L/sw/lib"
		else
			AC_MSG_RESULT([not found])
		fi
	;;
	*)
	;;
esac

dnl Enable 64-bit off_t on *NON* 64-bit platforms.
AC_SYS_LARGEFILE

dnl Set the correct language (C++)
AC_LANG(C++)

dnl Check for slice2cpp
AC_PATH_PROG(SLICE2CPP, slice2cpp)
if test -z $SLICE2CPP; then
	AC_MSG_ERROR([*** OMERO.blitz for C++ requires slice2cpp; (not in path)])
fi

dnl Check for slice2cpp
AC_PATH_PROG(SLICE2JAVA, slice2java)
if test -z $SLICE2JAVA; then
	AC_MSG_ERROR([*** OMERO.blitz for C++ requires slice2java; (not in path)])
fi

dnl Check for -lIce
AC_CHECK_LIB([Ice], [main], TESTLIBS="$TESTLIBS -lIce", [
    AC_MSG_ERROR([*** OMERO.blitz for C++ requires libIce; (library not found) ***])])
AC_CHECK_HEADER(Ice/Ice.h, LIBICE='yes', AC_MSG_ERROR([
*** OMERO.blitz for C++ requires libIce; (Required Ice headers not found) ***]))

dnl Check for -lIceUtil
AC_CHECK_LIB([IceUtil], [main], TESTLIBS="$TESTLIBS -lIceUtil", [
    AC_MSG_ERROR([*** blitz C++ requires libIceUtil; (library not found) ***])])
AC_CHECK_HEADER(IceUtil/UUID.h, LIBICEUTIL='yes', AC_MSG_ERROR([
*** OMERO.blitz for C++ requires libIceUtil; (Required Ice headers not found) ***]))

dnl Check for -lboost_unit_test_framework
AC_CHECK_HEADER([boost/test/auto_unit_test.hpp], 
						  AM_CONDITIONAL(BOOST_TEST,true), 
						  AM_CONDITIONAL(BOOST_TEST,false))
dnl AC_CHECK_LIB([boost_unit_test_framework-gcc-d-1_33_1], [main],
dnl 					      [TESTLIBS="$TESTLIBS -lboost_unit_test_framework-gcc-d-1_33_1"
dnl					      AM_CONDITIONAL(BOOST_TEST, true)],
dnl					      AM_CONDITIONAL(BOOST_TEST, false))

dnl Check for CPPUnit
ifdef([AM_PATH_CPPUNIT], [
			 AM_PATH_CPPUNIT(1.9.6)
			 AM_CONDITIONAL(CPPUNIT, true)], 
			 [AM_CONDITIONAL(CPPUNIT, false)])

AC_CONFIG_FILES([Makefile
		 src/Makefile
		 test/Makefile])
                 
AC_OUTPUT
