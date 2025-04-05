# WebDisk

WebDisk is a demo project for evaluation purposes. The name WebDisk is only for demo and is not related in any way with other existing projects having the same name.

## 1. Statement of Work

Implementation of a REST file storage service  

- Files are stored on disk, in the file system. 
- File names can be 1-64 characters long and restricted to character set: a-zA-Z0-9_- 
- Solution must support many files: at least 10.000.000 files, assume a huge disk at your disposal 
- REST API must support the following operations: 
    + File access: create, read, update, delete --> file is always identified by name 
    + File enumeration: return file names matching a regexp 
    + Size: return number of files in the storage 
        * Optimize file access operations, file enum and size can be slow. 
- Meaningful test coverage 

Constraints: 

- Use Maven / Java 17 
- 3rd party libraries are ok for utils, DON'T use an existing database / storage library. 

Evaluation criteria (in ascending order): 
1. Correctness - does the solution work? 
2. Code clarity / structure 
3. Performance

## 2. Implementation

### 2.1. Setup and run the project

The application can be run in either of these flavors:

- from downloaded .jar file (requires JRE 17)
- built from source code (requires JDK 17, Maven)
- in Docker (requires Docker, JDK 17, Maven)

It needs a path specified, either by command param or by changing the default value in application.properties. By default, the storage folder is *./sample*. A web interface is available on port 8080: https://localhost:8080.

#### 2.1.1. From downloaded .jar

To run the packaged application, you need JRE 17 installed and a folder to use as files repository.

    java -jar webdisk.jar

Optionally, you can specify a different cache directory

    java -jar webdisk.jar --webdisk.path=cache_directory

#### 2.1.2. From code

Source code for the application is available at https://github.com/github-stefan-rogin/webdisk.git or by .zip. 
The required packages for building the application from source code are JDK 17 and Maven 3.6.

Build, run and test the project, using your favorite IDE.

#### 2.1.3. In Docker

As public images are not available, Docker images need to be built from source code before creating containers. 

Build package, then Docker image, then start a container:

     mvn clean package
     docker build -t webdisk .
     docker run -p 8080:8080 webdisk

### 2.2. Service description

The service supports the following requests:

 	- GET /files/{fileName} - Retrieves a file by its name.
 	- HEAD /files/{fileName} - Check if a file exists, whithout getting its content.
 	- POST /files/ - Uploads a new file.
 	- PUT /files/{fileName} - Updates an existing file.
 	- DELETE /files/{fileName} - Deletes a file by its name.
 	- GET /files/search - Searches for files matching a given pattern, case sensitive.
 	- GET /files/size - Returns the total number of files stored by the application.
 	- GET /files/restricted - Demo endpoint for security implementation.

#### 2.2.1. Overview

The service is built with SpringBoot framework.

#### 2.2.2. Performance

The app's performance concerns are addressed mainly by a strategy of handling in memory as much of the service's operations as possible. For this purpose, a simplistic cache solution is implemented in FilesCache class, which holds a registry of all stored files in memory. More robust and scalable solutions - Memcached, Redis - were considered out of scope and against the requirements.

The main concern is memory footprint, which approximates at 6.5GB for 10^8 files (ten times the minimum requirements). This is in the realm of possibility, albeit restricting the amount of information available for each file to their names. Content type, last modified etc. would further increase the memory footprint and require alternative solutions already mentioned or similar.

A potential slow operation is performed at the start of the application, when the entire list of files is loaded to memory - cache initialization. A second performance concern is the Regexp pattern search for files. Both scenarios were tested on a large data set, on common hardware. Approximately 1,000,000 files were generated with a shell script generate.sh. Performance results were satisfactory - 1.4s for cache initialization, 120ms average for file name pattern search.

#### 2.2.3. Logging and monitoring

Logging is provided by Logback. The default log file is webdisk.log and is automatically rotated daily at 00:00 local. Default log level is INFO for both the web server and the app - configurable independently.

- Telemetry: Operations that are concerning app performance - cache initialization, file search - are measured and logged.
- Errors: When encountering errors, the originating web request is logged together with the error, where applicable, for easing investigations.
- Requests: A basic trace of all web requests are left for monitoring and BI.

Logging texts use tags - e.g. @Cause, @Request - to facilitate log aggregation, monitoring and reporting.

#### 2.2.4. Security

For ease of evaluating the demo, security is only implemented in one endpoint additional to SoW. It demonstrates a pre-authentication scenario. Each request is filtered using the authentication token, carried in a standard Authorization header with a Bearer token. An external authentication system to validate tokens was considered out of scope, therefore any token present is considered valid.

#### 2.2.5. Data consistency

Maintaining data consistency between cache and storage was considered out of scope. The only synchronization opportunity is at cache initialization. The only error correction measure is taken in GET /files/{filename}, where a cache presence followed by a FileNotFound error from the storage service, will result in the removal of the queried key from the cache.

#### 2.2.6. Throttling and queueing

Throttling and queueing requests were considered out of scope, therefore they were not addressed.

### 2.3. Project description

#### 2.3.1. Testing

- The project contains both unit and integration tests. Mocking is done with Mockito.
- For SonarQube reporting, the build generates JaCoCo reports.
- Performance evaluation tests were done with a large generated data set, as described earlier.

### 2.4. Documentation

A standard Swagger API UI is available at http://localhost:8080/swagger-ui/index.html.
For the project itself, Javadoc files are available at http://localhost:8080/apidocs/index.html, once the application is started.

#### 2.4.1. Example commands

Once started, commands can be run by any means of invoking a web service: curl, Postman, IDE plugins etc. .

    curl -X GET http://localhost:8080/files/size -H "accept: application/json"

    Output: 
    {"size":7}
    ...

    curl -O -X GET http://localhost:8080/files/one
    ...

    curl -X POST -H "Content-Type: multipart/form-data" -F "file=@./oneup" http://localhost:8080/files/upload
    ...
    
    Output:
    {"fileName":"C73SM6cuo_GxPRkOqzUIfMGvX-v5_FZ9bQWP_Vn3J"}
    ...

    curl -X PUT -H "Content-Type: multipart/form-data" -F "file=@./oneup" http://localhost:8080/files/oneup
    ...

    curl -X DELETE http://localhost:8080/files/two
    ...

    curl -X GET http://localhost:8080/files/search?pattern=one
    
    Output:
    {"results":["one","andone"]}
    ...

    curl -X GET http://localhost:8080/files/restricted -H "Authorization: Bearer any_token"
    
    Output:
    Authorized
    ...

