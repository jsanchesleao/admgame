#!/usr/bin/env bash

export MONGO_URI="mongodb://localhost:27017/admgamedev"

cd $(dirname $0)
cd ../app
../scripts/lein.sh run 
