/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the GoenJ library.
 *
 * The GoenJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The GoenJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the GoenJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goen.core.txn;


import com.medici.firestar.net.server.Channel;
import com.medici.firestar.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;


public class TxnBaseTask implements Callable<List<TxnBase>> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private final List<TxnBase> tx;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public TxnBaseTask(TxnBase tx, ChannelManager channelManager) {
        this(Collections.singletonList(tx), channelManager);
    }

    public TxnBaseTask(List<TxnBase> tx, ChannelManager channelManager) {
        this(tx, channelManager, null);
    }

    public TxnBaseTask(List<TxnBase> tx, ChannelManager channelManager, Channel receivedFrom) {
        this.tx = tx;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<TxnBase> call() throws Exception {

        try {
            logger.info("submit txnBase: {}", tx.toString());
            channelManager.sendTxnBases(tx, receivedFrom);
            return tx;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
