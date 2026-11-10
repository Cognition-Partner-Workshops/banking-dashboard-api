#!/usr/bin/env bash
# Builds the Docker image and checks that it runs as a non-root user and can read the app jar.
set -euo pipefail
cd "$(dirname "$0")/.."
image="${IMAGE:-banking-dashboard-api:verify}"
docker build -q -t "$image" . >/dev/null
uid="$(docker run --rm --entrypoint id "$image" -u)"
user="$(docker inspect -f '{{.Config.User}}' "$image")"
if [[ "$uid" == "0" || -z "$user" || "$user" == "root" || "$user" == "0" ]]; then
  echo "FAIL $image runs as root (id -u=$uid, Config.User='$user')"
  exit 1
fi
docker run --rm --entrypoint sh "$image" -c 'test -r /app/app.jar && java -version' >/dev/null 2>&1 \
  || { echo "FAIL $image: app.jar not readable or java not runnable as uid $uid"; exit 1; }
echo "PASS $image runs as uid $uid (Config.User=$user)"
