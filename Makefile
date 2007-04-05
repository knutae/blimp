# This is a wrapper for ant, mostly for unix systems

ANT = ant -lib ant-jars

compile:
	$(ANT) compile

test:
	$(ANT) tests

run:
	$(ANT) run

install:
	$(ANT) install

clean:
	$(ANT) clean

all: compile test
