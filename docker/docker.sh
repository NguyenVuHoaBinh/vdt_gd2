#!/bin/bash

# Find all services under docker
SUBDIRS=$(find ./ -mindepth 1 -maxdepth 1 -type d)

# Exec
if [ "$1" == "start" ]; then
    for dir in $SUBDIRS; do
        echo "[Starting Docker][$(basename "$dir")] ===================================="
        cd $dir
        docker-compose up -d
        cd ..
    done
elif [ "$1" == "stop" ]; then
    for dir in $SUBDIRS; do
        echo "[Stopping Docker][$(basename "$dir")] ===================================="
        cd $dir
        docker-compose down
        cd ..
    done
elif [ "$1" == "restart" ]; then
    for dir in $SUBDIRS; do
        echo "[Restarting Docker][$(basename "$dir")] ===================================="
        cd $dir
        docker-compose down
        docker-compose up -d
        cd ..
    done
else
    echo "Usage: ./docker.sh [start|shutdown|restart]"
fi
