echo off
echo Llamando a la Java (tm) Virtual Machine para ejecutar Aetheria...
REM java -Xmx256M -jar AgeCore.jar > NUL
java -Xmx512M -classpath AgeCore.jar;lib/bsh-2.0b2.jar;micromod.jar eu.irreality.age.SwingAetheriaGameLoaderInterface > NUL