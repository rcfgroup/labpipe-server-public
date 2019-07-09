## LabPipe Server

[![Build Status](https://travis-ci.com/colin-bz/labpipe-server-public.svg?branch=master)](https://travis-ci.com/colin-bz/labpipe-server-public)

Server to work with LabPipe Client to assist with data collection.

- Configurable: build your own study scaffold
- Light-weight: single jar file to run
- RESTful: streamline with your current solutions with APIs

**More documentations are being added**

## Build from source code
### Prerequisite
To build from source code, you will need to install the following components:

- JDK 8 and above (Tested with JDK 11)
- MongoDB (Tested with 4.0.3)
- Gradle 5 and above (Tested with 5.4.1 and 5.5)

### Build and run
#### Build artifacts and run
```
gradle clean build
java -jar build/libs/labpipe-server-kotlin-1.0.0-all.jar
```
#### Run from source code
```
gradle clean run
```
If you are using ```gradle run```, you can pass commandline arguments with ```--args```, e.g. ```--args="run"```.

## Commandline arguments
```
--help  list available commandline options
```

#### Run server
```
run                 start server
run --debug         start server in debug mode
```

#### Init server
```
init                create example study
```

#### Server configurations
```
config server --help    list available server config options
config server --port    server port
config server --cache   server cache file directory path
```
#### Database configurations
```
config db --help    list available database config options
config db --host    database host
config db --port    database port
config db --name    database name
config db --user    database user
config db --pass    database password
```
#### Email configurations
```
config email --help             list available email config options
config email --host             email server host
config email --port             email server port
config email --user             email server user
config email --pass             email server password
config email --notifier-name    notification sender name
config email --notifier-addr    notification sender email address
```

## Roadmap

## License
This project is open  under Non-Profit Open Software License 3.0 (NPOSL-3.0).