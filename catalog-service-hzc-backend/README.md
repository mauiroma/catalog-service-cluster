# README

Vert.x Lab for GPTE Modern App Dev - Microservices development with RHOAR course

## Run on OCP
```
export CATALOG_PRJ=mr-catalog-vertx 
```

### Create project on OCP
```
oc new-project $CATALOG_PRJ
```

### Create mongodb database on OCP
```
oc process -f ocp/coolstore-catalog-mongodb-persistent.yaml -p CATALOG_DB_USERNAME=mongo -p CATALOG_DB_PASSWORD=mongo -n $CATALOG_PRJ | oc create -f - -n $CATALOG_PRJ
```

### Deploy on OCP

```
oc policy add-role-to-user view -z default -n $CATALOG_PRJ
oc create configmap app-config --from-file=etc/app-config.yaml -n $CATALOG_PRJ
oc get configmap app-config -o yaml -n $CATALOG_PRJ
```
```
mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ -Dmaven.test.skip=true
```

### Testing on OCP
Get exposed route and then use to call the service
```
oc get route -n $CATALOG_PRJ
export ROUTE=`oc get route catalog-service -o template --template='{{.spec.host}}'`
```
```
curl -X GET "$ROUTE/products"
curl -X GET "$ROUTE/product/444435"
```

### Testing on OCP Monitoring fraction
```
curl -X GET "$ROUTE/health/readiness"
curl -X GET "$ROUTE/health/liveness"
```