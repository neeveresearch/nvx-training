package com.neeve.ccauth.seeder;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.neeve.aep.AepMessageSender;
import com.neeve.cli.annotations.Argument;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.trace.Tracer;
import com.neeve.util.UtlGovernor;

import com.neeve.ccauth.merchantservice.messages.*;
import com.neeve.ccauth.cardservice.messages.*;
import com.neeve.ccauth.roe.messages.*;

public class Application {
    @Configured(property = "ccauth.seeder.autoStart")
    private boolean autoStart;
    @Configured(property = "ccauth.seeder.numCustomers")
    private int numCustomers;
    @Configured(property = "ccauth.seeder.numTransactionsPerCustomer")
    private int numTransactionsPerCustomer;
    @Configured(property = "ccauth.seeder.customerIdStart")
    private int customerIdStart;
    @Configured(property = "ccauth.seeder.numMerchants")
    private int numMerchants;
    @Configured(property = "ccauth.seeder.numMerchantStores")
    private int numMerchantStores;
    @Configured(property = "ccauth.seeder.seedRate")
    private int seedRate;
    @Configured(property = "ccauth.customerservice.numpartitions")
    private int numPartitions;
    final private AtomicReference<Thread> seedingThread = new AtomicReference<Thread>();
    final private Random random = new Random(System.currentTimeMillis());
    private AepMessageSender messageSender;
    final private Tracer tracer;
    private boolean seedDone;

