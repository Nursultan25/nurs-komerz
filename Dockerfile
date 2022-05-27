FROM openjdk:11
EXPOSE 8083
ADD target/MoneyTransfer-0.0.1-SNAPSHOT.jar nurs-komerz.jar
ENTRYPOINT ["java", "-jar", "nurs-komerz.jar"]