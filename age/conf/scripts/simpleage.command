#!/bin/sh
here="`dirname \"$0\"`"
echo "Cambiando directorio a $here"
cd "$here" || exit 1
echo "Llamando a la Java (tm) Virtual Machine para ejecutar Aetheria..."
java -Xmx512M -classpath AgeCore.jar:lib/bsh-2.0b4.jar:lib/micromod.jar:lib/commons-logging-api.jar:lib/jl1.0.jar:lib/jogg-0.0.7.jar:lib/jorbis-0.0.15.jar:lib/jspeex0.9.7.jar:lib/tritonus_share.jar:lib/vorbisspi1.0.2.jar:lib/mp3spi1.9.4.jar:lib/basicplayer3.0.jar:lib/commons-cli-1.2.jar eu.irreality.age.swing.sdi.SwingSDIInterface > /dev/null
