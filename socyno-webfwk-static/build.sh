#!/bin/bash

export NODE_ENV=production
echo "- node version: $(node -v)"
echo "- npm  version: $(npm -v)"
echo "- yarn version: $(yarn -v)"

yarn install --ignore-optional && npm install --dev && npm run build

