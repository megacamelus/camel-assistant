# CLI

## Consuming plain text data

```
java -jar target/quarkus-app/quarkus-run.jar consume data --dynamic --source=cli.v2.org --id=3 "Manually loaded dynamic data"
```

or

```
java -jar target/quarkus-app/quarkus-run.jar consume "Static data"
```

## Consuming PDF data

```
java -jar target/quarkus-app/quarkus-run.jar consume file /path/to/pdf-file.pdf
```