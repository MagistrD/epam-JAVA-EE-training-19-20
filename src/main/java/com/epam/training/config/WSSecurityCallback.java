package com.epam.training.config;

import org.apache.log4j.Logger;
import org.apache.wss4j.common.ext.WSPasswordCallback;


import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class WSSecurityCallback implements CallbackHandler {
    private Logger logger = Logger.getLogger(WSSecurityCallback.class);

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        WSPasswordCallback callback = (WSPasswordCallback) callbacks[0];
        logger.info("Identifier: " + callback.getIdentifier());

        // you won't be able to retrieve the password using callback.getPassword().
        // to authenticate a user, you'll need to set the password tied to the user.
        // user credentials are typically retrieved from DB or your own authentication source.
        // if the password set here is the same as the password passed by caller, authentication is successful.
        if (callback.getIdentifier().equals("user")) {
            callback.setPassword("123");
        }
    }
}