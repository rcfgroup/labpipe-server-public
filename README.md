
![LabPipe Login](labpipe-logo-light.png)

![](../../workflows/Gradle%20CI/badge.svg)

Server to work with LabPipe Client to assist with data collection.

- Configurable: build your own study scaffold
- Light-weight: single jar file to run
- RESTful: streamline with your current solutions with APIs

## Documentation
The documentation is available [here](http://docs.labpipe.org).

## Quick start

### Prerequisite
* JRE 8 or above
* MongoDB 4.0.3 or above

Download latest release [here](../../releases). Once you have downloaded the jar file, you can run it with:

```
java -jar labpipe-server-all.jar --help
```

The `help` argument provides you a list of available arguments. Details about command line arguments can be found [here](https://docs.labpipe.org/server/command-line).

For the server to function properly, you will at least set the following parameters:

| Parameter | Required | Default | Description |
| --- | --- | --- | --- |
| `server.port` | No | 4567 | The port LabPipe server runs on |
| `database.host` | No | localhost | MongoDB server address |
| `database.port` | No | 27017 | MongoDB server port |
| `database.name` | Yes |  | MongoDB database name |
| `database.user` | No |  | MongoDB server user |
| `database.pass` | No |  | MongoDB server password |
| `path.cache` | No | $HOME_DIR$/labpipe | Server cache directory path |

You can set the parameter value one at each time like:

```
java -jar labpipe-server-all.jar config db --host=localhost
```

Or you can set them all together like:

```
java -jar labpipe-server-all.jar config db --host=yourhost --port=12345
```

If you want to enable email notifications on the server, you will need to set mail server parameters, details can be found [here](https://docs.labpipe.org/server/configuration-file).

You will need to populate your database with required collections so that your server can provide results through its APIs, details can be found [here](https://docs.labpipe.org/server/database).


## License
This project is open source under GNU General Public License v3.0 (GPL-3.0).
