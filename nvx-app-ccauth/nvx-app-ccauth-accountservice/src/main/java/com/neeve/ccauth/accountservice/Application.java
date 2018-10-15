package com.neeve.ccauth.accountservice;

import com.neeve.aep.AepEngine;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;
import com.neeve.sma.MessageView;

import com.neeve.ccauth.accountservice.state.Repository;

@AppHAPolicy(value=AepEngine.HAPolicy.StateReplication)
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
}
