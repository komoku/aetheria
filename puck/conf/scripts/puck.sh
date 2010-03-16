#!/bin/sh
echo "Llamando a la Java (tm) Virtual Machine para ejecutar PUCK..."
java -Xmx512M -classpath PuckCore2.jar:lib/jhall.jar:lib/jhelpaction.jar:AgeCore.jar:lib/bsh-2.0b2.jar:lib/micromod.jar:lib/jsyntaxpane-0.9.5-20100209.jar org.f2o.absurdum.puck.gui.PuckFrame > /dev/null
