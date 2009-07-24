set JIU=net\sourceforge\jiu\
del jiu.jar
f:\jdk\bin\jar cmf META-INF\MANIFEST.MF jiu.jar %JIU%apps\*.class %JIU%codecs\jpeg\*.class %JIU%codecs\tiff\*.class %JIU%codecs\*.class
f:\jdk\bin\jar uf jiu.jar %JIU%color\adjustment\*.class %JIU%color\analysis\*.class %JIU%color\conversion\*.class %JIU%color\data\*.class
f:\jdk\bin\jar uf jiu.jar %JIU%color\dithering\*.class %JIU%color\io\*.class %JIU%color\promotion\*.class %JIU%color\quantization\*.class 
f:\jdk\bin\jar uf jiu.jar %JIU%color\reduction\*.class %JIU%color\*.class %JIU%data\*.class %JIU%filters\*.class %JIU%geometry\*.class 
f:\jdk\bin\jar uf jiu.jar %JIU%gui\awt\dialogs\*.class %JIU%gui\awt\*.class %JIU%ops\*.class %JIU%util\*.class 
f:\jdk\bin\jar uf jiu.jar resources\images\* resources\lang\*.txt
