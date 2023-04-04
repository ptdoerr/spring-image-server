# spring-image-server

I built this application to provide access to some COVID county leval case maps that I built with my COVID Dashboard notebook. There are service endpoints to list 
and display images from an AWS S3 bucket or local file system. There is a basic index.html to access the services and display images. The page is authenticated through GitHub using Oauth.

<img src="src/main/resources/static/66.228.55.215_8080_.png" width="300px"/>

Here's a [link to the running application.](http://66.228.55.215:8080) It's running on Linode instance as a docker image.

## Installation
jdk 17
maven 3

## Running

#### Build and run local server
./mvnw spring-boot:run  

#### Build Docker Image
./mvnw spring-boot:build-image

### Required Environment Variables
