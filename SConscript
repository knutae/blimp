# -*- Mode: python -*-
# Blimp SCons rules for java

Import('dcraw', 'BLIMP_VERSION')

import os, sys, platform, glob, subprocess

if os.environ.has_key('JAVA_HOME'):
    java_home = os.environ['JAVA_HOME']
    print '--- Using JAVA_HOME:', java_home
    # To ensure that all java tools (such as jar) are detected, the
    # path has to be initialized when creating the environment.
    env = Environment(ENV = {'PATH': os.path.join(java_home, 'bin')})
else:
    # Create a default environment and hope that jdk tools are in the path
    env = Environment()

env['JAVA'] = 'java'
env['JVMARGS'] = ''

def copy_os_env(key):
    if os.environ.has_key(key):
        env['ENV'][key] = os.environ[key]

# Needed for "scons run" to work on unix systems
copy_os_env('DISPLAY')
copy_os_env('XAUTHORITY')

class_dir = 'build/classes'

# Install image resources to be included in blimp.jar
# Include all PNGs except those named something with 'wix'
env.Install(
    class_dir + '/resources/images',
    [x for x in glob.glob('icons/*.png') if 'wix' not in x])

jiu_jar = 'jiu/jiu.jar'
if not os.path.exists(jiu_jar):
    # Prepare jiu through a script
    import subprocess
    subprocess.check_call([sys.executable, 'tools/prepare_jiu.py'])

# Surprisingly, the Eclipse compiler seems to produce significantly faster
# code than a standard jdk6 javac, so try to use it if possible.
if os.environ.has_key('ECLIPSE_HOME'):
    # try to use eclipse java compiler
    # the eclipse_javac tool is prepared as part of JIU
    env.Tool('eclipse_javac', ['jiu/site_scons/site_tools'])
    env['JAVACFLAGS'] = '-1.5'

swt_jar = '/usr/lib/java/swt.jar'
junit_jar = 'jiu/junit.jar' # Use junit.jar prepared by JIU build
blimp_jar = env.Jar('build/blimp.jar', class_dir, JARCHDIR = class_dir)

def first_existing(*paths):
    for path in paths:
        if os.path.isfile(path):
            return path
    return paths[-1]

if sys.platform == 'win32':
    # TODO: detect 32/64-bit java VM (or support both somehow)
    swt_jar = 'swt-3.5.1-win32-win32-x86/swt.jar'
    if not os.path.exists(swt_jar):
        subprocess.check_call([sys.executable, 'tools/prepare_swt.py'])
elif sys.platform == 'darwin':
    #swt_jar = 'swt-3.4-carbon-macosx/swt.jar'
    swt_jar = 'swt-3.3.2-carbon-macosx/swt.jar'
else:
    # Search for a usable swt jar (a bit ugly)
    swt_jar = first_existing('/usr/lib/java/swt-gtk-3.5.jar', '/usr/share/java/swt-gtk-3.4.jar', '/usr/share/java/swt.jar', swt_jar)

env.Append(JAVACLASSPATH = [ jiu_jar, swt_jar, junit_jar ])
env.Java(class_dir, 'src', JAVAVERSION='1.6')

test_env = env.Clone()
test_env.Append(JAVACLASSPATH = [ class_dir ])
test_env.Java(class_dir, 'tests', JAVAVERSION='1.6')

def emit_java_runner(target, source, env):
    assert len(target) == 1
    assert len(source) == 1
    # The source is the full class name (org.example.foo.MyClass)
    # The target is garbage (org.example.foo)
    # Quirky hack: return the source as the target and discard the old target
    return source, []

java_runner = Builder(
    action = '$JAVA $JVMARGS $_JAVACLASSPATH $TARGET',
    emitter = emit_java_runner)

runner_env = env.Clone()
runner_env.Replace(JAVACLASSPATH = [class_dir, jiu_jar])
runner_env.Append(BUILDERS = {'RunClass': java_runner})

swt_runner_env = runner_env.Clone()
swt_runner_env.Append(JAVACLASSPATH = [swt_jar])
swt_runner_env.Append(JVMARGS = ['-Xmx1024M', '-Dblimp.dcraw.path=' + str(dcraw[0])])
if os.path.isdir('/usr/lib/jni'):
    swt_runner_env.Append(JVMARGS = ['-Djava.library.path=/usr/lib/jni'])
run_blimp = swt_runner_env.RunClass('org.boblycat.blimp.gui.swt.MainWindow')
Depends(run_blimp, class_dir)
Alias('run', run_blimp)

test_runner_env = swt_runner_env.Clone()
test_runner_env.Append(JAVACLASSPATH = [junit_jar])
run_tests = test_runner_env.RunClass('org.boblycat.blimp.tests.RunTests')
Depends(run_tests, class_dir)
Alias('test', run_tests)

def build_tar(target, source, env):
    import tarfile
    assert len(target) == 1
    if env.has_key('TAR_PREFIX'):
        prefix = env['TAR_PREFIX']
    else:
        prefix = None
    tar = tarfile.open(str(target[0]), mode='w:gz')
    for src in source:
        path = str(src)
        if prefix:
            tar.add(path, os.path.join(prefix, path))
        else:
            tar.add(path)
    tar.close()
    return None

def versioned_mercurial_files():
    '''Generates a list of versioned files (in mercurial).'''
    import subprocess
    cmd = 'hg status -c -m -a -n'.split()
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    for line in p.stdout:
        yield line.strip()

def modify_tar_sources(target, source, env):
    import re
    new_source = []
    re_exclude = re.compile('swt.*win32')
    for filepath in versioned_mercurial_files():
        if re_exclude.search(filepath):
            continue
        new_source.append(filepath)
    return target, sorted(new_source)

tar_builder = Builder(action = build_tar, suffix = 'tar.gz', emitter = modify_tar_sources)
env['BUILDERS']['SourceTar'] = tar_builder
tar = env.SourceTar('build/blimp-' + BLIMP_VERSION, TAR_PREFIX='blimp-' + BLIMP_VERSION)
Alias('tar', tar)

Default(class_dir)

# Export variables for bundling
install_jars = [ blimp_jar, jiu_jar, swt_jar ]
#Export('install_jars')
Export('blimp_jar', 'jiu_jar', 'swt_jar')

