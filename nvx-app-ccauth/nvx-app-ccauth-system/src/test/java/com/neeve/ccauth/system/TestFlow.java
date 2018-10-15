package com.neeve.ccauth.system;

import java.util.Properties;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * A test case that tests the authorization flow. 
 */
public class TestFlow extends AbstractTest {
    @Test
    public void testFlow() throws Throwable {
        // configure
        Properties env = new Properties();
        env.put("nv.ddl.profiles", "test");
        env.put("x.apps.merchantservice-1.storage.clustering.enabled", "false");
        env.put("x.apps.cardservice-1.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-1.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-2.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-3.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-4.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-5.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-6.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-7.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-8.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-9.storage.clustering.enabled", "false");
        env.put("x.apps.customerservice-10.storage.clustering.enabled", "false");
        env.put("ccauth.driver.autoStart", "true");
        env.put("ccauth.driver.sendRate", "1000");
        env.put("ccauth.driver.sendCount", "1000");

        // start apps
        startApp(com.neeve.ccauth.merchantservice.Application.class, "merchantservice-1", "merchantservice-1a", env);
        startApp(com.neeve.ccauth.cardservice.Application.class, "cardservice-1", "cardservice-1a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-1", "customerservice-1a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-2", "customerservice-2a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-3", "customerservice-3a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-4", "customerservice-4a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-5", "customerservice-5a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-6", "customerservice-6a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-7", "customerservice-7a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-8", "customerservice-8a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-9", "customerservice-9a", env);
        startApp(com.neeve.ccauth.customerservice.Application.class, "customerservice-10", "customerservice-10a", env);

        // seed
        com.neeve.ccauth.seeder.Application seeder = startApp(com.neeve.ccauth.seeder.Application.class, "seeder", "seeder", env);

        // wait
        long timeout = System.currentTimeMillis() + 60000;
        while (!seeder.seedDone() && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        // sleep to let the seeding requests get fully processed
        Thread.sleep(1000);

        // drive
        com.neeve.ccauth.driver.Application driver = startApp(com.neeve.ccauth.driver.Application.class, "driver", "driver", env);

        // wait
        timeout = System.currentTimeMillis() + 60000;
        while (driver.getNumReceived() < 1000 && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        // sleep to catch any extra messages
        Thread.sleep(1000);

        // validate
        assertEquals("Driver did not send expected requests", 1000, driver.getSentCount());
        assertEquals("Driver did not receive expected responses", 1000, driver.getNumReceived());
    }
}
