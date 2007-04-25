SUBDIR= src/cpp test/cpp
DISTCLEANFILES = \
	config.cache \
	*.loT
MAINTAINERCLEANFILES = \
	aclocal.m4 \
	autoscan.log \
	compile \
	config.guess \
	config.h.in \
	config.sub \
	configure.scan \
	depcomp \
	install-sh \
	ltmain.sh \
	missing \
	mkinstalldirs

all : slice src test

slice: force
	@date
	cd src/cpp ; $(MAKE) $(MFLAGS) slice
	@date

src: force
	@date
	cd src/cpp ; $(MAKE) $(MFLAGS)
	@date

test: force
	@date
	cd test/cpp ; $(MAKE) $(MFLAGS)
	@date

force:
	true