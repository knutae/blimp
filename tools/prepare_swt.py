#!/usr/bin/env python

import os, sys, urllib, zipfile

ECLIPSE_MIRROR = 'http://filemirror.hu/pub/'

def prepare_swt(version, rev, variant, platform, arch):
    keys = {
        'VERSION': version,
        'REV': rev,
        'VARIANT': variant,
        'PLATFORM': platform,
        'ARCH': arch,
    }
    dirname = 'swt-%(VERSION)s-%(VARIANT)s-%(PLATFORM)s-%(ARCH)s' % keys
    filename = dirname + '.zip'
    if not os.path.exists(filename):
        url = ECLIPSE_MIRROR + ('eclipse/downloads/drops/R-%(VERSION)s-%(REV)s/' % keys) + filename
        urllib.urlretrieve(url, filename)
    assert os.path.exists(filename)
    if not os.path.isdir(dirname):
        os.mkdir(dirname)
        zip = zipfile.ZipFile(filename, 'r')
        for filename in zip.namelist():
            extract_path = os.path.normpath(dirname + '/' + filename)
            if filename.endswith('/'):
                # Directory
                if not os.path.isdir(extract_path):
                    os.makedirs(extract_path)
            else:
                # File
                #print filename, '->', extract_path
                data = zip.read(filename)
                open(extract_path, 'wb').write(data)
        zip.close()

if __name__ == '__main__':
    if sys.platform == 'win32':
        prepare_swt('3.5.1', '200909170800', 'win32', 'win32', 'x86_64')
        prepare_swt('3.5.1', '200909170800', 'win32', 'win32', 'x86')
