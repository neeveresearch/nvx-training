package com.neeve.ccauth.cardservice;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;

import com.neeve.ccauth.roe.messages.*;
import com.neeve.ccauth.cardservice.messages.*;
import com.neeve.ccauth.cardservice.state.Repository;
import com.neeve.ccauth.cardservice.state.Card;

@AppHAPolicy(value=AepEngine.HAPolicy.StateReplication)
public class Application {
    @Configured(property = "ccauth.customerservice.numpartitions")
    private int numPartitions;
    private AepMessageSender messageSender;

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
    final public void setMessageSender(final AepMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @EventHandler
    final public void onCardAdd(final AddCardMessage message, final Repository repository) {
        Card card = repository.getCards().get(message.getAccountNumber());
        if (card == null) {
            card = Card.create();
            card.setAccountNumber(message.getAccountNumber());
            repository.getCards().put(card.getAccountNumber(), card);
        }
        card.setCustomerIdFrom(message.getCustomerIdUnsafe());
    }

    @EventHandler
    final public void onAuthMessage(final AuthorizationRequestMessage request, final Repository repository) {
        final Card card = repository.getCards().get(request.getAccountNumber());
        if (card == null) {
            throw new IllegalArgumentException("unknown card " + request.getAccountNumber());
        }
        CustomerEnrichedAuthorizationRequestMessage enrichedRequest = CustomerEnrichedAuthorizationRequestMessage.create(); 
        enrichedRequest.setTxnId(request.getTxnId());
        enrichedRequest.setAccountNumberFrom(request.getAccountNumberUnsafe());
        enrichedRequest.setTxnId(request.getTxnId());
        enrichedRequest.setTxnTimestamp(request.getTxnTimestamp());
        enrichedRequest.setTxnAmount(request.getTxnAmount());
        enrichedRequest.setMerchantNumberFrom(request.getMerchantNumberUnsafe());
        enrichedRequest.setTRSStringVariable1From(request.getTRSStringVariable1Unsafe());
        enrichedRequest.setTRSStringVariable2From(request.getTRSStringVariable2Unsafe());
        enrichedRequest.setTRSStringVariable3From(request.getTRSStringVariable3Unsafe());
        enrichedRequest.setTRSIntVariable1(request.getTRSIntVariable1());
        enrichedRequest.setTRSLongVariable1(request.getTRSLongVariable1());
        enrichedRequest.setTRSLongVariable2(request.getTRSLongVariable2());
        enrichedRequest.setTRSDoubleVariable1(request.getTRSDoubleVariable1());
        enrichedRequest.setTRSDoubleVariable2(request.getTRSDoubleVariable2());
        enrichedRequest.setTRSDoubleVariable3(request.getTRSDoubleVariable3());
        enrichedRequest.setTRSDoubleVariable4(request.getTRSDoubleVariable4());
        enrichedRequest.setTRSDoubleVariable5(request.getTRSDoubleVariable5());
        enrichedRequest.setCustomerIdFrom(card.getCustomerIdUnsafe());
        enrichedRequest.setPartition((short)((Integer.parseInt(card.getCustomerId()) % numPartitions) + 1));
        messageSender.sendMessage("eauthrequests", enrichedRequest);
    }
}

