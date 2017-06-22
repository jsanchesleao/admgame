#!/usr/bin/env bash

export MONGO_URI="mongodb://admgame:admgame@ds125262.mlab.com:25262/admgamedev"

cd $(dirname $0)
cd ../app
../scripts/lein.sh run 