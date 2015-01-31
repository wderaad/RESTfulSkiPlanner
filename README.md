# RESTfulSkiPlace
A simple REST application for basic contact management (See the Wiki for more detailed information)

## Team: Pure Freaky Magic (PFM)

## Framework Configuration
* Stripes Framework
* Jetty
* Hibernate
* HSQLDB
* JQuery
* JQuery UI


## Build/Compile
ant build

## Run Test Database
ant TestServer

## Run Production Database
ant ProdServer

## Run Server Tests
1. Run: ant -lib WEB-INF/lib junitretport
2. Open junit/all-tests.html in a web browser

## Web Client
1. Start either the Test or Production server
2. Point your web browser to http://localhost:8080

(Note: All 'ant' commands should be run where the build.xml file is located.)

