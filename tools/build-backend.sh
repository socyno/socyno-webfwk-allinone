#/bin/bash

TOOLDIR=$(dirname "$0") \
    && cd "$TOOLDIR" \
    && export TOOLDIR="$(pwd)" \
    && cd .. \
    && export WORKSPACE="$(pwd)" \
    && git fetch origin \
    && git reset --hard FETCH_HEAD \
    && git checkout -B master origin/master \
    && export WORKTMPDIR="$WORKSPACE/build" \
    && export MVNSETTINGS="$WORKTMPDIR/maven-settings.xml" \
    && export BUILDSCRIPT="$WORKTMPDIR/build-backend.sh" \
    && mkdir -p "$WORKTMPDIR" \
    && echo '<?xml version="1.0" encoding="UTF-8"?>' >"$MVNSETTINGS" \
    && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >>"$MVNSETTINGS" \
    && echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >>"$MVNSETTINGS" \
    && echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">' >>"$MVNSETTINGS" \
    && echo "    <localRepository>${WORKTMPDIR}/maven-repo/</localRepository>" >>"$MVNSETTINGS" \
    && echo '    <mirrors>' >>"$MVNSETTINGS" \
    && echo '        <mirror>' >>"$MVNSETTINGS" \
    && echo '            <id>nexus-aliyun</id>' >>"$MVNSETTINGS" \
    && echo '            <mirrorOf>central</mirrorOf>' >>"$MVNSETTINGS" \
    && echo '            <name>Nexus aliyun</name>' >>"$MVNSETTINGS" \
    && echo '            <url>http://maven.aliyun.com/nexus/content/groups/public</url>' >>"$MVNSETTINGS" \
    && echo '        </mirror>' >>"$MVNSETTINGS" \
    && echo '    </mirrors>' >>"$MVNSETTINGS" \
    && echo '</settings>' >>"$MVNSETTINGS" \
    && echo '#!/bin/sh' > "$BUILDSCRIPT" \
    && echo "cd '$WORKSPACE' && mvn -s '$MVNSETTINGS' clean install || exit \$?" >> "$BUILDSCRIPT" \
    && echo "rm -f '$WORKTMPDIR'/*.war || exit \$?" >> "$BUILDSCRIPT" \
    && echo "cp -f weimob-webfwk-gateway/target/weimob-webfwk-gateway-*.war '$WORKTMPDIR/webfwk-gateway.war' || exit \$?" >>"$BUILDSCRIPT" \
    && echo "cp -f weimob-webfwk-backend/target/weimob-webfwk-backend-*.war '$WORKTMPDIR/webfwk-backend.war' || exit \$?" >>"$BUILDSCRIPT" \
    && echo "cp -f weimob-webfwk-executor/target/weimob-webfwk-executor-*.war '$WORKTMPDIR/webfwk-executor.war' || exit \$?" >>"$BUILDSCRIPT" \
    && echo "cp -f weimob-webfwk-schedule/target/weimob-webfwk-schedule-*.war '$WORKTMPDIR/webfwk-schedule.war' || exit \$?" >>"$BUILDSCRIPT" \
    && docker run --rm -it -u root --privileged=true -v "$WORKSPACE:$WORKSPACE" maven:3.6.3-jdk-8 /bin/sh "$BUILDSCRIPT" \
    || exit $?

cd "$TOOLDIR/docker" \
    && cp -f "$WORKTMPDIR"/*.war . \
    && IMAGE_TAG_NAME=$(date '+%Y-%m-%d_%H-%M') \
    && IMAGE_REPO_SERVER=webfwk-hub.weimob.com:5000 \
    && IMAGE_FULL_NAME=$IMAGE_REPO_SERVER/webfwk-allinone:$IMAGE_TAG_NAME \
    && docker build -t "$IMAGE_FULL_NAME" . \
    && docker push "$IMAGE_FULL_NAME" \
    && NODE_DEPLOY_HOST=root@webfwk-app.weimob.com \
    && scp -B -o StrictHostKeyChecking=no "$TOOLDIR/start-backend.sh" "$NODE_DEPLOY_HOST:/tmp/start-backend.sh" \
    && ssh -o PasswordAuthentication=no -o StrictHostKeyChecking=no "$NODE_DEPLOY_HOST" "bash /tmp/start-backend.sh '$IMAGE_FULL_NAME'"
STATUS=$?
rm -f *.war
exit $STATUS
