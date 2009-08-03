'''
Use the Eclipse java compiler from the command-line (like javac).
Set ECLIPSE_HOME in the OS or scons environment to make this work.
This is still experimental, but seems to work...
'''
import os, glob

def find_eclipse_core_jar(env):
    if env.has_key('ECLIPSE_HOME'):
        eclipse_home = env['ECLIPSE_HOME']
    elif os.environ.has_key('ECLIPSE_HOME'):
        eclipse_home = os.environ['ECLIPSE_HOME']
    else:
        return False
    jars = glob.glob(os.path.join(eclipse_home, 'plugins', 'org.eclipse.jdt.core_*.jar'))
    if jars:
        return jars[0]
    else:
        return False

def generate(env):
    jar = find_eclipse_core_jar(env)
    env['JAVAC'] = 'java -classpath %s org.eclipse.jdt.internal.compiler.batch.Main' % jar
    # Have to change JAVACCOM to use a tempfile, but only for sources, not the whole java command
    # Assume that _JAVACCOM defines the normal command.
    env['JAVACCOM'] = env['_JAVACCOM'].replace('$SOURCES', "${TEMPFILE('$SOURCES')}")

def exists(env):
    return find_eclipse_core_jar(env)
