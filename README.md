## About

Course project for *XML and web services* course

Author: Ivan Mihajlov SW19/2013, Software engineering and IT, Faculty of Technical Sciences, Novi Sad, Serbia

Demonstration video: https://youtu.be/fpGVptp4XT0

## Technologies used

* [Java](https://www.java.com/) - a general-purpose object-oriented, class-based programming language
* [Spring Boot](https://spring.io/projects/spring-boot) - an open source Java-based framework built on the top of the Spring Framework
* [eXist-db](http://exist-db.org/exist/apps/homepage/index.html) - an open source software project for NoSQL databases built on XML technology
* [Apache Jena Fuseki](https://jena.apache.org/documentation/fuseki2/) - a SPARQL server used for metadata manipulation & extraction
* [Angular](https://angular.io/) - TypeScript-based open-source web application framework

## Installation

### eXist-db

* Download [eXist-db 5.2.0](https://bintray.com/existdb/releases/download_file?file_path=exist-distribution-5.2.0-win.zip) and unpack it
  * The path to the unpacked folder must not contain any spaces!
* Open the unpacked folder and execute `bin\startup.bat`
* Verify the program is running on port 8080 by opening [eXide](http://localhost:8080/exist/apps/eXide/)

### Apache Jena Fuseki

* Download [Apache Jena Fuseki 3.16.0](https://downloads.apache.org/jena/binaries/apache-jena-fuseki-3.16.0.zip) and unpack it
* Open the unpacked folder and execute `fuseki-server.bat`
* Verify the program is running on port 3030 by opening [Apache Jena Fuseki](http://localhost:3030/)

### Java application

* Go to *papers_please\papers_please\src\main\resources*
* Open all three __*.properties*__ files and if necessary edit their content (e.g. email credentials in `application.properties`)
* Run `papers_please\papers_please\src\main\java\com\ftn\papers_please\PapersPleaseApplication.java` as a Spring Boot App using JDK8
  * Spring Tool Suite (STS) 3 IDE was used for developing purposes

### Frontend

* Download [Node.js LTS](https://nodejs.org/en/download/) and install it
* Open the command prompt in *papers_please\frontend* folder and execute `npm install`
* Once the installation finishes, execute `npm install -g @angular/cli`
* Start the app by executing `ng serve --open`
* Verify the app is running by opening [localhost:4200](http://localhost:4200/)

## Setup

* On Apache Jena Fuseki [manage datasets page](http://localhost:3030/manage.html) create a new **Papers** dataset with the type **Persistent (TDB2)**
* After starting the application, register a new user.
* To give user the editor role:
  * Open [eXide](http://localhost:8080/exist/apps/eXide/) and select *directory* tab in the left panel
  * Go to *db/sample/users* collection and find the desired user, e.g. *user0*
  * Add `<role role="ROLE_EDITOR"/>` to `<roles>` element and click *Save* above
* Examples of valid XML documents can be found in *papers_please\papers_please\src\main\resources\examples*
