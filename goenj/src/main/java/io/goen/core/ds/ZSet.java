package io.goen.core.ds;

import com.google.common.base.Charsets;
import com.medici.firestar.core.AccountState;
import com.medici.firestar.core.BlockchainImpl;
import com.medici.firestar.core.Repository;
import com.medici.firestar.core.txn.TxnBase;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.crypto.HashUtil;
import com.medici.firestar.db.ByteArrayWrapper;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.witness.WitnessTypeEnum;
import com.medici.firestar.witness.common.WitnessObject;
import org.apache.commons.lang3.ArrayUtils;
import org.spongycastle.util.encoders.Hex;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ZSet {
    private Map<ByteArrayWrapper, WitnessObject> addr2Witness;
    private TreeSet<WitnessObject> witnessObjects;
    private ReadWriteLock readWriteLock;
    private BlockchainImpl blockchain;

    public void init(BlockchainImpl blockchain) {
        readWriteLock = new ReentrantReadWriteLock();
        addr2Witness = new HashMap<>();
        witnessObjects = new TreeSet<>((o1, o2) -> {
            long result = o2.getVotes() - o1.getVotes();
            if (result > 0) {
                return 1;
            }
            if (result < 0) {
                return -1;
            }
            return 0;
        });
        this.blockchain = blockchain;
    }

    public boolean add(Repository track, WitnessObject wo) {
        boolean isValid = validateWitnessObjet(track, wo);
        if (isValid) {
            Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                ByteArrayWrapper paramAddr = new ByteArrayWrapper(wo.getWitnessAddr());
                String paramName = wo.getOwner();
                track.getName2PubK().put(paramName.getBytes(Charsets.UTF_8), wo.getPublicKey());
                addr2Witness.put(paramAddr, wo);
                witnessObjects.add(wo);
                AccountState accountState = track.getAccountState(paramAddr.getData());
                accountState.setIdentity(1);
                accountState.setTxHash(Hex.decode(wo.getApplyHash()));
            } finally {
                writeLock.unlock();
            }

        }
        return isValid;
    }

    private boolean validateWitnessObjet(Repository repository, WitnessObject wo) {
        boolean isValid = false;
        ByteArrayWrapper paramPubK = new ByteArrayWrapper(wo.getPublicKey());
        String paramName = wo.getOwner();
        ByteArrayWrapper inCachePubK = new ByteArrayWrapper(repository.getName2PubK().get(paramName.getBytes(Charsets
            .UTF_8)));
        if (inCachePubK != null) {
            if (!paramPubK.equals(inCachePubK)) {
                return isValid;
            }
            ByteArrayWrapper paramAddr = new ByteArrayWrapper(wo.getWitnessAddr());
            ByteArrayWrapper inCacheAddr = new ByteArrayWrapper(AddressUtil.addressToBytes(AddressUtil
                .getAddressFromPubKey(inCachePubK.getData
                ())));
            if (!paramAddr.equals(inCacheAddr)) {
                return isValid;
            }

            WitnessObject witnessObject = addr2Witness.get(inCachePubK);
            if (witnessObject == null) {
                //witness
                //
                AccountState accountState = repository.getAccountState(inCachePubK.getData());
                if (accountState.getTotalVotes().compareTo(BigInteger.ZERO) <= 0) {
                    return isValid;
                }
                if (accountState.getIdentity() != 1) {
                    return isValid;
                }
                addr2Witness.put(paramAddr, wo);
                witnessObjects.add(wo);
                witnessObject = wo;
            }
            byte[] pubKey = witnessObject.getPublicKey();
            isValid = AddressUtil.checkAddressByPubKey(witnessObject.getWitnessAddr(), pubKey);
            if (!isValid) {
                return isValid;
            }
        }
        isValid = true;
        return isValid;
    }

    public WitnessObject getWitenessByName(Repository track, @NotNull String witnessName) {
        WitnessObject witnessObject = null;
        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            ByteArrayWrapper inCachePubK = new ByteArrayWrapper(track.getName2PubK().get(witnessName.getBytes
                (Charsets.UTF_8)));
            if (inCachePubK != null) {
                ByteArrayWrapper inCacheAddr = new ByteArrayWrapper(AddressUtil.addressToBytes(AddressUtil
                    .getAddressFromPubKey(inCachePubK.getData())));
                witnessObject = getWitnessObject(track, witnessName, inCachePubK, inCacheAddr);
            }
        } finally {
            lock.unlock();
        }
        return witnessObject;
    }

    private WitnessObject getWitnessObject(Repository track, @NotNull String witnessName, ByteArrayWrapper
        inCachePubK, ByteArrayWrapper inCacheAddr) {
        WitnessObject witnessObject;
        witnessObject = addr2Witness.get(inCacheAddr);
        if (witnessObject == null) {
            AccountState accountState = track.getAccountState(inCacheAddr.getData());
            if (accountState != null) {
                witnessObject = constructWitnessObject(witnessName, inCachePubK, accountState);
                if (witnessObject != null) {
                    addr2Witness.put(inCacheAddr, witnessObject);
                    witnessObjects.add(witnessObject);
                }
            }
        }
        return witnessObject;
    }

    private WitnessObject constructWitnessObject(String userName, ByteArrayWrapper inCachePubK, AccountState accountState) {
        if (accountState.getIdentity() != 1) {
            return null;
        }
        if (accountState.getTotalVotes().compareTo(BigInteger.ZERO) <= 0) {
            return null;
        }

        if (accountState.getTxHash() == HashUtil.EMPTY_DATA_HASH) {
            return null;
        }

        if (ArrayUtils.isEmpty(accountState.getUsername())) {
            return null;
        }

        String username = new String(accountState.getUsername(), Charsets.UTF_8);
        if (!userName.equals(username)) {
            return null;
        }

        byte[] txHash = accountState.getTxHash();

        WitnessObject wo = new WitnessObject();
        wo.setPublicKey(inCachePubK.getData());
        wo.setOwner(userName);
        wo.setWitnessType(WitnessTypeEnum.MINER);
        wo.setVotes(accountState.getTotalVotes().longValue());
        wo.setWitnessAddr(AddressUtil.addressToBytes(AddressUtil.getAddressFromPubKey(AddressUtil.getAddressFromPubKey(inCachePubK.getData()))));
        wo.setApplyHash(Hex.toHexString(txHash));
        TxnBase txnBase = blockchain.getTxnBaseInfo(txHash).getTxnBase();
        wo.setCreated(ByteUtil.byteArrayToLong(txnBase.getTimestamp()));
        return wo;
    }

    public WitnessObject getWitnessByAddr(Repository track, byte[] addr) {
        WitnessObject witnessObject = null;
        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            ByteArrayWrapper addrWrapper = new ByteArrayWrapper(addr);
            AccountState accountState = track.getAccountState(addr);
            String username = new String(accountState.getUsername(), Charsets.UTF_8);
            ByteArrayWrapper inCachePubK = new ByteArrayWrapper(track.getName2PubK().get(username.getBytes(Charsets
                .UTF_8)));
            ByteArrayWrapper inCacheAddr = new ByteArrayWrapper(AddressUtil.addressToBytes(AddressUtil
                .getAddressFromPubKey(inCachePubK.getData())));
            if (!inCacheAddr.equals(addrWrapper)) {
                return null;
            }
            witnessObject = getWitnessObject(track, username, inCachePubK, addrWrapper);
        } finally {
            lock.unlock();
        }
        return witnessObject;
    }

    public void incrVotes(Repository track, byte[] addr) {
        ByteArrayWrapper addrWrapper = new ByteArrayWrapper(addr);
        doIncrVotes(track, addrWrapper);
    }

    public void incrVotes(Repository track, String witnessName) {
        ByteArrayWrapper inCachePubK = new ByteArrayWrapper(track.getName2PubK().get(witnessName.getBytes(Charsets
            .UTF_8)));
        if (inCachePubK != null) {
            ByteArrayWrapper inCacheAddr = new ByteArrayWrapper(AddressUtil.addressToBytes(AddressUtil
                .getAddressFromPubKey(inCachePubK
                .getData())));
            doIncrVotes(track, inCacheAddr);
        }
    }

    public List<WitnessObject> queryWitnessByVotes(int limit) {
        int i = 0;
        readWriteLock.readLock().lock();
        List<WitnessObject> topWitnesses;
        try {
            topWitnesses = new ArrayList<>();
            for (WitnessObject witnessObject : witnessObjects) {
                if (i == limit) {
                    break;
                }
                topWitnesses.add(witnessObject);
                i++;
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
        return topWitnesses;
    }

    public List<WitnessObject> getAllWitnesses() {
        readWriteLock.readLock().lock();
        try {
            return Arrays.asList(witnessObjects.toArray(new WitnessObject[witnessObjects.size()]));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void doIncrVotes(Repository track, ByteArrayWrapper addr) {
        WitnessObject witnessObject = addr2Witness.get(addr);
        if (witnessObject != null) {
            readWriteLock.writeLock().lock();
            try {
                witnessObject.setVotes(witnessObject.getVotes() + 1);
                witnessObjects.remove(witnessObject);
                witnessObjects.add(witnessObject);
                track.addVote(addr.getData(), BigInteger.ONE);
            } finally {
                readWriteLock.writeLock().unlock();
            }

        }
    }

}
