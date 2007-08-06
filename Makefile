# This is a wrapper for ant, meant for unix systems
# Note: $DESTDIR is used when building Debian packages

BASEDIR = /usr/share/blimp
INSTALLDIR = $(DESTDIR)$(BASEDIR)
USR_BIN = $(DESTDIR)/usr/bin
MAN_DIR = $(DESTDIR)/usr/share/man/man1
ANT = ant -lib ant-jars
ANT_INSTALL = $(ANT) -Dinstall.home=$(INSTALLDIR) -Dblimp.home=$(BASEDIR) -Ddcraw.install.bin=$(USR_BIN) -Dinstall.man=$(MAN_DIR)
SOURCE_IGNORE = -I.svn -I*-stamp -I_* -Iswt*win32*

default: all

compile:
	$(ANT) compile

test:
	$(ANT) tests

run:
	$(ANT) run

install-dcraw:
	$(ANT_INSTALL) install-dcraw
	mkdir -p $(MAN_DIR)
	gzip -c -9 tools/bundle-unix/blimp-dcraw.1 > $(MAN_DIR)/blimp-dcraw.1.gz

install-blimp:
	$(ANT_INSTALL) install-unix
	mkdir -p $(MAN_DIR)
	gzip -c -9 tools/bundle-unix/blimp.1 > $(MAN_DIR)/blimp.1.gz
	mkdir -p $(USR_BIN)
	cd $(USR_BIN) ; ln -sf ../share/blimp/bin/blimp

compile-dcraw:
	$(ANT) dcraw

compile-java:
	$(ANT) compile-java

uninstall:
	$(ANT_INSTALL) uninstall
	rm -f $(USR_BIN)/blimp

dpkg:
	dpkg-buildpackage -rfakeroot $(SOURCE_IGNORE)

clean:
	$(ANT) clean

all:
	$(ANT) all

