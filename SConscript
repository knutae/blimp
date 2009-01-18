# Blimp SCons rules for java

Import('dcraw')

import os, sys, platform, glob

env = Environment()
env['JAVA'] = 'java'
env['JVMARGS'] = ''
if os.environ.has_key('JAVA_HOME'):
    java_home = os.environ['JAVA_HOME']
    print '--- Using JAVA_HOME:', java_home
    env['ENV']['PATH'] = os.path.join(java_home, 'bin')

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

jiu_jar = 'jiu-0.14.2/jiu.jar'
xerces_jar = 'jars/xercesImpl.jar'
swt_jar = '/usr/lib/java/swt.jar'
junit_jar = 'junit4.1/junit-4.1.jar'
blimp_jar = env.Jar('build/blimp.jar', class_dir, JARCHDIR = class_dir)

def first_existing(*paths):
    for path in paths:
        if os.path.isfile(path):
            return path
    return paths[-1]

if sys.platform == 'win32':
    # TODO: detect 32/64-bit java VM (or support both somehow)
    swt_jar = 'swt-3.4-win32_64/swt.jar'
else:
    # Search for a usable swt jar (a bit ugly)
    swt_jar = first_existing('/usr/share/java/swt-gtk-3.4.jar', '/usr/share/java/swt.jar', swt_jar)

env.Append(JAVACLASSPATH = [ jiu_jar, swt_jar, junit_jar ])
env.Java(class_dir, 'src', JAVAVERSION='1.6')

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
runner_env.Replace(JAVACLASSPATH = [class_dir, jiu_jar, xerces_jar])
runner_env.Append(BUILDERS = {'RunClass': java_runner})

test_runner_env = runner_env.Clone()
test_runner_env.Append(JAVACLASSPATH = [junit_jar])
test_runner_env.RunClass('org.boblycat.blimp.tests.RunTests')
Alias('test', 'org.boblycat.blimp.tests.RunTests')

swt_runner_env = runner_env.Clone()
swt_runner_env.Append(JAVACLASSPATH = [swt_jar])
swt_runner_env.Append(JVMARGS = ['-Xmx1024M', '-Dblimp.dcraw.path=' + str(dcraw[0])])
if os.path.isdir('/usr/lib/jni'):
    swt_runner_env.Append(JVMARGS = ['-Djava.library.path=/usr/lib/jni'])
swt_runner_env.RunClass('org.boblycat.blimp.gui.swt.MainWindow')
Alias('run', 'org.boblycat.blimp.gui.swt.MainWindow')

def tar_exclude():
   patterns = ['build', '.svn', '*~', 'swt*win32*', 'debian', '.classpath', '.scons*']
   return ' '.join(['--exclude='+pattern for pattern in patterns])

tar = Command('build/blimp-source.tar.gz', [],
    'tar cfvz $TARGET ' + tar_exclude() + ' .')
Alias('tar', tar)

Default(class_dir)

# Export variables for bundling
install_jars = [ blimp_jar, jiu_jar, xerces_jar, swt_jar ]
#Export('install_jars')
Export('blimp_jar', 'jiu_jar', 'xerces_jar', 'swt_jar')

