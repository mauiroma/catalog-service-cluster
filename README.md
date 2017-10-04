# README

Vert.x Lab for GPTE Modern App Dev - Microservices development with RHOAR course
The project is a fork of catalog-service project where the verticle is in cluster and composed by rest api and a backend.

The clustering model works with hazelcast or infinispan

## Infinispan
use the modules
- catalog-service-is-rest
- catalog-service-is-backend

## Hazelcast
use the modules
- catalog-service-hzc-rest
- catalog-service-hzc-backend


## Run on OCP
```
export CATALOG_PRJ=mr-catalog-service-cluster
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
cd catalog-service-<CLUSTERING_MODEL>-rest
mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ -Dmaven.test.skip=true
```
```
cd catalog-service-<CLUSTERING_MODEL>-backend
mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ -Dmaven.test.skip=true
```


### Testing on OCP
Get exposed route and then use to call the service
```
oc get route -n $CATALOG_PRJ
export ROUTE_INFINISPAN=`oc get route catalog-service-is-rest -o template --template='{{.spec.host}}'`
export ROUTE_HAZELCAST=`oc get route catalog-service-hzc-rest -o template --template='{{.spec.host}}'`
```
```
curl -X GET "$ROUTE_INFINISPAN/products"
curl -X GET "$ROUTE_INFINISPAN/product/444435"
```
```
curl -X GET "$ROUTE_HAZELCAST/products"
curl -X GET "$ROUTE_HAZELCAST/product/444435"
```


### Testing on OCP Monitoring fraction
```
curl -X GET "$ROUTE_INFINISPAN/health/readiness"
curl -X GET "$ROUTE_INFINISPAN/health/liveness"
```
```
curl -X GET "$ROUTE_HAZELCAST/health/readiness"
curl -X GET "$ROUTE_HAZELCAST/health/liveness"
```
