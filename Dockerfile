FROM maven:3.5.0-jdk-8-alpine

WORKDIR /app
ADD . /app

EXPOSE 8080

ENTRYPOINT ["mvn"]
CMD ["spring-boot:run"]
