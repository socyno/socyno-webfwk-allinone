#/bin/bash

export TOMCATDIR=/usr/local/tomcat/ \
    && cd "$TOMCATDIR/bin" \
    && bash ./catalina.sh run
