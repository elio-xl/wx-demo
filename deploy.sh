#!/bin/sh

rm -rf wx-demo && mkdir wx-demo
cp Dockerfile Jenkinsfile docker-compose.yml  entrypoint.sh wx-demo.jar wx-demo/
tar -zcvf wx-demo-$(date "+%Y%m%d%H%M%S").tar wx-demo
mv wx-demo-*.tar ../backup/
rm -rf wx-demo

CONTAINER=$(docker ps -a | grep wx-demo |awk "{print $1}")
IMAGES=$(docker images | grep wx-demo |awk "{print $1}")

if [ ! $CONTAINER ]; then
  CONTAINER="1"
else
  echo 'CONTAINER: $CONTAINER'
fi

if [ ! $IMAGES ]; then
  IMAGES="1"
else
  echo 'IMAGES: $IMAGES'
fi

docker stop $CONTAINER && docker rm $CONTAINER
docker rmi $IMAGES
