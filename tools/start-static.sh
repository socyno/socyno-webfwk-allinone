#/bin/bash
docker rm -vf webfwk-static
docker run -d -p 8090:80 -p 8443:443 \
           --restart=always \
           -m 512M --memory-swap -1 \
           --privileged=true \
           --name webfwk-static \
           --link webfwk-app:webfwk-app \
           --link webfwk-app:webfwk-app.weimob.com \
           "$1"
