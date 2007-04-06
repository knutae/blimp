# This is a wrapper for ant, meant for unix systems
# Note: $DESTDIR is used when building Debian packages

BASEDIR = /usr/lib/blimp
INSTALLDIR = $(DESTDIR)$(BASEDIR)
USR_BIN = $(DESTDIR)/usr/bin
ANT = ant -lib ant-jars
ANT_INSTALL = $(ANT) -Dinstall.home=$(INSTALLDIR) -Dblimp.home=$(BASEDIR)

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
	cd $(USR_BIN) ; ln -sf ../lib/blimp/bin/blimp

uninstall:
	$(ANT_INSTALL) uninstall
	rm -f $(USR_BIN)/blimp

clean:
	$(ANT) clean

all:
	$(ANT) all

