# -*- Mode: python -*-
import glob, os, sys, subprocess

APPNAME='blimp'
VERSION='1.2.0-beta'

top = '.'
out = 'build'

def configure(ctx):
    ctx.check_tool('java')
    # There's probably a better way to ensure class output dirs exist
    for classdir in ['classes', 'test_classes']:
        path = os.path.join(out, 'default', classdir)
        if not os.path.isdir(path):
            os.makedirs(path)
    ctx.env.BLIMP_ICONS = [x for x in glob.glob('icons/*.png') if 'wix' not in x]
    
    # Jars / classpath
    jiu_jar = 'jiu/jiu.jar'
    junit_jar = 'jiu/junit.jar'
    if sys.platform == 'win32':
        swt_jar = 'swt-3.5.1-win32-win32-x86/swt.jar'
        if not os.path.exists(swt_jar):
            subprocess.check_call([sys.executable, 'tools/prepare_swt.py'])
    else:
        swt_jar = '/usr/lib/java/swt-gtk-3.5.jar'
    
    ctx.env.CLASSPATH = os.pathsep.join(['../' + x for x in [swt_jar, jiu_jar]])
    ctx.env.TESTS_CLASSPATH = os.pathsep.join(['default/classes', '../' + junit_jar, ctx.env.CLASSPATH])
    ctx.env.RUN_TESTS_CLASSPATH = os.pathsep.join(['default/test_classes', ctx.env.TESTS_CLASSPATH])
    ctx.env.store('build/config.env')

def build(ctx):
    #print 'Build...'
    blimp_sources = ctx.path.ant_glob('src/**/*.java')
    blimp_tests = ctx.path.ant_glob('tests/**/*.java')
    #print 'Found %s blimp source files' % len(blimp_sources.split())
    #print 'Found %s blimp test files' % len(blimp_tests.split())
    ctx(rule='${JAVAC} -sourcepath src -classpath ${CLASSPATH} -d ${TGT} ${SRC}',
        source=blimp_sources,
        target='classes',
        name='classes',
        shell=False)
    ctx(rule='${JAVAC} -sourcepath tests -classpath ${TESTS_CLASSPATH} -d ${TGT} ${SRC}',
        source=blimp_tests,
        target='test_classes',
        name='test_classes',
        after='classes',
        shell=False)
    ctx(rule='${JAR} cf ${TGT} -C ${SRC} .',
        source='classes',
        target='blimp.jar',
        after='classes',
        shell=False)
    #ctx(rule='${JAVA} -classpath ${RUN_TESTS_CLASSPATH} org.boblycat.blimp.tests.RunTests',
    #    after='test_classes',
    #    always=True,
    #    shell=False)
    
    ctx.install_files('default/classes/resources/images', ctx.env.BLIMP_ICONS)

def test(ctx):
    import Environment
    env = Environment.Environment()
    env.load('build/config.env')
    cmd = [env.JAVA, '-classpath', env.RUN_TESTS_CLASSPATH, 'org.boblycat.blimp.tests.RunTests']
    #print cmd
    here = os.getcwd()
    try:
        os.chdir('build')
        subprocess.check_call([
            env.JAVA, '-classpath', env.RUN_TESTS_CLASSPATH,
            'org.boblycat.blimp.tests.RunTests'])
    finally:
        os.chdir(here)

def rebuild(ctx):
    import Scripting
    Scripting.commands += ['distclean', 'configure', 'build', 'install']

def all(ctx):
    import Scripting
    Scripting.commands += ['configure', 'build', 'install', 'test']
