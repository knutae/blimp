# -*- Mode: python -*-
APPNAME='blimp'
VERSION='1.2.0-beta'

top = '.'
out = 'build'

def configure(ctx):
    import glob, os
    ctx.env.set_variant('') # avoid 'default' in path
    ctx.check_tool('java')
    # probably an easier way to ensure build/classes exists...
    if not os.path.isdir('build/classes'):
        os.makedirs('build/classes')
    #ctx.check_tool('msvc')
    ctx.env.BLIMP_ICONS = [x for x in glob.glob('icons/*.png') if 'wix' not in x]
    ctx.env.CLASSPATH = os.pathsep.join(['../swt-3.5.1-win32-win32-x86/swt.jar', '../jiu/jiu.jar'])

def build(ctx):
    print 'Build...'
    blimp_sources = ctx.path.ant_glob('src/**/*.java')
    blimp_tests = ctx.path.ant_glob('tests/**/*.java')
    print 'Found %s blimp source files' % len(blimp_sources.split())
    print 'Found %s blimp test files' % len(blimp_tests.split())
    ctx(rule='"${JAVAC}" -sourcepath src -classpath ${CLASSPATH} -d classes ${SRC}',
        source=blimp_sources, target='classes')
    ctx.install_files('classes/resources/images', ctx.env.BLIMP_ICONS)

def rebuild(ctx):
    import Scripting
    Scripting.commands += ['distclean', 'configure', 'build', 'install']
