package com.empyrean.camel.routes;

import java.time.Instant;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.StartupListener;

import io.nats.client.KeyValue;
import io.nats.client.KeyValueManagement;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueWatcher;

public class NatsRouteBuilder extends RouteBuilder {

    private static final String BUCKET = "POSITIONS";

    @Override
    public void configure() throws Exception {
        from("seda:nats-kv-positions?concurrentConsumers=1")
                .routeId("nats-kv-consumer")
                .process(exchange -> {
                    KeyValueEntry entry = exchange.getMessage().getBody(KeyValueEntry.class);
                    System.out.println("Received at: " + Instant.now()
                            + " | Key: " + entry.getKey()
                            + " | Value: " + new String(entry.getValue()));
                });

        // Register a StartupListener so the KV watcher starts after Camel is fully up.
        getCamelContext().addStartupListener(new StartupListener() {
            @Override
            public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
                startKvWatcher(context);
            }
        });
    }

    private void startKvWatcher(CamelContext context) throws Exception {
        io.nats.client.Connection nc = Nats.connect(
                new Options.Builder()
                        .server("nats://localhost:4222")
                        .build());

        KeyValueManagement kvm = nc.keyValueManagement();

        // Ensure the bucket exists.
        boolean exists = kvm.getBucketNames().contains(BUCKET);
        if (!exists) {
            kvm.create(KeyValueConfiguration.builder().name(BUCKET).build());
        }

        KeyValue kv = nc.keyValue(BUCKET);
        ProducerTemplate producer = context.createProducerTemplate();

        kv.watchAll(new KeyValueWatcher() {
            @Override
            public void watch(KeyValueEntry entry) {
                producer.sendBody("seda:nats-kv-positions", entry);
            }

            @Override
            public void endOfData() {
                // initial snapshot delivered; live updates follow
            }
        });
    }
}
