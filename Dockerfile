FROM openjdk:8-jre-alpine
WORKDIR /data/wx-demo
COPY target/wx-demo.jar /data/wx-demo/wx-demo.jar
COPY entrypoint.sh /data/wx-demo/entrypoint.sh
RUN chmod +x ./entrypoint.sh
ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 20400
