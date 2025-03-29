Front end runs port 3000, SSL/HTTPS
Must have cert.pem and key.pem at the same level as index.js, package.json and package-lock.json
Backend runs port 8443, SSL/HTTPS. There must be signed keystore.jks in src/main/resources in the Backend Folder

Docker-compose.yml will simultaneously manage 3 containers needed for the application: Backend-container, Mysql-container, Redis-container

The Dockerfile will contain the necessary information to build the Backend docker image

Redis runs port 6379

MySQL runs port 3306

The default SQL file will have 1 record in the User table, which is also the Admin Role and 1 default record is detailed information related to top-up payments.
WARNING: This is a development environment and it is not necessary to use environment variables (.env). If necessary, create an .env file and map it to application.properties of the Backend application
The API specification document is located inside /src/main/resource/static

Oops: Edit the necessary configuration information in application.properties
