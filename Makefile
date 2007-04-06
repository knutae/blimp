# This is a wrapper for ant, meant for unix systems
# Note: $DESTDIR is used when building Debian packages

BASEDIR = /usr/share/blimp
INSTALLDIR = $(DESTDIR)$(BASEDIR)
USR_BIN = $(DESTDIR)/usr/bin
MAN_DIR = $(DESTDIR)/usr/share/man/man1
ANT = ant -lib ant-jars
ANT_INSTALL = $(ANT) -Dinstall.home=$(INSTALLDIR) -Dblimp.home=$(BASEDIR) -Ddcraw.install.bin=$(USR_BIN) -Dinstall.man=$(MAN_DIR)

default: all

compile:
	$(ANT) compile

test:
	$(ANT) tests

run:
	$(ANT) run

install:
	$(ANT_INSTALL) install
	mkdir -p $(USR_BIN)
	cd $(USR_BIN) ; ln -sf ../share/blimp/bin/blimp

install-dcraw:
	$(ANT_INSTALL) install-dcraw

install-blimp:
	$(ANT_INSTALL) install-blimp
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
	dpkg-buildpackage -rfakeroot -I.svn

clean:
	$(ANT) clean

all:
	$(ANT) all

