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
          --link mysql:webfwk-db \
          --link mysql:webfwk-db.weimob.com \
          -e CATALINA_OPTS=" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050 " \
          "$1"
