package com.redhat.coolstore.catalog.verticle.service;

import com.redhat.coolstore.catalog.model.Product;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

@ProxyGen
public interface CatalogService {

    final static String ADDRESS = "catalog-service"; 


    static CatalogService createProxy(Vertx vertx){
        return new CatalogServiceVertxEBProxy(vertx,"coolstore-catalog-service");
    }
    //----
    //
    // Add a static method that returns an instance of the client side proxy class for this service
    // Initialize the proxy with the vertx instance and the event bus address (ADDRESS)
    // Method signature:
    // static CatalogService createProxy(Vertx vertx)
    //----

    void getProducts(Handler<AsyncResult<List<Product>>> resulthandler);

    void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler);

    void addProduct(Product product, Handler<AsyncResult<String>> resulthandler);

    void ping(Handler<AsyncResult<String>> resultHandler);

}
