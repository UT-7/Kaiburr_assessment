Task 1. Java REST API example

1) We have used Spark Framework which is open source Java Web Framework
2) The project is created in Eclipse as Maven project
3) Java source file server.java is added under src/main/java
4) pom.xml contains dependencies for Spark framework and Mongo DB driver
5) Mongo DB named "mydb" and collection named "servers" should exist in Mongodb server
6) The project is run as Java Application and listening on port 8080
7) Three APIs are implemented:
	a) get - To get all servers, get server by id, find servers by name
	b) put - To create server object from json data ( id, name, language, framework)
	c) delete - Delete server by id
8) Postman collection is also available to test above APIs