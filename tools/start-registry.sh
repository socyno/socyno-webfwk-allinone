docker run -itd --name registry \
    --privileged=true \
    -p 5000:5000 --restart=always \
    -v /opt/registry/:/var/lib/registry \
    registry:2.7.1
