FROM java:8
COPY ./target/betbot.jar /usr/app/
EXPOSE  8080
ENTRYPOINT ["java", "-jar", "/usr/app/betbot.jar"]