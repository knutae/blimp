#!/usr/bin/env python

import os, sys, subprocess

# Public repos
JIU_REPOS = 'http://jiu.hg.sourceforge.net:8000/hgroot/jiu/jiu'
JIU_LOCAL_DIR = 'jiu'
SCONS = 'scons'

if sys.platform == 'win32':
    SCONS = 'scons.bat'

def fetch():
    if os.path.isdir(JIU_LOCAL_DIR):
        print 'Updating', JIU_LOCAL_DIR
        here = os.getcwd()
        try:
            os.chdir(JIU_LOCAL_DIR)
            subprocess.check_call(['hg', 'pull', '-u'])
        finally:
            os.chdir(here)
    else:
        print 'Cloning', JIU_REPOS
        subprocess.check_call(['hg', 'clone', JIU_REPOS, JIU_LOCAL_DIR])
    assert os.path.isdir(JIU_LOCAL_DIR)
    assert os.path.isdir(JIU_LOCAL_DIR + '/.hg')

def build():
    here = os.getcwd()
    try:
        os.chdir(JIU_LOCAL_DIR)
        subprocess.check_call([SCONS, 'jiu.jar'])
    finally:
        os.chdir(here)

def fetch_and_build():
    fetch()
    build()

if __name__ == '__main__':
    fetch_and_build()
