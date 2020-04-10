# Single page application for the sugar removal algorithm

Works as a simple docker container, to fire it up, build it first:

```
docker-compose build
docker-compose up -d
```

When running on localhost, will run on the port 8092.

Note that this is a Maven project, therefore all dependencies are managed by it. In case of library content modification, ensure that you have a stable internet connection so Maven can update the packages.
