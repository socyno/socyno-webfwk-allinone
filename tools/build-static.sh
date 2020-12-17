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
    && export BUILDSCRIPT="$WORKTMPDIR/build-static.sh" \
    && mkdir -p "$WORKTMPDIR" \
    && echo '#!/bin/sh' > "$BUILDSCRIPT" \
    && echo 'npm config set registry https://registry.npm.taobao.org' >> "$BUILDSCRIPT" \
    && echo "npm config set cache '$WORKTMPDIR/npm-cached'" >> "$BUILDSCRIPT" \
    && echo "cd '$WORKSPACE/socyno-webfwk-static/' || exit \$?" >> "$BUILDSCRIPT" \
    && echo 'export NODE_ENV=production || exit $?' >> "$BUILDSCRIPT" \
    && echo 'yarn install --ignore-optional && npm install --dev && npm run build || exit $?' >> "$BUILDSCRIPT" \
    && echo "cd dist && tar -cf '$WORKTMPDIR/webfwk-static.tar' * || exit \$?" >> "$BUILDSCRIPT" \
    && docker run --rm -it -u root --privileged=true -v "$WORKSPACE:$WORKSPACE" node:12.18.1-alpine /bin/sh "$BUILDSCRIPT" \
    || exit $?
cd "$TOOLDIR/docker" \
    && cp -f "$TOOLDIR"/cert-*.pem . \
    && cp -f "$WORKTMPDIR/webfwk-static.tar" . \
    && IMAGE_TAG_NAME=$(date '+%Y-%m-%d_%H-%M') \
    && IMAGE_REPO_SERVER=webfwk-hub.socyno.org:5000 \
    && IMAGE_FULL_NAME=$IMAGE_REPO_SERVER/webfwk-static:$IMAGE_TAG_NAME \
    && docker build -t "$IMAGE_FULL_NAME" -f Dockerfile-static  . \
    && docker push "$IMAGE_FULL_NAME" \
    && NODE_DEPLOY_HOST=root@webfwk-app.socyno.org \
    && scp -B -o StrictHostKeyChecking=no "$TOOLDIR/start-static.sh" "$NODE_DEPLOY_HOST":/tmp/start-static.sh \
    && ssh -o PasswordAuthentication=no -o StrictHostKeyChecking=no "$NODE_DEPLOY_HOST" "bash /tmp/start-static.sh '$IMAGE_FULL_NAME'"
STATUS=$?
rm -f webfwk-static.tar cert-*.pem
exit $STATUS
