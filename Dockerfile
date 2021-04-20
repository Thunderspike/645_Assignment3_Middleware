FROM maven:3.8.1-openjdk-11-slim as build

LABEL "maintainers"="Pol Ajazi; Flavio Amurrio Moya"

RUN apk --no-cache add curl # adding curl for debugging

WORKDIR /root/surveyMiddleware

COPY . .

RUN mvn compile war:war 

# FROM tomcat:10.0-jdk15
FROM tomcat:10.0.5-jdk15-openjdk-slim-buster

COPY --from=build /root/surveyMiddleware/target/SurveyMiddleware.war /usr/local/tomcat/webapps/

# debugging 
RUN apt-get update;
RUN apt-get install curl
