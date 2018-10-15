package com.neeve.ccauth.customerservice;

import java.util.Iterator;
import java.util.Random;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.AepEngineDescriptor;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Configured;
import com.neeve.lang.XString;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;
import com.neeve.xbuf.XbufStringField;

import com.neeve.ccauth.roe.messages.*;
import com.neeve.ccauth.customerservice.state.Repository;
import com.neeve.ccauth.customerservice.state.Customers;
import com.neeve.ccauth.customerservice.state.Customer;
import com.neeve.ccauth.customerservice.state.Transaction;
import com.neeve.ccauth.merchantservice.messages.*;

@AppHAPolicy(value=AepEngine.HAPolicy.StateReplication)
public class Application {
    @Configured(property = "ccauth.customerservice.transactionhistoryretention")
    private int transactionHistoryRetention;
    @Configured(property = "ccauth.customerservice.rollup.getstring")
    private boolean getStringDuringRollup;
    @Configured(property = "ccauth.customerservice.rollup.getjavastring")
    private boolean getJavaStringDuringRollup;
    @AppStat
    final private Counter customerCount = StatsFactory.createCounterStat("Customer Count");
    @AppStat
    final private Counter transactionCount = StatsFactory.createCounterStat("Transaction Count");
    final private static Random random = new Random(System.currentTimeMillis());
    private String identity;
    private AepMessageSender messageSender;

    final private static XString stringValue = XString.create(32, true, true);
    final private static void getValue(final XbufStringField field, final boolean getString, final boolean getJavaString) {
        if (getString) {
            field.getValueTo(stringValue); 
            if (getJavaString) {
                stringValue.getValue();
            }
        }
    }

