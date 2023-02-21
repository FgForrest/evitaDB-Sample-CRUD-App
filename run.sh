#!/bin/bash

name='evitaDB'

if ! docker ps -f "name=$name" --format '{{.Names}}' | grep -w $name &> /dev/null; then
  if docker ps --all -f "name=$name" --format '{{.Names}}'  | grep -w $name &> /dev/null; then
    echo "Starting container '$name'"
    docker start $name &> /dev/null && sleep 2
  else
    echo "Creating container '$name'"
    docker run -d --name="$name" --net=host -v "./data:/evita/data" index.docker.io/evitadb/evitadb:latest && sleep 2
  fi
fi

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000 -jar target/evita-crud.jar

echo "Stopping container '$name'"
docker stop $name &> /dev/null