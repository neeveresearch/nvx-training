package com.neeve.ccauth.driver;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Random;

import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Argument;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.StatsFactory;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.IStats.Latencies;
import com.neeve.util.UtlGovernor;

import com.neeve.ccauth.roe.messages.*;

public class Application {
    @Configured(property = "ccauth.driver.autoStart")
    private boolean autoStart;
    @Configured(property = "ccauth.driver.numCustomers")
    private int numCustomers;
    @Configured(property = "ccauth.driver.numMerchants")
    private int numMerchants;
    @Configured(property = "ccauth.driver.numMerchantStores")
    private int numMerchantStores;
    @Configured(property = "ccauth.driver.sendCount")
    private int sendCount;
    @Configured(property = "ccauth.driver.sendRate")
    private int sendRate;
    @AppStat
    private final Counter sentCount = StatsFactory.createCounterStat("Auth Driver Send Count");
    @AppStat
    private final Counter receivedCount = StatsFactory.createCounterStat("Auth Driver Receive Count");
    @AppStat(name = "Authorization Latency")
    private volatile Latencies authLatencies = StatsFactory.createLatencyStat("Authorization Latency");
    final private AtomicReference<Thread> sendingThread = new AtomicReference<Thread>();
    final private static Random random = new Random(System.currentTimeMillis());
    private AepMessageSender messageSender;

    final private static String randomString(final int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.valueOf(random.nextInt(10)));
        }
        return sb.toString();
    }

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Command(name = "send",displayName = "Send Auth Requests",description = "Instructs the driver to send authorization requests")
    public final void doSend(@Argument(name = "numCustomers",position = 1,required = true,description = "The number of customers to randomly select from for the auth requests")
                             final int numCustomers,
                             @Argument(name = "numMerchants",position = 2,required = true,description = "The number of merchants to randomly select from for the auth requests")
                             final int numMerchants,
                             @Argument(name = "numMerchantStores",position = 3,required = true,description = "The number of merchant stores (per merchant) to randomly select from for the auth requests")
                             final int numMerchantStores,
                             @Argument(name = "count",position = 4,required = true,description = "The number of requests to send")
                             final int count,
                             @Argument(name = "rate",position = 5,required = true,description = "The rate at which to send the requests")
                             final int rate) throws Exception {
        final Thread thread = new Thread(new Runnable() {
            private final UtlGovernor sendGoverner = new UtlGovernor(rate);
            private int sent = 0;

            @Override
            public void run() {
                while (sent++ < count && sendingThread.get() == Thread.currentThread()) {
                    AuthorizationRequestMessage message = AuthorizationRequestMessage.create();
                    long now = System.nanoTime();
                    int customerId = random.nextInt(numCustomers);
                    message.setTxnId(now);
                    message.setAccountNumber(String.format("%016d", customerId));
                    message.setTxnId(System.nanoTime());
                    message.setTxnTimestamp(System.nanoTime());
                    message.setTxnAmount(random.nextDouble());
                    message.setMerchantNumber(String.format("%d", random.nextInt(numMerchants)));
                    message.setTRSStringVariable1(randomString(20));
                    message.setTRSStringVariable2(randomString(20));
                    message.setTRSStringVariable3(randomString(20));
                    message.setTRSIntVariable1(random.nextInt());
                    message.setTRSLongVariable1(random.nextLong());
                    message.setTRSLongVariable2(random.nextLong());
                    message.setTRSDoubleVariable1(random.nextDouble());
                    message.setTRSDoubleVariable2(random.nextDouble());
                    message.setTRSDoubleVariable3(random.nextDouble());
                    message.setTRSDoubleVariable4(random.nextDouble());
                    message.setTRSDoubleVariable5(random.nextDouble());
                    messageSender.sendMessage("authrequests", message);
                    sentCount.increment();
                    sendGoverner.blockToNext();
                }
            }
        }, "CCAuth Driver");

        Thread oldThread = sendingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
            oldThread.join();
        }
        thread.start();
    }

    @Command(name = "stopSending",displayName = "Stop Sending",description = "Stops sending of messages.")
    final public void stop() throws Exception {
        Thread oldThread = sendingThread.getAndSet(null);
        if (oldThread != null) {
            oldThread.join();
        }
    }

    @Command
    public long getSentCount() {
        return sentCount.getCount();
    }

    @Command(name = "numReceived",displayName = "Get Number Received",description = "Returns the count of messages received")
    public long getNumReceived() {
        return receivedCount.getCount();
    }

    @AppMain
    public void run(String[] args) throws Exception {
        if (autoStart) {
            doSend(numCustomers, numMerchants, numMerchantStores, sendCount, sendRate);
        }
    }

    @EventHandler
    public final void onAuthorizationResponse(final AuthorizationResponseMessage message) {
        receivedCount.increment();
        authLatencies.add((System.nanoTime() - message.getTxnId())/1000l);
    }
}