    final static String randomString(final int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.valueOf(random.nextInt(10)));
        }
        return sb.toString();
    }

    final private void pruneTransactionHistory(final Customer customer) {
        if (transactionHistoryRetention > 0) {
            while (customer.getTransactionHistory().size() > transactionHistoryRetention) {
                customer.getTransactionHistory().poll();
            }
        }
    }

    final private static AccountVariables accountVariables = new AccountVariables();
    final public static AccountVariables rollup(final Customer customer, final boolean getString, final boolean getJavaString) {
        final Iterator<Transaction> iterator = customer.getTransactionHistory().iterator(); 
        while (iterator.hasNext()) {
            final Transaction transaction = iterator.next();
            transaction.getAccountNumber();
            transaction.getTxnId();
            transaction.getTxnTimestamp();
            transaction.getTxnAmount();
            getValue(transaction.getMerchantNumberField(), getString, getJavaString); // string
            transaction.getTrasactionHistoryKey();
            getValue(transaction.getCMHistoryStringVariable1Field(), getString, getJavaString); // string
            getValue(transaction.getCMHistoryStringVariable2Field(), getString, getJavaString); // string
            getValue(transaction.getCMHistoryStringVariable3Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable1Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable2Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable3Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable4Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable5Field(), getString, getJavaString); // string
            getValue(transaction.getProfileStringVariable6Field(), getString, getJavaString); // string
            transaction.getProfileLongVariable1();
            transaction.getProfileLongVariable2();
            getValue(transaction.getSEProfStringVariable2Field(), getString, getJavaString); // string
            getValue(transaction.getSEProfStringVariable3Field(), getString, getJavaString); // string
            getValue(transaction.getSEProfStringVariable4Field(), getString, getJavaString); // string
            getValue(transaction.getSEProfStringVariable5Field(), getString, getJavaString); // string
            getValue(transaction.getSEProfStringVariable6Field(), getString, getJavaString); // string
            getValue(transaction.getSEProfStringVariable7Field(), getString, getJavaString); // string
            transaction.getSEProfLongVariable1();
            transaction.getSEProfLongVariable2();
            transaction.getSEProfDoubleVariable();
            transaction.getSEProfDoubleVariable1();
            getValue(transaction.getTRSStringVariable1Field(), getString, getJavaString); // string
            getValue(transaction.getTRSStringVariable2Field(), getString, getJavaString); // string
            getValue(transaction.getTRSStringVariable3Field(), getString, getJavaString); // string
            transaction.getTRSIntVariable1();
            transaction.getTRSLongVariable1();
            transaction.getTRSLongVariable2();
            transaction.getTRSDoubleVariable1();
            transaction.getTRSDoubleVariable2();
            transaction.getTRSDoubleVariable3();
            transaction.getTRSDoubleVariable4();
            transaction.getTRSDoubleVariable5();
        }
        return accountVariables.init(customer.getCustomerId(), "1111111111111111");
    }

    @AppStateFactoryAccessor
    final public IAepApplicationStateFactory getStateFactory() {
        return new IAepApplicationStateFactory() {
            @Override
            final public Repository createState(MessageView view) {
                return Repository.create();
            }
        };
    }

    @AppInjectionPoint
    final public void setIdentity(final AepEngineDescriptor descriptor) {
        this.identity = descriptor.getName();
    }

    @AppInjectionPoint
    final public void setMessageSender(final AepMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @EventHandler
    final public void onTransactionAdd(final AddTransactionMessage message, final Repository repository) {
        // get the transaction
        final TransactionDTO transactionDTO = message.getTransaction();

        // get customer (create if not present)
        final Customers customers = repository.getCustomers();
        Customer customer = customers.get(transactionDTO.getCustomerId());
        if (customer == null) {
            customer = Customer.create();
            customer.setCustomerIdFrom(transactionDTO.getCustomerIdUnsafe());
            customer.setProfileFirstName(randomString(20));
            customer.setProfileLastName(randomString(20));
            customer.setProfilePrimaryEmail(randomString(10) + "@gmail.com");
            customer.setProfilePrimaryPhoneNumber("111-111-1111");
            customer.setProfileStringVariable1(randomString(20));
            customer.setProfileStringVariable2(randomString(20));
            customer.setProfileStringVariable3(randomString(20));
            customer.setProfileStringVariable4(randomString(20));
            customer.setProfileStringVariable5(randomString(20));
            customer.setProfileStringVariable6(randomString(10));
            customer.setProfileLongVariable1(random.nextLong());
            customer.setProfileLongVariable2(random.nextLong());
            customers.put(customer.getCustomerId(), customer);
            customerCount.increment();
        }

        // add transaction to customer transaction history
        final Transaction transaction = Transaction.create();
        transaction.setCustomerIdFrom(transactionDTO.getCustomerIdUnsafe());
        transaction.setAccountNumberFrom(transactionDTO.getAccountNumberUnsafe());
        transaction.setTxnId(transactionDTO.getTxnId());
        transaction.setTxnTimestamp(transactionDTO.getTxnTimestamp());
        transaction.setTxnAmount(transactionDTO.getTxnAmount());
        transaction.setMerchantNumberFrom(transactionDTO.getMerchantNumberUnsafe());
        transaction.setCMHistoryStringVariable1From(transactionDTO.getCMHistoryStringVariable1Unsafe());
        transaction.setCMHistoryStringVariable2From(transactionDTO.getCMHistoryStringVariable2Unsafe());
        transaction.setCMHistoryStringVariable3From(transactionDTO.getCMHistoryStringVariable3Unsafe());
        transaction.setProfileStringVariable1From(transactionDTO.getProfileStringVariable1Unsafe());
        transaction.setProfileStringVariable2From(transactionDTO.getProfileStringVariable2Unsafe());
        transaction.setProfileStringVariable3From(transactionDTO.getProfileStringVariable3Unsafe());
        transaction.setProfileStringVariable4From(transactionDTO.getProfileStringVariable4Unsafe());
        transaction.setProfileStringVariable5From(transactionDTO.getProfileStringVariable5Unsafe());
        transaction.setProfileStringVariable6From(transactionDTO.getProfileStringVariable6Unsafe());
        transaction.setProfileLongVariable1(transactionDTO.getProfileLongVariable1());
        transaction.setProfileLongVariable2(transactionDTO.getProfileLongVariable2());
        transaction.setSEProfStringVariable2From(transactionDTO.getSEProfStringVariable2Unsafe());
        transaction.setSEProfStringVariable3From(transactionDTO.getSEProfStringVariable3Unsafe());
        transaction.setSEProfStringVariable4From(transactionDTO.getSEProfStringVariable4Unsafe());
        transaction.setSEProfStringVariable5From(transactionDTO.getSEProfStringVariable5Unsafe());
        transaction.setSEProfStringVariable6From(transactionDTO.getSEProfStringVariable6Unsafe());
        transaction.setSEProfStringVariable7From(transactionDTO.getSEProfStringVariable7Unsafe());
        transaction.setSEProfLongVariable1(transactionDTO.getSEProfLongVariable1());
        transaction.setSEProfLongVariable2(transactionDTO.getSEProfLongVariable2());
        transaction.setSEProfDoubleVariable(transactionDTO.getSEProfDoubleVariable());
        transaction.setSEProfDoubleVariable1(transactionDTO.getSEProfDoubleVariable1());
        transaction.setTRSStringVariable1From(transactionDTO.getTRSStringVariable1Unsafe());
        transaction.setTRSStringVariable2From(transactionDTO.getTRSStringVariable2Unsafe());
        transaction.setTRSStringVariable3From(transactionDTO.getTRSStringVariable3Unsafe());
        transaction.setTRSIntVariable1(transactionDTO.getTRSIntVariable1());
        transaction.setTRSLongVariable1(transactionDTO.getTRSLongVariable1());
        transaction.setTRSLongVariable2(transactionDTO.getTRSLongVariable2());
        transaction.setTRSDoubleVariable1(transactionDTO.getTRSDoubleVariable1());
        transaction.setTRSDoubleVariable2(transactionDTO.getTRSDoubleVariable2());
        transaction.setTRSDoubleVariable3(transactionDTO.getTRSDoubleVariable3());
        transaction.setTRSDoubleVariable4(transactionDTO.getTRSDoubleVariable4());
        transaction.setTRSDoubleVariable5(transactionDTO.getTRSDoubleVariable5());
        customer.getTransactionHistory().offer(transaction);
        transactionCount.increment();
        pruneTransactionHistory(customer);
    }

    @EventHandler
    final public void onAuthorizationRequest(final CustomerEnrichedAuthorizationRequestMessage request, final Repository repository) {
        // validate customer is present
        final Customer customer = repository.getCustomers().get(request.getCustomerId());
        if (customer == null) {
            throw new IllegalArgumentException("customer " + request.getCustomerId() + ", is not present");
        }
        
        // add TRS1StringVariable1 from request to customer
        customer.setTRSStringVariable1From(request.getTRSStringVariable1Unsafe());

        // create a transaction and add it to the requests table. It sits there till remaining transaction data has been fetched from reference data stores
        final Transaction transaction = Transaction.create();
        transaction.setCustomerIdFrom(request.getCustomerIdUnsafe());
        transaction.setAccountNumberFrom(request.getAccountNumberUnsafe());
        transaction.setTxnId(request.getTxnId());
        transaction.setTxnTimestamp(request.getTxnTimestamp());
        transaction.setTxnAmount(request.getTxnAmount());
        transaction.setMerchantNumberFrom(request.getMerchantNumberUnsafe());
        transaction.setTRSStringVariable1From(request.getTRSStringVariable1Unsafe());
        transaction.setTRSStringVariable2From(request.getTRSStringVariable2Unsafe());
        transaction.setTRSStringVariable3From(request.getTRSStringVariable3Unsafe());
        transaction.setTRSIntVariable1(request.getTRSIntVariable1());
        transaction.setTRSLongVariable1(request.getTRSLongVariable1());
        transaction.setTRSLongVariable2(request.getTRSLongVariable2());
        transaction.setTRSDoubleVariable1(request.getTRSDoubleVariable1());
        transaction.setTRSDoubleVariable2(request.getTRSDoubleVariable2());
        transaction.setTRSDoubleVariable3(request.getTRSDoubleVariable3());
        transaction.setTRSDoubleVariable4(request.getTRSDoubleVariable4());
        transaction.setTRSDoubleVariable5(request.getTRSDoubleVariable5());
        repository.getRequests().put(request.getTxnId(), transaction);

        // send request for merchant store info
        final GetMerchantInfoRequestMessage getMerchantInfoRequest = GetMerchantInfoRequestMessage.create();
        getMerchantInfoRequest.setTxnId(request.getTxnId());
        getMerchantInfoRequest.setRequestorId(identity);
        getMerchantInfoRequest.setMerchantNumberFrom(request.getMerchantNumberUnsafe());
        messageSender.sendMessage("merchantrequests", getMerchantInfoRequest);
    }

    @EventHandler
    final public void onGetMerchantInfoResponse(final GetMerchantInfoResponseMessage response, final Repository repository) {
        // retrieve and remove the transaction for this response from the requests table
        // ...in reality we would only remove if all reference data responses for this request have been fetched
        final Transaction transaction = repository.getRequests().remove(response.getTxnId());
        transaction.acquire();

        // add merchant information to the transaction
        // ...TODO

        // update transacion count
        transactionCount.increment();

        // lookup customer
        final Customer customer = repository.getCustomers().get(transaction.getCustomerId());

        // perform rollup to calculate customer account variables
        final AccountVariables accountVariables = rollup(customer, getStringDuringRollup, getJavaStringDuringRollup);

        // send auth response with account variables
        // ...per this flow, the downstream micro app uses the account variables to perform the
        //    actual fraud check, prepares a (transaction) and sends it back to be added
        //    to the transaction history for use with subsequent transactions. This last leg
        //    is not implemented as part of the training apps
        AuthorizationResponseMessage authResponse = AuthorizationResponseMessage.create();
        authResponse.setTxnId(response.getTxnId());
        authResponse.setTxnTimestamp(transaction.getTxnTimestamp());
        authResponse.setCustomerIdFrom(transaction.getCustomerIdUnsafe());
        authResponse.setAccountNumberFrom(transaction.getAccountNumberUnsafe());
        authResponse.setCustomerVar1String(accountVariables.CustomerVar1String);
        authResponse.setCustomerVar2String(accountVariables.CustomerVar2String);
        authResponse.setCustomerVar3String(accountVariables.CustomerVar3String);
        authResponse.setCustomerVar4String(accountVariables.CustomerVar4String);
        authResponse.setCustomerVar5String(accountVariables.CustomerVar5String);
        authResponse.setCustomerVar6String(accountVariables.CustomerVar6String);
        authResponse.setCustomerVar7String(accountVariables.CustomerVar7String);
        authResponse.setCustomerVar8String(accountVariables.CustomerVar8String);
        authResponse.setCustomerVar9String(accountVariables.CustomerVar9String);
        authResponse.setCustomerVar10String(accountVariables.CustomerVar10String);
        authResponse.setCustomerVar11String(accountVariables.CustomerVar11String);
        authResponse.setCustomerVar12String(accountVariables.CustomerVar12String);
        authResponse.setCustomerVar1Int(accountVariables.CustomerVar1Int);
        authResponse.setCustomerVar2Int(accountVariables.CustomerVar2Int);
        authResponse.setCustomerVar3Int(accountVariables.CustomerVar3Int);
        authResponse.setCustomerVar4Int(accountVariables.CustomerVar4Int);
        authResponse.setCustomerVar5Int(accountVariables.CustomerVar5Int);
        authResponse.setCustomerVar6Int(accountVariables.CustomerVar6Int);
        authResponse.setCustomerVar7Int(accountVariables.CustomerVar7Int);
        authResponse.setCustomerVar8Int(accountVariables.CustomerVar8Int);
        authResponse.setCustomerVar1Long(accountVariables.CustomerVar1Long);
        authResponse.setCustomerVar2Long(accountVariables.CustomerVar2Long);
        authResponse.setCustomerVar3Long(accountVariables.CustomerVar3Long);
        authResponse.setCustomerVar4Long(accountVariables.CustomerVar4Long);
        authResponse.setCustomerVar5Long(accountVariables.CustomerVar5Long);
        authResponse.setCustomerVar6Long(accountVariables.CustomerVar6Long);
        authResponse.setCustomerVar7Long(accountVariables.CustomerVar7Long);
        authResponse.setCustomerVar8Long(accountVariables.CustomerVar8Long);
        authResponse.setCustomerVar1Double(accountVariables.CustomerVar1double);
        authResponse.setCustomerVar2Double(accountVariables.CustomerVar2double);
        authResponse.setCustomerVar3Double(accountVariables.CustomerVar3double);
        authResponse.setCustomerVar4Double(accountVariables.CustomerVar4double);
        authResponse.setCustomerVar5Double(accountVariables.CustomerVar5double);
        authResponse.setCustomerVar6Double(accountVariables.CustomerVar6double);
        authResponse.setCustomerVar7Double(accountVariables.CustomerVar7double);
        authResponse.setCustomerVar8Double(accountVariables.CustomerVar8double);
        messageSender.sendMessage("authresponses", authResponse);
    }
}
