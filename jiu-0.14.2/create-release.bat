set JIUVER=0.14.2
call create-api-docs.bat
call create-jiu.jar.bat
echo JIU %JIUVER% class files - website: http://schmidt.devlib.org/jiu/ | zip -z jiu.jar
tar -c --group 0 --owner 0 -o -f java-imaging-utilities-%JIUVER%.tar LICENSE README *.bat packages *options jiu.jar exclude.txt META-INF resources net doc -X exclude.txt
bzip2 -f -9 java-imaging-utilities-%JIUVER%.tar
