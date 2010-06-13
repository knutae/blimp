# -*- Mode: python -*-
import glob, os

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
    #ctx.check_tool('msvc')
    ctx.env.BLIMP_ICONS = [x for x in glob.glob('icons/*.png') if 'wix' not in x]
    ctx.env.CLASSPATH = os.pathsep.join(['../swt-3.5.1-win32-win32-x86/swt.jar', '../jiu/jiu.jar'])
    ctx.env.TESTS_CLASSPATH = os.pathsep.join(['default/classes', '../jiu/junit.jar', ctx.env.CLASSPATH])

def build(ctx):
    print 'Build...'
    blimp_sources = ctx.path.ant_glob('src/**/*.java')
    blimp_tests = ctx.path.ant_glob('tests/**/*.java')
    print 'Found %s blimp source files' % len(blimp_sources.split())
    print 'Found %s blimp test files' % len(blimp_tests.split())
    ctx(rule='"${JAVAC}" -sourcepath src -classpath ${CLASSPATH} -d ${TGT} ${SRC}',
        source=blimp_sources,
        target='classes',
        name='src')
    ctx(rule='"${JAVAC}" -sourcepath tests -classpath ${TESTS_CLASSPATH} -d ${TGT} ${SRC}',
        source=blimp_tests,
        target='test_classes',
        after='src')
    
    ctx.install_files('default/classes/resources/images', ctx.env.BLIMP_ICONS)

def test(ctx):
    print 'Test...'
    

def rebuild(ctx):
    import Scripting
    Scripting.commands += ['distclean', 'configure', 'build', 'install']
