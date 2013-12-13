#!/usr/bin/env bash

message=${1:-'foo bar'}

curl \
-v \
-X POST \
--data-urlencode "message=${message}" \
http://localhost:8080/event
