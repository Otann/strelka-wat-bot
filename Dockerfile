FROM java:8
COPY ./target/strelka-wat-bot.jar /usr/app/
EXPOSE  8080
ENTRYPOINT ["java", "-jar", "/usr/app/strelka-wat-bot.jar"]