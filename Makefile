# This is a wrapper for ant, meant for unix systems
# Note: $DESTDIR is used when building Debian packages

BASEDIR = /usr/share/blimp
INSTALLDIR = $(DESTDIR)$(BASEDIR)
USR_BIN = $(DESTDIR)/usr/bin
MAN_DIR = $(DESTDIR)/usr/share/man/man1
ICONDIR = $(INSTALLDIR)/icons
ANT = ant -lib ant-jars
ANT_INSTALL = $(ANT) -Dinstall.home=$(INSTALLDIR) -Dblimp.home=$(BASEDIR) -Ddcraw.install.bin=$(USR_BIN) -Dinstall.man=$(MAN_DIR)
SOURCE_IGNORE = -I.svn -I*-stamp -I_* -Iswt*win32*

# gcj-stuff
GCJ = gcj-4.2
BLIMP_SOURCES_FULL = $(shell find src -name \*.java)
BLIMP_SOURCES = $(filter-out src/org/boblycat/blimp/tests/%,$(BLIMP_SOURCES_FULL))
JIU_SOURCES = $(shell find jiu-0.14.2 -name \*.java)
SWT_JAR = /usr/lib/java/swt.jar
SWT_LIB = /usr/lib/jni/libswt-gtk-3236.so
JUNIT_JAR = junit4.1/junit-4.1.jar
#JARS = $(SWT_JAR) $(JUNIT_JAR)
JARS=$(SWT_JAR)
INC_JARS = $(patsubst %,-I%,$(JARS))
GCJ_OBJ = build/blimp.o build/swt.o

default: all

compile:
	$(ANT) compile

test:
	$(ANT) tests

run:
	$(ANT) run

tar-source:
	$(ANT) tar-source

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
	mkdir -p $(ICONDIR)
	cp icons/blimp-logo-48.png $(ICONDIR)/blimp.png
	convert icons/blimp-logo-32.png $(ICONDIR)/blimp.xpm

compile-dcraw:
	$(ANT) dcraw

compile-java:
	$(ANT) compile-java

build/blimp.o: $(JIU_SOURCES) $(BLIMP_SOURCES)
	$(GCJ) $(INC_JARS) -O1 -c $^ -o $@

build/junit.o: $(JUNIT_JAR)
	$(GCJ) -o $@ -c $^

build/swt.o: $(SWT_JAR)
	$(GCJ) -fjni -o $@ -c $^

list:
	echo $(BLIMP_SOURCES)

build/blimp-gcj: $(GCJ_OBJ)
	$(GCJ) --main=org.boblycat.blimp.gui.swt.MainWindow -o $@ $^ $(SWT_LIB)
	strip $@

uninstall:
	$(ANT_INSTALL) uninstall
	rm -f $(USR_BIN)/blimp

dpkg:
	dpkg-buildpackage -rfakeroot $(SOURCE_IGNORE)

clean:
	$(ANT) clean

all:
	$(ANT) all

