# Single page application for the sugar removal algorithm

Sugar units in natural products are pharmacokinetically important but often redundant and therefore obstructing the study of the main function of the aglycon. Therefore, it is recommended to remove the sugars before a theoretical or experimental study of a molecule. Deglycogenases, enzymes that specialized in sugar removal from small molecules, are often used in laboratories to perform this task. However, there is no standardized computational procedure to do it in silico.


This web application can be fired up as a simple docker container:

```
docker-compose build
docker-compose up -d
```

When running on localhost, will run on the port 8092.

Note that this is a Maven project, therefore all dependencies are managed by it. In case of library content modification, ensure that you have a stable internet connection so Maven can update the packages.
