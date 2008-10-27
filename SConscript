# Blimp SCons rules for java

import os, sys

env = Environment()
if os.environ.has_key('JAVA_HOME'):
	java_home = os.environ['JAVA_HOME']
	print '--- Using JAVA_HOME:', java_home
	env['ENV']['PATH'] = os.path.join(java_home, 'bin')

jiu_jar = 'jiu-0.14.2/jiu.jar'
xerces_jar = 'jars/xercesImpl.jar'
swt_jar = '/usr/lib/java/swt.jar'
junit_jar = 'junit4.1/junit-4.1.jar'

if sys.platform == 'win32':
	# TODO: detect 32/64-bit java VM (or support both somehow)
	swt_jar = 'swt-3.4-win32_64/swt.jar'

env.Append(JAVACLASSPATH = [ jiu_jar, swt_jar, junit_jar ])
env.Java('build', 'src')

def emit_java_runner(target, source, env):
	assert len(target) == 1
	assert len(source) == 1
	# The source is the full class name (org.example.foo.MyClass)
	# The target is garbage (org.example.foo)
	# Quirky hack: return the source as the target and discard the old target
	return source, []

def generate_java_runner(source, target, env, for_signature):
	assert len(target) == 1
	mainclass = str(target[0])
	classpath = os.pathsep.join(env['JAVACLASSPATH'])
	cmd = 'java -classpath ' + classpath + ' ' + mainclass
	return cmd

java_runner = Builder(
	generator = generate_java_runner,
	emitter = emit_java_runner)

runner_env = env.Clone()
runner_env.Replace(JAVACLASSPATH = ['build', jiu_jar, xerces_jar])
runner_env.Append(BUILDERS = {'RunClass': java_runner})

test_runner_env = runner_env.Clone()
test_runner_env.Append(JAVACLASSPATH = [junit_jar])
test_runner_env.RunClass('org.boblycat.blimp.tests.RunTests')
Alias('test', 'org.boblycat.blimp.tests.RunTests')

swt_runner_env = runner_env.Clone()
swt_runner_env.Append(JAVACLASSPATH = [swt_jar])
swt_runner_env.RunClass('org.boblycat.blimp.gui.swt.MainWindow')
Alias('run', 'org.boblycat.blimp.gui.swt.MainWindow')

Default('build', 'test')
