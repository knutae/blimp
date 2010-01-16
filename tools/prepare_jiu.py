#!/usr/bin/env python

import os, sys, subprocess, shutil

# Public repos
JIU_REPOS = 'http://jiu.hg.sourceforge.net:8000/hgroot/jiu/jiu'
JIU_LOCAL_DIR = 'jiu'
SCONS = 'scons'

def find_scons_bat():
    base = sys.exec_prefix
    for path in [os.path.join(base, 'Scripts'), base]:
        scons = os.path.join(path, 'scons.bat')
        if os.path.exists(scons):
            return scons
    return 'scons.bat'

if sys.platform == 'win32':
    SCONS = find_scons_bat()

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
        if os.path.isdir('classes'):
            # Workaround for scons bug (DirNodeInfo instance has no attribute 'csig')
            shutil.rmtree('classes')
        subprocess.check_call([SCONS, 'jiu.jar'])
    finally:
        os.chdir(here)

def fetch_and_build():
    fetch()
    build()

if __name__ == '__main__':
    fetch_and_build()
