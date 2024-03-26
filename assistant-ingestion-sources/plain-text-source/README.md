Types of Data:

* Static: data that doesn't change often. Facts.
* Dynamic: data that can change quickly (i.e.: transient information that can change over time)

For example: 

* Static data: Apache Camel 4.4.0 was released on February 2024. 
* Dynamic data: The latest Apache Camel version is 4.4.0 and was released on February 2024. 

On the examples above, note how the dynamic data can change over time (i.e.: when the community releases a new major version, that will be the latest version)

Insert static data: 

```shell
curl -X POST http://localhost:8083/api/consume/static -H "Content-Type: text/plain" -d 'Apache Camel is 4.4.0 was released on February 2024'
```

* The path format is: `api/consume/{externalSourceName}/{externalSourceId}`

Insert dynamic data: 

```shell
curl -X POST http://localhost:8083/api/consume/dynamic/cli.org/1 -H "Content-Type: text/plain" -d 'The latest version of Apache Camel is 4.5.1 and it was released on March 2024'
```

* The path format is: `api/consume/{externalSourceName}/{externalSourceId}`