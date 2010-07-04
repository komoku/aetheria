#!/bin/sh
echo "Llamando a la Java (tm) Virtual Machine para ejecutar PUCK..."
java -Xmx512M -classpath PuckCore2.jar:lib/jhall.jar:lib/jhelpaction.jar:AgeCore.jar:lib/bsh-2.0b4.jar:lib/micromod.jar:lib/jsyntaxpane-0.9.5-20100209.jar:lib/commons-logging-api.jar:lib/jl1.0.jar:lib/jogg-0.0.7.jar:lib/jorbis-0.0.15.jar:lib/jspeex0.9.7.jar:lib/tritonus_share.jar:lib/vorbisspi1.0.2.jar:lib/mp3spi1.9.4.jar:lib/basicplayer3.0.jar:lib/svgSalamander.jar:lib/commons-cli-1.2.jar org.f2o.absurdum.puck.gui.PuckFrame > /dev/null