    public Application() {
        this.tracer = Tracer.create(Tracer.Level.INFO);
        this.tracer.bind("ccauth.seeder");
    }

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        this.messageSender = messageSender;

    }

    final private String randomString(final int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.valueOf(random.nextInt(10)));
        }
        return sb.toString();
    }

    final private AddMerchantMessage createAddMerchantMessage(final int seeded, final int numMerchantStores) {
        final AddMerchantMessage message = AddMerchantMessage.create();
        message.setMerchantNumber(String.format("%d", seeded));
        for (int i = 0; i < numMerchantStores; i++) {
            final MerchantStoreDTO merchantStoreDTO = MerchantStoreDTO.create();
            merchantStoreDTO.setMerchantStoreId(i);
            merchantStoreDTO.setMerchantStoreCountryCode(randomString(3));
            merchantStoreDTO.setMerchantStorePostCode(randomString(5));
            message.addMerchantStoreList(merchantStoreDTO);
        }
        return message;
    }

    final private AddTransactionMessage createAddTransactionMessage(final int seeded, final int numTransactionsPerCustomer, final int customerIdStart, final int numMerchants, final int numMerchantStores) {
        final AddTransactionMessage message = AddTransactionMessage.create();
        final int customerNumber = customerIdStart + (seeded / numTransactionsPerCustomer);
        final String customerId = String.format("%d", customerNumber);
        message.setPartition((short)((customerNumber % numPartitions) + 1));
        final TransactionDTO transactionDTO = TransactionDTO.create();
        transactionDTO.setCustomerId(customerId);
        transactionDTO.setAccountNumber(String.format("%016d", customerNumber));
        transactionDTO.setTxnId(System.nanoTime());
        transactionDTO.setTxnTimestamp(System.nanoTime());
        transactionDTO.setTxnAmount(random.nextDouble());
        transactionDTO.setMerchantNumber(String.format("%d", random.nextInt(numMerchants)));
        transactionDTO.setTrasactionHistoryKey(random.nextLong());
        transactionDTO.setCMHistoryStringVariable1(randomString(20));
        transactionDTO.setCMHistoryStringVariable2(randomString(20));
        transactionDTO.setCMHistoryStringVariable3(randomString(20));
        transactionDTO.setProfileStringVariable1(randomString(20));
        transactionDTO.setProfileStringVariable2(randomString(20));
        transactionDTO.setProfileStringVariable3(randomString(20));
        transactionDTO.setProfileStringVariable4(randomString(20));
        transactionDTO.setProfileStringVariable5(randomString(20));
        transactionDTO.setProfileStringVariable6(randomString(20));
        transactionDTO.setProfileLongVariable1(random.nextLong());
        transactionDTO.setProfileLongVariable2(random.nextLong());
        transactionDTO.setSEProfStringVariable2(randomString(20));
        transactionDTO.setSEProfStringVariable3(randomString(20));
        transactionDTO.setSEProfStringVariable4(randomString(20));
        transactionDTO.setSEProfStringVariable5(randomString(20));
        transactionDTO.setSEProfStringVariable6(randomString(20));
        transactionDTO.setSEProfStringVariable7(randomString(20));
        transactionDTO.setSEProfLongVariable1(random.nextLong());
        transactionDTO.setSEProfLongVariable2(random.nextLong());
        transactionDTO.setSEProfDoubleVariable(random.nextDouble());
        transactionDTO.setSEProfDoubleVariable1(random.nextDouble());
        transactionDTO.setTRSStringVariable1(randomString(20));
        transactionDTO.setTRSStringVariable2(randomString(20));
        transactionDTO.setTRSStringVariable3(randomString(20));
        transactionDTO.setTRSIntVariable1(random.nextInt());
        transactionDTO.setTRSLongVariable1(random.nextLong());
        transactionDTO.setTRSLongVariable2(random.nextLong());
        transactionDTO.setTRSDoubleVariable1(random.nextDouble());
        transactionDTO.setTRSDoubleVariable2(random.nextDouble());
        transactionDTO.setTRSDoubleVariable3(random.nextDouble());
        transactionDTO.setTRSDoubleVariable4(random.nextDouble());
        transactionDTO.setTRSDoubleVariable5(random.nextDouble());
        message.setTransaction(transactionDTO);
        return message;
    }

    final private AddCardMessage createAddCardMessage(final int seeded, final int customerIdStart) {
        final AddCardMessage message = AddCardMessage.create();
        message.setAccountNumber(String.format("%016d", seeded));
        message.setCustomerId(String.format("%d", customerIdStart + seeded));
        return message;
    }

    @Command(name = "seed", displayName = "Seed Data", description = "Instructs the seeder to seed data into the CCAUTH system")
    public final void doSeed(@Argument(name = "numCustomers", position = 1, required = true, description = "The number of customers to seed") final int numCustomers,
                             @Argument(name = "numTransactionsPerCustomer", position = 2, required = true, description = "The number of transactions to seed per customer") final int numTransactionsPerCustomer,
                             @Argument(name = "customerIdStart", position = 3, required = true, description = "The id to use for the first seeded customer (subsequent are monotonically increasing from the starting id))") final int customerIdStart,
                             @Argument(name = "numMerchants", position = 4, required = true, description = "The size of the merchant set to use for seeded transactions") final int numMerchants,
                             @Argument(name = "numMerchantStores", position = 5, required = true, description = "The size of the merchant store set to use for seeded transactions") final int numMerchantStores,
                             @Argument(name = "seedRate", position = 6, required = true, description = "The rate at which to seed customer transactions") final int seedRate) throws Exception {
        final Thread thread = new Thread(new Runnable() {
            private final UtlGovernor seedGoverner = new UtlGovernor(seedRate);
            private int numMerchantsSeeded = 0;
            private int numTransactionsSeeded = 0;
            private int numCardsSeeded = 0;

            @Override
            public void run() {
                tracer.log("Started merchant seed...", Tracer.Level.INFO);
                try {
                    while (numMerchantsSeeded < numMerchants && seedingThread.get() == Thread.currentThread()) {
                        messageSender.sendMessage("merchantrequests", createAddMerchantMessage(numMerchantsSeeded, numMerchantStores));
                        numMerchantsSeeded++;
                        if (numMerchantsSeeded % 1000 == 0) {
                            tracer.log("..." + numMerchantsSeeded + " merchants seeded.", Tracer.Level.INFO);
                        }
                        seedGoverner.blockToNext();
                    }
                }
                finally {
                    tracer.log("Seeding complete (" + numMerchantsSeeded + " merchants seeded)", Tracer.Level.INFO);
                }

                tracer.log("Started customer seed...", Tracer.Level.INFO);
                try {
                    while (numTransactionsSeeded < (numCustomers * numTransactionsPerCustomer) && seedingThread.get() == Thread.currentThread()) {
                        messageSender.sendMessage("custaddrequests", createAddTransactionMessage(numTransactionsSeeded, numTransactionsPerCustomer, customerIdStart, numMerchants, numMerchantStores));
                        numTransactionsSeeded++;
                        if (numTransactionsSeeded % 10000 == 0) {
                            tracer.log("..." + numTransactionsSeeded + " transactions seeded.", Tracer.Level.INFO);
                        }
                        seedGoverner.blockToNext();
                    }
                }
                finally {
                    tracer.log("Seeding complete (" + numTransactionsSeeded + " transactions seeded)", Tracer.Level.INFO);
                }

                tracer.log("Started card seed...", Tracer.Level.INFO);
                try {
                    while (numCardsSeeded < numCustomers && seedingThread.get() == Thread.currentThread()) {
                        messageSender.sendMessage("cardaddrequests", createAddCardMessage(numCardsSeeded, customerIdStart));
                        numCardsSeeded++;
                        if (numCardsSeeded % 10000 == 0) {
                            tracer.log("..." + numCardsSeeded + " cards seeded.", Tracer.Level.INFO);
                        }
                        seedGoverner.blockToNext();
                    }
                }
                finally {
                    tracer.log("Seeding complete (" + numCardsSeeded + " cards seeded)", Tracer.Level.INFO);
                }

                seedDone = true;
            }
        }, "CCAuth Data Seeder");

        Thread oldThread = seedingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
            oldThread.join();
        }
        tracer.log("--------------------------", Tracer.Level.INFO);
        tracer.log("Data Seed Parameters {", Tracer.Level.INFO);
        tracer.log("...numCustomers=" + numCustomers, Tracer.Level.INFO);
        tracer.log("...numTransactionsPerCustomer=" + numTransactionsPerCustomer, Tracer.Level.INFO);
        tracer.log("...customerIdStart=" + customerIdStart, Tracer.Level.INFO);
        tracer.log("...numMerchants=" + numMerchants, Tracer.Level.INFO);
        tracer.log("...numMerchantStores=" + numMerchantStores, Tracer.Level.INFO);
        tracer.log("...seedRate=" + seedRate, Tracer.Level.INFO);
        tracer.log("}", Tracer.Level.INFO);
        tracer.log("--------------------------", Tracer.Level.INFO);
        thread.start();
    }

    @Command(name = "stopSeeding", displayName = "Stop Seeding", description = "Stops seeding of data.")
    final public void stop() throws Exception {
        Thread oldThread = seedingThread.getAndSet(null);
        if (oldThread != null) {
            oldThread.join();
        }
    }

    @AppMain
    public void run(String[] args) throws Exception {
        if (autoStart) {
            doSeed(numCustomers, numTransactionsPerCustomer, customerIdStart, numMerchants, numMerchantStores, seedRate);
        }
    }

    final public boolean seedDone() {
        return seedDone;
    }
}
