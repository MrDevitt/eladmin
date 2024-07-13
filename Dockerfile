FROM --platform=linux/amd64 openjdk:11.0.11-jre
ARG JAR_FILE=./eladmin-system/target/eladmin-system-2.7.jar
COPY ${JAR_FILE} app.jar
RUN ls -lh
ENV TZ=Asia/Shanghai
EXPOSE 8000
ENTRYPOINT ["java","-jar","/app.jar"]