package io.goen.core;

import com.google.common.base.MoreObjects;

public class AddressAndKey {

	private String address;

	private String priKey;

	private String pubKey;

	private String newAddr;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPriKey() {
		return priKey;
	}

	public void setPriKey(String priKey) {
		this.priKey = priKey;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public String getNewAddr() {
		return newAddr;
	}

	public void setNewAddr(String newAddr) {
		this.newAddr = newAddr;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(AddressAndKey.class).add("address", address)
            .add("newAddr", newAddr)
            .add("priKey", priKey).add(
				"pubKey", pubKey).toString();
	}
}
