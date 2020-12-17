docker run -p 3306:3306 --name mysql \
    --restart=always \
    --privileged=true \
    -v /opt/mysql/conf:/etc/mysql \
    -v /opt/mysql/logs:/var/log/mysql \
    -v /opt/mysql/data:/var/lib/mysql \
    -e MYSQL_ROOT_PASSWORD=adminp@ssword \
    -d mysql:5.7
