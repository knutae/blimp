'''
SCons tool for bundling files into the formats zip, tar.gz and tar.bz2.
'''

import os, tarfile, zipfile

def build_tar(target_fname, sources, prefix, mode):
    print 'creating tar file:', target_fname
    tar = tarfile.open(target_fname, mode=mode)
    for src in sources:
        path = str(src)
        tar.add(path, prefix + path)
    tar.close()

def build_zip(target_fname, sources, prefix):
    print 'creating zip file:', target_fname
    zip = zipfile.ZipFile(target_fname, 'w', zipfile.ZIP_DEFLATED)
    for src in sources:
        path = str(src)
        if os.path.isdir(path):
            # Add all files below dir
            for base, dirnames, filenames in os.walk(path):
                for fname in filenames:
                    fpath = os.path.join(base, fname)
                    zip.write(fpath, prefix + fpath)
        else:
            # Normal file
            zip.write(path, prefix + path)
    zip.close()

def build_bundle(target, source, env):
    if env.has_key('BUNDLE_PREFIX'):
        prefix = env['BUNDLE_PREFIX']
    else:
        prefix = ''
    for target_file in target:
        fname = str(target_file)
        if fname.endswith('.zip'):
            build_zip(fname, source, prefix)
        elif fname.endswith('.tar.gz'):
            build_tar(fname, source, prefix, 'w:gz')
        elif fname.endswith('.tar.bz2'):
            build_tar(fname, source, prefix, 'w:bz2')
        else:
            raise Exception('Unknown target file type: %s' % (fname,))
    return None

def print_null(*args):
    pass

def generate(env):
    env['PRINT_CMD_LINE_FUNC'] = print_null # silence printing of all sources
    env['BUILDERS']['Bundle'] = env.Builder(action=build_bundle)

def exists(env):
    return True
