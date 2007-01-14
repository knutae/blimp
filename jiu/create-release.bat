set JIUVER=0.14.0
call create-jiu.jar.bat
echo JIU %JIUVER% class files - website: http://schmidt.devlib.org/jiu/ | zip -z jiu.jar
tar cf java-imaging-utilities-%JIUVER%.tar README LICENSE *.bat packages *options jiu.jar exclude.txt META-INF net doc -X exclude.txt
bzip2 -f -9 java-imaging-utilities-%JIUVER%.tar
