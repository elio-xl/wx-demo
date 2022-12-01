#!/bin/sh

java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Diname=iot-platform ${JVM_CONFIG} -jar wx-demo.jar
