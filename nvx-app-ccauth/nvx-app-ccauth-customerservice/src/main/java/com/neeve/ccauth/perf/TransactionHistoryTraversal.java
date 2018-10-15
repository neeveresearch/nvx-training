package com.neeve.ccauth.perf;

import java.util.Random;

import com.neeve.ci.XRuntime;
import com.neeve.xbuf.XbufDesyncPolicy;
import com.neeve.ccauth.customerservice.state.Customer;
import com.neeve.ccauth.customerservice.state.Transaction;

/**
 * A perf program that tests the performance of traversal through a customer's transaction history
 */
final public class TransactionHistoryTraversal {
    final private static Random random = new Random(System.currentTimeMillis());

    static {
        Transaction.setDesyncPolicy(XbufDesyncPolicy.FrameFields);
    }

    final private static String randomString(final int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.valueOf(random.nextInt(10)));
        }
        return sb.toString();
    }

    final private static Transaction createTransaction() {
        final Transaction transaction = Transaction.create();
        transaction.setAccountNumber(randomString(16));
        transaction.setTxnId(System.nanoTime());
        transaction.setTxnTimestamp(System.nanoTime());
        transaction.setTxnAmount(random.nextDouble());
        transaction.setMerchantNumber(randomString(10));
        transaction.setTrasactionHistoryKey(random.nextLong());
        transaction.setCMHistoryStringVariable1(randomString(20));
        transaction.setCMHistoryStringVariable2(randomString(20));
        transaction.setCMHistoryStringVariable3(randomString(20));
        transaction.setProfileStringVariable1(randomString(20));
        transaction.setProfileStringVariable2(randomString(20));
        transaction.setProfileStringVariable3(randomString(20));
        transaction.setProfileStringVariable4(randomString(20));
        transaction.setProfileStringVariable5(randomString(20));
        transaction.setProfileStringVariable6(randomString(20));
        transaction.setProfileLongVariable1(random.nextLong());
        transaction.setProfileLongVariable2(random.nextLong());
        transaction.setSEProfStringVariable2(randomString(20));
        transaction.setSEProfStringVariable3(randomString(20));
        transaction.setSEProfStringVariable4(randomString(20));
        transaction.setSEProfStringVariable5(randomString(20));
        transaction.setSEProfStringVariable6(randomString(20));
        transaction.setSEProfStringVariable7(randomString(20));
        transaction.setSEProfLongVariable1(random.nextLong());
        transaction.setSEProfLongVariable2(random.nextLong());
        transaction.setSEProfDoubleVariable(random.nextDouble());
        transaction.setSEProfDoubleVariable1(random.nextDouble());
        transaction.setTRSStringVariable1(randomString(20));
        transaction.setTRSStringVariable2(randomString(20));
        transaction.setTRSStringVariable3(randomString(20));
        transaction.setTRSIntVariable1(random.nextInt());
        transaction.setTRSLongVariable1(random.nextLong());
        transaction.setTRSLongVariable2(random.nextLong());
        transaction.setTRSDoubleVariable1(random.nextDouble());
        transaction.setTRSDoubleVariable2(random.nextDouble());
        transaction.setTRSDoubleVariable3(random.nextDouble());
        transaction.setTRSDoubleVariable4(random.nextDouble());
        transaction.setTRSDoubleVariable5(random.nextDouble());
        return transaction;
    }

    final public static void main(final String args[]) {
        Customer customer = Customer.create();
        for (int i = 0; i < 20000; i++) {
            customer.getTransactionHistory().offer(createTransaction());
        }
        for (int i = 0; i < 100; i++) {
            final long start = System.currentTimeMillis();
            com.neeve.ccauth.customerservice.Application.rollup(customer, XRuntime.getValue("getString", true), XRuntime.getValue("getJavaString", false));
            System.out.println("Run #" + i + ": " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
