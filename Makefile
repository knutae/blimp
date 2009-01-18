# This is a wrapper for ant, meant for unix systems
# Note: $DESTDIR is used when building Debian packages

BASEDIR = /usr/share/blimp
INSTALLDIR = $(DESTDIR)$(BASEDIR)
USR_BIN = $(DESTDIR)/usr/bin
MAN_DIR = $(DESTDIR)/usr/share/man/man1
ICONDIR = $(INSTALLDIR)/icons
SCONS = scons
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
	$(SCONS)

test:
	$(SCONS) test

run:
	$(SCONS) run

tar-source:
	$(SCONS) tar

install-blimp:
	$(SCONS) build/install
	mkdir -p $(INSTALLDIR)
	cp -a build/install/* $(INSTALLDIR)
	mkdir -p $(MAN_DIR)
	gzip -c -9 tools/bundle-unix/blimp.1 > $(MAN_DIR)/blimp.1.gz
	mkdir -p $(USR_BIN)
	cd $(USR_BIN) ; ln -sf ../share/blimp/bin/blimp
	mkdir -p $(ICONDIR)
	cp icons/blimp-logo-48.png $(ICONDIR)/blimp.png
	convert icons/blimp-logo-32.png $(ICONDIR)/blimp.xpm

compile-dcraw:
	$(SCONS) dcraw

compile-java:
	$(SCONS) build/classes

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

dpkg:
	dpkg-buildpackage -rfakeroot $(SOURCE_IGNORE)

clean:
	rm -rf build

all: compile test
