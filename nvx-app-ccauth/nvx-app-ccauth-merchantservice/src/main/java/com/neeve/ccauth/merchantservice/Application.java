package com.neeve.ccauth.merchantservice;

import java.util.Iterator;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;

import com.neeve.ccauth.merchantservice.messages.*;
import com.neeve.ccauth.merchantservice.state.Repository;
import com.neeve.ccauth.merchantservice.state.Merchant;
import com.neeve.ccauth.merchantservice.state.MerchantStore;

@AppHAPolicy(value = AepEngine.HAPolicy.StateReplication)
public class Application {
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
    final public void onMerchantAdd(final AddMerchantMessage message, final Repository repository) {
        Merchant merchant = repository.getMerchants().get(message.getMerchantNumber());
        if (merchant == null) {
            merchant = Merchant.create();
            merchant.setMerchantNumber(message.getMerchantNumber());
            repository.getMerchants().put(merchant.getMerchantNumber(), merchant);
        }
        final Iterator<MerchantStoreDTO> iterator = message.getMerchantStoreListIterator();
        while (iterator.hasNext()) {
            final MerchantStoreDTO storeDTO = iterator.next();
            final MerchantStore store = MerchantStore.create();
            store.setMerchantStoreId(storeDTO.getMerchantStoreId());
            store.setMerchantStoreCountryCodeFrom(storeDTO.getMerchantStoreCountryCodeUnsafe());
            store.setMerchantStorePostCodeFrom(storeDTO.getMerchantStorePostCodeUnsafe());
            merchant.getStores().offer(store);
        }
    }

    @EventHandler
    final public void getMerchantInfo(final GetMerchantInfoRequestMessage request, final Repository repository) {
        final GetMerchantInfoResponseMessage response = GetMerchantInfoResponseMessage.create();
        response.setTxnId(request.getTxnId());
        response.setRequestorId(request.getRequestorId());
        final Merchant merchant = repository.getMerchants().get(request.getMerchantNumber());
        if (merchant == null) {
            throw new IllegalArgumentException("unknown merchant " + request.getMerchantNumber());
        }
        // TODO: Get merchant info
        messageSender.sendMessage("merchantresponses", response);
    }
}
