#!/usr/bin/env bash

export MONGO_URI="mongodb://localhost:27017/admgame"

cd $(dirname $0)
cd ../app
../scripts/lein.sh run 