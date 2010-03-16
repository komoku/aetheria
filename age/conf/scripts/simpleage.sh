#!/bin/sh
echo "Llamando a la Java (tm) Virtual Machine para ejecutar Aetheria..."
java -Xmx512M -classpath AgeCore.jar:lib/bsh-2.0b2.jar:micromod.jar eu.irreality.age.swing.sdi.SwingSDIInterface > /dev/null
