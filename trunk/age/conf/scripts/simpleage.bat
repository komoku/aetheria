echo off
echo Llamando a la Java (tm) Virtual Machine para ejecutar Aetheria...
REM java -Xmx256M -jar AgeCore.jar > NUL
java -Xmx512M -classpath AgeCore.jar;lib/bsh-2.0b2.jar;lib/micromod.jar;lib/commons-logging-api.jar;lib/jl1.0.jar;lib/jogg-0.0.7.jar;lib/jorbis-0.0.15.jar;lib/jspeex0.9.7.jar;lib/tritonus_share.jar;lib/vorbisspi1.0.2.jar;lib/mp3spi1.9.4.jar eu.irreality.age.swing.sdi.SwingSDIInterface > NUL