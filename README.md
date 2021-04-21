# Survey Middleware

This repo serves data to the frontend at this repo: https://github.com/Thunderspike/645_Assignment3_Middleware

<br />

In order to have local access to the code of this repo:

1. In a local folder of your choce run `git clone https://github.com/Thunderspike/645_Assignment3_Middleware.git`
2. Open Eclipse EE and select File > Import > Maven > Existing Maven Projects, point the 'Root Directory' input to the internal location of the previously cloned folder
3. Make sure you have provisioned a Tomcat server with Eclipse
    - **Note**: We compiled the project with JDK15 on Tomcat 10, and had trouble deploying the war to a Tomcat 9.0 in Docker, so make sure to heed this if you get stuck
4. Give the project runtime required environmental variables by right clicking on the Project name (645_Assignment3_Middleware) > Run as > Run Configurations

    - In the Create, manage, and run configurations select Apache Tomcat > Tomcat v10.0 Server at localhost (you mighht already be on this screen)
    - On the right side of the screen, select the Environment Tab and values from your RDS provisioning step the following 3 parameters

        - `DB_URL`
            - **Note**: `DB_URL` should have the driver, so the format should be something like: `jdbc:mysql://amazonDBURL:3306/SchemaName`, where amazonDBURL is the url noted from the RDS step from earlier, and SchemaName is the automatic Schema configured in the RDS - you must provide a SchemaName)
        - `DB_PASSWORD`
        - `DB_USER`

    * **Note**: the Environment keys need to be upper case, and the values of these variables need to be the values from the RDS established process, as already mentioned. The application will not be able communicate with a MySQL database without providing these parameters as they are getting overloaded before getting passed to the EntityManager for security reasons discussed in later CI/CD steps (as opposed to being hard coded into the `persirence.xml` file).

5. Run the project by Right clicking on the project > Run As > Run on Server

Running this project will allow the Tomcat webserver to serve Rest methods at `localhost:8080/SurveyMiddleware/api`
The project currently supports the following paths:

-   GET `/surveys` - gets all surveys in the DB and returns them as a JSON array
-   GET `/survey/{id}` - gets a JSON response of the survey with the specified ID or 404 if survey with that id does not exist
-   POST `/survey/{id}` - saves a new new survey to the database as specified in the request's body, or throws validation errors if non-comforming fields are found
-   PUT `/survey/{id}` - updates a survey with the given ID with new data in the DB by specifcying the full survey payload in the body

If the project is running, the following curl command will allow you to get a list of all surveys:

```bash
curl 'localhost:8080/SurveyMiddleware/api/surveys'
```

As previously mentioned, this is a deployable JPA project using Jersey 3.0 and Hibernate. See the `pom.xml` file inside for a list of dependencies. This project was not bootsrapped with Spring - although we wish it were.

The Survey.java is the Java bean which doubles as the Hibernate Entity. `survey` is thus also the name of the table. SurveyAPI. is where the rest logic is found and the rest of the files are validators / utilities.

---

The `Dockerfile` of this project is different from the HW2 because it is split into two steps. The firt step, is based of a Maven image and copies the currenct directory of a project to the workdir location inside the image at `/root/surveyMiddleware` and then compile it with the `mvn compile war:war` command.
The second step pull from the tomcat v10 image, copies the war file produced from waven and serves it from `/usr/local/tomcat/webapps/` directory thus serving it.

As with the previous project, we have established a CI/CD pipleine using git actions. You can find the actions in the `.github\workflows`, but the process is the same as in the last project:

-   we build the Docker image on every commit to the repo
-   after successfully building, we restart the rancher workload responsible for that image with a curl command

The middleware maven/tomcat image lives on [Dockerhub thunderspike/645_assignment3_middleware:latest](https://hub.docker.com/repository/docker/thunderspike/645_assignment3_middleware)
