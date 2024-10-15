This is the dataserver which is designed using a KTOR structure to manage the serving of data to the primary app front-end along with the backend database management for the news data that is retrieved from NewsAPI.org.

The folder structure is as follows:
```
src/main/kotlin/com/dataserver
├---/plugins (contains the details of managing database CRUD, schema, serialization, user access)
├---/resources (contains the configuration YAML for the application)
└---Application.kt (primary main class)
````

Added Google Cloud Build for continuous deployment