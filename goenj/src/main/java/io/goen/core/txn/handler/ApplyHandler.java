package io.goen.core.txn.handler;

import com.google.common.base.Charsets;
import com.medici.firestar.core.BlockchainImpl;
import com.medici.firestar.core.Repository;
import com.medici.firestar.core.txn.Apply;
import com.medici.firestar.core.txn.TxnBase;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.witness.WitnessTypeEnum;
import com.medici.firestar.witness.common.WitnessObject;
import org.spongycastle.util.encoders.Hex;


public class ApplyHandler implements TransactionHandler {
    private final BlockchainImpl blockchain;
    private final Repository repository;

    public ApplyHandler(BlockchainImpl blockchain, Repository repository) {
        this.blockchain = blockchain;
        this.repository = repository;
    }

    @Override
    public void handle(TxnBase tx) {
        Apply apply = (Apply) tx;
        ECDSASignature signature = apply.getSignature();
        String pubKey = AddressUtil.getPubKeyFromSignature(signature, apply.encodeForSign());
        //
        WitnessObject wo = new WitnessObject();
        wo.setApplyHash(Hex.toHexString(apply.getHash()));
        wo.setPublicKey(Hex.decode(pubKey));
        wo.setOwner(new String(apply.getWitnessName(), Charsets.UTF_8));
        wo.setWitnessType(WitnessTypeEnum.MINER);
        wo.setVotes(0);
        wo.setWitnessAddr(AddressUtil.addressToBytes(AddressUtil.getAddressFromPubKey(AddressUtil.getAddressFromPubKey(pubKey))));
        wo.setCreated(ByteUtil.byteArrayToLong(apply.getTimestamp()));
        blockchain.addWitness(repository, wo);
    }
}
