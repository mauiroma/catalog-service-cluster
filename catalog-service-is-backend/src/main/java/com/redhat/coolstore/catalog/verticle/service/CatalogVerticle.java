package com.redhat.coolstore.catalog.verticle.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Optional;

public class CatalogVerticle extends AbstractVerticle {
    
    private MongoClient client;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        
        client = MongoClient.createShared(vertx, config());
        
        //----
        // * Create an instance of `CatalogService`.
        // * Register the service on the event bus
        // * Complete the future
        //----
        CatalogService catalogService = new CatalogServiceImpl(vertx, config(), client);
        ProxyHelper.registerService(CatalogService.class, vertx, catalogService,
                "coolstore-catalog-service");

        startFuture.complete();
    }

    @Override
    public void stop() throws Exception {
        Optional.ofNullable(client).ifPresent(c -> c.close());
    }

}
