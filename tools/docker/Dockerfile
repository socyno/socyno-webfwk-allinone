FROM tomcat:7.0.104-jdk8-openjdk

ADD start.sh /opt/start.sh
ADD webfwk-*.war "/usr/local/tomcat/webapps/"
CMD [ "bash", "/opt/start.sh" ]
