package com.redhat.coolstore.catalog.verticle;

import com.redhat.coolstore.catalog.verticle.service.CatalogService;
import com.redhat.coolstore.catalog.verticle.service.CatalogVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        //----
        // To be implemented
        //
        // * Create a `ConfigStoreOptions` instance.
        // * Set the type to "configmap" and the format to "yaml".
        // * Configure the `ConfigStoreOptions` instance with the name and the key of the configmap
        // * Create a `ConfigRetrieverOptions` instance
        // * Add the `ConfigStoreOptions` instance as store to the `ConfigRetrieverOptions` instance
        // * Create a `ConfigRetriever` instance with the `ConfigRetrieverOptions` instance
        // * Use the `ConfigRetriever` instance to retrieve the configuration
        // * If the retrieval was successful, call the `deployVerticles` method, otherwise fail the `startFuture` object.
        //
        //----

        ConfigStoreOptions jsonConfigStore = new ConfigStoreOptions().setType("json");
        ConfigStoreOptions appStore = new ConfigStoreOptions()
                .setType("configmap")
                .setFormat("yaml")
                .setConfig(new JsonObject()
                        .put("name", "app-config")
                        .put("key", "app-config.yaml"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions();
        if (System.getenv("KUBERNETES_NAMESPACE") != null) {
            //we're running in Kubernetes
            options.addStore(appStore);
        } else {
            //default to json based config
            jsonConfigStore.setConfig(config());
            options.addStore(jsonConfigStore);
        }

        ConfigRetriever.create(vertx, options)
                .getConfig(ar -> {
                    if (ar.succeeded()) {
                        deployVerticles(ar.result(), startFuture);
                    } else {
                        System.out.println("Failed to retrieve the configuration.");
                        startFuture.fail(ar.cause());
                    }
                });



        //Health Checks
        Router router = Router.router(vertx);
        router.get("/health/readiness").handler(rc -> rc.response().end("OK"));
        router.get("/health/liveness").handler(rc -> rc.response().end("OK"));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("catalog.http.port", 8080), result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void deployVerticles(JsonObject config, Future<Void> startFuture) {
        //----
        // To be implemented
        //
        // * Create a proxy for the `CatalogService`.
        // * Create an instance of `ApiVerticle` and `CatalogVerticle`
        // * Deploy the verticles
        // * Make sure to pass the verticle configuration object as part of the deployment options
        // * Use `Future` objects to get notified of successful deployment (or failure) of the verticle deployments.
        // * Use a `CompositeFuture` to coordinate the deployment of both verticles.
        // * Complete or fail the `startFuture` depending on the result of the CompositeFuture
        //
        //----
        Future<String> apiVerticleFuture = Future.future();
        Future<String> catalogVerticleFuture = Future.future();

        CatalogService catalogService = CatalogService.createProxy(vertx);
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config);
        vertx.deployVerticle(new CatalogVerticle(), options, catalogVerticleFuture.completer());

        CompositeFuture.all(apiVerticleFuture, catalogVerticleFuture).setHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println("Verticles deployed successfully.");
                startFuture.complete();
            } else {
                System.out.println("WARNINIG: Verticles NOT deployed successfully.");
                startFuture.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

}
