# Camel Assistant UI (Vaadin)

This project is a Vaadin Flow application for Quarkus.

Quarkus 3.0+ requires Java 17.

## Running the Application

Import the project to the IDE of your choosing as a Maven project. 

Run application using `mvn quarkus:dev`.

Open [http://localhost:8081/](http://localhost:8081/) in browser.

If you want to run your app locally in production mode, call `mvn package -Pproduction`
and then
```
java -jar target/quarkus-app/quarkus-run.jar
```