docker run -p 3306:3306 --name mysql \
    --restart=always \
    --privileged=true \
    -v /opt/mysql/conf:/etc/mysql \
    -v /opt/mysql/logs:/var/log/mysql \
    -v /opt/mysql/data:/var/lib/mysql \
    -e MYSQL_ROOT_PASSWORD=adminp@ssword \
    -d mysql:5.7 \
    --character-set-server=utf8mb4 \
    --collation-server=utf8mb4_general_ci \
    --sql-mode STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
    

