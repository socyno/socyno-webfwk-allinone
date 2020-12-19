#/bin/bash

docker rm -vf webfwk-app
mkdir -p /opt/log/tomcat \
    && mkdir -p /opt/log/webfwk \
    && docker run -d \
          -p 8080:8080 \
          -p 5050:5050 \
          -m 1024M --memory-swap -1 \
          --restart=always \
          --privileged=true \
          --name webfwk-app \
          -v /opt/log/tomcat:/usr/local/tomcat/logs \
          -v /opt/log/webfwk:/opt/log/ \
          --add-host webfwk-db:103.39.217.201 \
          --add-host webfwk-db.socyno.org:103.39.217.201 \
          -e CATALINA_OPTS=" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5050 " \
          "$1"
