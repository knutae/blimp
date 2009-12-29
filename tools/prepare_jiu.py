#!/usr/bin/env python

import os, subprocess

# Public repos
JIU_REPOS = 'http://jiu.hg.sourceforge.net:8000/hgroot/jiu/jiu'
JIU_LOCAL_DIR = 'jiu'

def main():
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

if __name__ == '__main__':
    main()
