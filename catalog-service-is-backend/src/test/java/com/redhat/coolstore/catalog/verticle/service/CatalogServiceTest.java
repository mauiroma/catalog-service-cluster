package com.redhat.coolstore.catalog.verticle.service;

import com.redhat.coolstore.catalog.model.Product;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(VertxUnitRunner.class)
public class CatalogServiceTest extends MongoTestBase {

    private Vertx vertx;

    @Before
    public void setup(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        JsonObject config = getConfig();
        mongoClient = MongoClient.createNonShared(vertx, config);
        Async async = context.async();
        dropCollection(mongoClient, "products", async, context);
        async.await(10000);
    }

    @After
    public void tearDown() throws Exception {
        mongoClient.close();
        vertx.close();
    }

    @Test
    public void testAddProduct(TestContext context) throws Exception {
        String itemId = "999999";
        String name = "productName";
        Product product = new Product();
        product.setItemId(itemId);
        product.setName(name);
        product.setDesc("productDescription");
        product.setPrice(100.0);

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.addProduct(product, ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                JsonObject query = new JsonObject().put("_id", itemId);
                mongoClient.findOne("products", query, null, ar1 -> {
                    if (ar1.failed()) {
                        context.fail(ar1.cause().getMessage());
                    } else {
                        assertThat(ar1.result().getString("name"), equalTo(name));
                        async.complete();
                    }
                });
            }
        });
    }

    @Test
    public void testGetProducts(TestContext context) throws Exception {
        // ----
        // To be implemented
        //
        // In your test:
        // -Insert two or more products in MongoDB. Use the `MongoClient.save` method to do so.
        // - Retrieve the products from Mongo using the `testGetProducts` method.
        // - Verify that no failures happened,
        //   that the number of products retrieved corresponds to the number inserted,
        //   and that the product values match what was inserted.
        //
        // ----
        Product p = getProduct("1","name1", "desc1",1.0);
        Product p1 = getProduct("2","name2", "desc2",2.0);

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);
        Async async = context.async(2);

        mongoClient.save("products",p.toJson(),ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                System.out.println("Inserted "+p.toJson());
                async.countDown();
            }
        });
        mongoClient.save("products",p1.toJson(),ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                System.out.println("Inserted "+p1.toJson());
                async.countDown();
            }
        });
        async.await();


        Async async2 = context.async();
        service.getProducts( ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                JsonObject query = new JsonObject().put("_id", "1");
                mongoClient.findOne("products", query, null, ar1 -> {
                    if (ar1.failed()) {
                        context.fail(ar1.cause().getMessage());
                    } else {
                        assertThat(ar1.result().getString("name"), equalTo("name1"));
                        System.out.println("Name  "+ ar1.result().getString("name"));
                    }
                });

                System.out.println("-------------------->"+ar.result().size());

                assertThat(ar.result().size(),equalTo(2));
                async2.complete();
            }
        });
    }

    @Test
    public void testGetProduct(TestContext context) throws Exception {
        // ----
        // To be implemented
        // 
        // ----
        Product p = getProduct("1","name1", "desc1",1.0);
        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);
        Async async = context.async();

        service.addProduct(p, ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            }else{
                async.complete();
            }
        });

        async.await();
        Async async1 = context.async();

        service.getProducts( ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                JsonObject query = new JsonObject().put("_id", "1");
                mongoClient.findOne("products", query, null, ar1 -> {
                    if (ar1.failed()) {
                        context.fail(ar1.cause().getMessage());
                    } else {
                        assertThat(ar1.result().getString("name"), equalTo("name1"));
                    }
                });
                assertThat(ar.result().size(),equalTo(1));
                async1.complete();
            }
        });
    }

    @Test
    public void testGetNonExistingProduct(TestContext context) throws Exception {
        // ----
        // To be implemented
        // 
        // ----
        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.getProducts( ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                JsonObject query = new JsonObject().put("_id", "1");
                mongoClient.findOne("products", query, null, ar1 -> {
                    if (ar1.failed()) {
                        context.fail(ar1.cause().getMessage());
                    } else {
                        assertThat(ar1.result(), equalTo(null));
                    }
                });
                assertThat(ar.result().size(),equalTo(0));
                async.complete();
            }
        });
    }

    @Test
    public void testPing(TestContext context) throws Exception {
        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);
        
        Async async = context.async();
        service.ping(ar -> {
            assertThat(ar.succeeded(), equalTo(true));
            async.complete();
        });
    }


    private Product getProduct(String itemId, String name, String description, double price){
        Product product = new Product();
        product.setItemId(itemId);
        product.setName(name);
        product.setDesc(description);
        product.setPrice(price);
        return product;
    }
}
