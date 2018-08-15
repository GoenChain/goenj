/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goen.crypto;

/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.crypto.KeyAgreement;

import io.goen.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.agreement.ECDHBasicAgreement;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

/**
 * <p>
 * Represents an elliptic curve public and (optionally) private key, usable for
 * digital signatures but not encryption. Creating a new ECKey with the empty
 * constructor will generate a new random keypair. Other static methods can be
 * used when you already have the public or private parts. If you create a key
 * with only the public part, you can check signatures but not create them.
 * </p>
 *
 * <p>
 * The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a
 * couple of discriminator bits can be reversed to find the public key used to
 * calculate it. This can be convenient when you have a message and a signature
 * and want to find out who signed it, rather than requiring the user to provide
 * the expected identity.
 * </p>
 *
 * This code is borrowed from the bitcoinj project and altered to fit Ethereum.<br>
 * See <a href=
 * "https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/com/google/bitcoin/core/ECKey.java"
 * > bitcoinj on GitHub</a>.
 */
public class ECKey implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(ECKey.class);

	/**
	 * The parameters of the secp256k1 curve that Ethereum uses.
	 */
	public static final ECDomainParameters CURVE;
	public static final ECParameterSpec CURVE_SPEC;

	public static final BigInteger N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);

	/**
	 * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value
	 * of a signature. ECDSA signatures are mutable in the sense that for a
	 * given (R, S) pair, then both (R, S) and (R, N - S mod N) are valid
	 * signatures. Canonical signatures are those where 1 <= S <= N/2
	 *
	 * See https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#
	 * Low_S_values_in_signatures
	 */
	public static final BigInteger HALF_CURVE_ORDER;

	private static final SecureRandom secureRandom;
	private static final long serialVersionUID = -728224901792295832L;

	static {
		// All clients must agree on the curve to use by agreement. Ethereum
		// uses secp256k1.
		X9ECParameters params = SECNamedCurves.getByName("secp256k1");
		CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
		CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
		HALF_CURVE_ORDER = params.getN().shiftRight(1);
		secureRandom = new SecureRandom();
	}

	// The two parts of the key. If "priv" is set, "pub" can always be
	// calculated. If "pub" is set but not "priv", we
	// can only verify signatures not make them.
	// TODO: Redesign this class to use consistent internals and more efficient
	// serialization.
	private final PrivateKey privKey;
	protected final ECPoint pub;

	// the Java Cryptographic Architecture provider to use for Signature
	// this is set along with the PrivateKey privKey and must be compatible
	// this provider will be used when selecting a Signature instance
	// https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html
	private final Provider provider;

	// Transient because it's calculated on demand.
	transient private byte[] pubKeyHash;
	transient private byte[] nodeId;

	/**
	 * Generates an entirely new keypair.
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 */
	public ECKey() {
		this(secureRandom);
	}

	/*
	 * Convert a Java JCE ECPublicKey into a BouncyCastle ECPoint
	 */
	private static ECPoint extractPublicKey(final ECPublicKey ecPublicKey) {
		final java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
		final BigInteger xCoord = publicPointW.getAffineX();
		final BigInteger yCoord = publicPointW.getAffineY();

		return CURVE.getCurve().createPoint(xCoord, yCoord);
	}

	/**
	 * Generate a new keypair using the given Java Security Provider.
	 *
	 * All private key operations will use the provider.
	 */
	public ECKey(Provider provider, SecureRandom secureRandom) {
		this.provider = provider;

		final KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, secureRandom);
		final KeyPair keyPair = keyPairGen.generateKeyPair();

		this.privKey = keyPair.getPrivate();

		final PublicKey pubKey = keyPair.getPublic();
		if (pubKey instanceof BCECPublicKey) {
			pub = ((BCECPublicKey) pubKey).getQ();
		} else if (pubKey instanceof ECPublicKey) {
			pub = extractPublicKey((ECPublicKey) pubKey);
		} else {
			throw new AssertionError("Expected Provider " + provider.getName()
					+ " to produce a subtype of ECPublicKey, found " + pubKey.getClass());
		}
	}

	/**
	 * Generates an entirely new keypair with the given {@link SecureRandom}
	 * object.
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 *
	 * @param secureRandom
	 *            -
	 */
	public ECKey(SecureRandom secureRandom) {
		this(SpongyCastleProvider.getInstance(), secureRandom);
	}

	/*
	 * Test if a generic private key is an EC private key
	 * 
	 * it is not sufficient to check that privKey is a subtype of ECPrivateKey
	 * as the SunPKCS11 Provider will return a generic PrivateKey instance a
	 * fallback that covers this case is to check the key algorithm
	 */
	private static boolean isECPrivateKey(PrivateKey privKey) {
		return privKey instanceof ECPrivateKey || privKey.getAlgorithm().equals("EC");
	}

	/**
	 * Pair a private key with a public EC point.
	 *
	 * All private key operations will use the provider.
	 */
	public ECKey(Provider provider, @Nullable PrivateKey privKey, ECPoint pub) {
		this.provider = provider;

		if (privKey == null || isECPrivateKey(privKey)) {
			this.privKey = privKey;
		} else {
			throw new IllegalArgumentException(
					"Expected EC private key, given a private key object with class "
							+ privKey.getClass().toString() + " and algorithm " + privKey.getAlgorithm());
		}

		if (pub == null) {
			throw new IllegalArgumentException("Public key may not be null");
		} else {
			this.pub = pub;
		}
	}

	/*
	 * Convert a BigInteger into a PrivateKey object
	 */
	private static PrivateKey privateKeyFromBigInteger(BigInteger priv) {
		if (priv == null) {
			return null;
		} else {
			try {
				return ECKeyFactory.getInstance(SpongyCastleProvider.getInstance()).generatePrivate(
						new ECPrivateKeySpec(priv, CURVE_SPEC));
			} catch (InvalidKeySpecException ex) {
				throw new AssertionError("Assumed correct key spec statically");
			}
		}
	}

	/**
	 * Pair a private key integer with a public EC point
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 */
	public ECKey(@Nullable BigInteger priv, ECPoint pub) {
		this(SpongyCastleProvider.getInstance(), privateKeyFromBigInteger(priv), pub);
	}

	/**
	 * Utility for compressing an elliptic curve point. Returns the same point
	 * if it's already compressed. See the ECKey class docs for a discussion of
	 * point compression.
	 *
	 * @param uncompressed
	 *            -
	 *
	 * @return -
	 * @deprecated per-point compression property will be removed in Bouncy
	 *             Castle
	 */
	public static ECPoint compressPoint(ECPoint uncompressed) {
		return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
	}

	/**
	 * Utility for decompressing an elliptic curve point. Returns the same point
	 * if it's already compressed. See the ECKey class docs for a discussion of
	 * point compression.
	 *
	 * @param compressed
	 *            -
	 *
	 * @return -
	 * @deprecated per-point compression property will be removed in Bouncy
	 *             Castle
	 */
	public static ECPoint decompressPoint(ECPoint compressed) {
		return CURVE.getCurve().decodePoint(compressed.getEncoded(false));
	}

	/**
	 * Creates an ECKey given the private key only.
	 *
	 * @param privKey
	 *            -
	 *
	 *
	 * @return -
	 */
	public static ECKey fromPrivate(BigInteger privKey) {
		return new ECKey(privKey, CURVE.getG().multiply(privKey));
	}

	/**
	 * Creates an ECKey given the private key only.
	 *
	 * @param privKeyBytes
	 *            -
	 *
	 * @return -
	 */
	public static ECKey fromPrivate(byte[] privKeyBytes) {
		return fromPrivate(new BigInteger(1, privKeyBytes));
	}

	/**
	 * Creates an ECKey that simply trusts the caller to ensure that point is
	 * really the result of multiplying the generator point by the private key.
	 * This is used to speed things up when you know you have the right values
	 * already. The compression state of pub will be preserved.
	 *
	 * @param priv
	 *            -
	 * @param pub
	 *            -
	 *
	 * @return -
	 */
	public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
		return new ECKey(priv, pub);
	}

	/**
	 * Creates an ECKey that simply trusts the caller to ensure that point is
	 * really the result of multiplying the generator point by the private key.
	 * This is used to speed things up when you know you have the right values
	 * already. The compression state of the point will be preserved.
	 *
	 * @param priv
	 *            -
	 * @param pub
	 *            -
	 * @return -
	 */
	public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
		check(priv != null, "Private key must not be null");
		check(pub != null, "Public key must not be null");
		return new ECKey(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
	}

	/**
	 * Creates an ECKey that cannot be used for signing, only verifying
	 * signatures, from the given point. The compression state of pub will be
	 * preserved.
	 *
	 * @param pub
	 *            -
	 * @return -
	 */
	public static ECKey fromPublicOnly(ECPoint pub) {
		return new ECKey(null, pub);
	}

	/**
	 * Creates an ECKey that cannot be used for signing, only verifying
	 * signatures, from the given encoded point. The compression state of pub
	 * will be preserved.
	 *
	 * @param pub
	 *            -
	 * @return -
	 */
	public static ECKey fromPublicOnly(byte[] pub) {
		return new ECKey(null, CURVE.getCurve().decodePoint(pub));
	}

	/**
	 * Returns a copy of this key, but with the public point represented in
	 * uncompressed form. Normally you would never need this: it's for
	 * specialised scenarios or when backwards compatibility in encoded form is
	 * necessary.
	 *
	 * @return -
	 * @deprecated per-point compression property will be removed in Bouncy
	 *             Castle
	 */
	public ECKey decompress() {
		if (!pub.isCompressed()) {
			return this;
		} else {
			return new ECKey(this.provider, this.privKey, decompressPoint(pub));
		}
	}

	/**
	 * @deprecated per-point compression property will be removed in Bouncy
	 *             Castle
	 */
	public ECKey compress() {
		if (pub.isCompressed()) {
            return this;
        }else {
            return new ECKey(this.provider, this.privKey, compressPoint(pub));
        }
	}

	/**
	 * Returns true if this key doesn't have access to private key bytes. This
	 * may be because it was never given any private key bytes to begin with (a
	 * watching key).
	 *
	 * @return -
	 */
	public boolean isPubKeyOnly() {
		return privKey == null;
	}

	/**
	 * Returns true if this key has access to private key bytes. Does the
	 * opposite of {@link #isPubKeyOnly()}.
	 *
	 * @return -
	 */
	public boolean hasPrivKey() {
		return privKey != null;
	}

	/**
	 * Returns public key bytes from the given private key. To convert a byte
	 * array into a BigInteger, use <tt>
	 * new BigInteger(1, bytes);</tt>
	 *
	 * @param privKey
	 *            -
	 * @param compressed
	 *            -
	 * @return -
	 */
	public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
		ECPoint point = CURVE.getG().multiply(privKey);
		return point.getEncoded(compressed);
	}


	/**
	 * Compute the encoded X, Y coordinates of a public point.
	 *
	 * This is the encoded public key without the leading byte.
	 *
	 * @param pubPoint
	 *            a public point
	 * @return 64-byte X,Y point pair
	 */
	public static byte[] pubBytesWithoutFormat(ECPoint pubPoint) {
		final byte[] pubBytes = pubPoint.getEncoded(/* uncompressed */false);
		return Arrays.copyOfRange(pubBytes, 1, pubBytes.length);
	}

	/**
	 * Generates the NodeID based on this key, that is the public key without
	 * first format byte
	 */
	public byte[] getNodeId() {
		if (nodeId == null) {
			nodeId = pubBytesWithoutFormat(this.pub);
		}
		return nodeId;
	}

	/**
	 * Recover the public key from an encoded node id.
	 *
	 * @param nodeId
	 *            a 64-byte X,Y point pair
	 */
	public static ECKey fromNodeId(byte[] nodeId) {
		check(nodeId.length == 64, "Expected a 64 byte node id");
		byte[] pubBytes = new byte[65];
		System.arraycopy(nodeId, 0, pubBytes, 1, nodeId.length);
		pubBytes[0] = 0x04; // uncompressed
		return ECKey.fromPublicOnly(pubBytes);
	}

	/**
	 * Gets the encoded public key value.
	 *
	 * @return 65-byte encoded public key
	 */
	public byte[] getPubKey() {
		return pub.getEncoded(/* compressed */false);
	}

	/**
	 * Gets the public key in the form of an elliptic curve point object from
	 * Bouncy Castle.
	 *
	 * @return -
	 */
	public ECPoint getPubKeyPoint() {
		return pub;
	}

	/**
	 * Gets the private key in the form of an integer field element. The public
	 * key is derived by performing EC point addition this number of times (i.e.
	 * point multiplying).
	 *
	 *
	 * @return -
	 *
	 * @throws IllegalStateException
	 *             if the private key bytes are not available.
	 */
	public BigInteger getPrivKey() {
		if (privKey == null) {
			throw new MissingPrivateKeyException();
		} else if (privKey instanceof BCECPrivateKey) {
			return ((BCECPrivateKey) privKey).getD();
		} else {
			throw new MissingPrivateKeyException();
		}
	}

	/**
	 * Returns whether this key is using the compressed form or not. Compressed
	 * pubkeys are only 33 bytes, not 64.
	 *
	 *
	 * @return -
	 */
	public boolean isCompressed() {
		return pub.isCompressed();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("pub:").append(Hex.toHexString(pub.getEncoded(false)));
		return b.toString();
	}

	/**
	 * Produce a string rendering of the ECKey INCLUDING the private key. Unless
	 * you absolutely need the private key it is better for security reasons to
	 * just use toString().
	 *
	 *
	 * @return -
	 */
	public String toStringWithPrivate() {
		StringBuilder b = new StringBuilder();
		b.append(toString());
		if (privKey != null && privKey instanceof BCECPrivateKey) {
			b.append(" priv:").append(Hex.toHexString(((BCECPrivateKey) privKey).getD().toByteArray()));
		}
		return b.toString();
	}

	/**
	 * Signs the given hash and returns the R and S components as BigIntegers
	 * and put them in ECDSASignature
	 *
	 * @param input
	 *            to sign
	 * @return ECDSASignature signature that contains the R and S components
	 */
	public ECDSASignature doSign(byte[] input) {
		if (input.length != 32) {
			throw new IllegalArgumentException("Expected 32 byte input to ECDSA signature, not "
					+ input.length);
		}
		// No decryption of private key required.
		if (privKey == null) {
            throw new MissingPrivateKeyException();
        }
		if (privKey instanceof BCECPrivateKey) {
			ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
			ECPrivateKeyParameters privKeyParams = new ECPrivateKeyParameters(((BCECPrivateKey) privKey)
					.getD(), CURVE);
			signer.init(true, privKeyParams);
			BigInteger[] components = signer.generateSignature(input);
			return new ECDSASignature(components[0], components[1]).toCanonicalised();
		} else {
			try {
				final Signature ecSig = ECSignatureFactory.getRawInstance(provider);
				ecSig.initSign(privKey);
				ecSig.update(input);
				final byte[] derSignature = ecSig.sign();
				return ECDSASignature.decodeFromDER(derSignature).toCanonicalised();
			} catch (SignatureException | InvalidKeyException ex) {
				throw new RuntimeException("ECKey signing error", ex);
			}
		}
	}

	/**
	 * Takes the keccak hash (32 bytes) of data and returns the ECDSA signature
	 *
	 * @param messageHash
	 *            -
	 * @return -
	 * @throws IllegalStateException
	 *             if this ECKey does not have the private part.
	 */
	public ECDSASignature sign(byte[] messageHash) {
		ECDSASignature sig = doSign(messageHash);
		// Now we have to work backwards to figure out the recId needed to
		// recover the signature.
		int recId = -1;
		byte[] thisKey = this.pub.getEncoded(/* compressed */false);
		for (int i = 0; i < 4; i++) {
			byte[] k = ECKey.recoverPubBytesFromSignature(i, sig, messageHash);
			if (k != null && Arrays.equals(k, thisKey)) {
				recId = i;
				break;
			}
		}
		if (recId == -1) {
			throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
		}
		sig.v = (byte) (recId + 27);
		return sig;
	}

	/**
	 * Given a piece of text and a message signature encoded in base64, returns
	 * an ECKey containing the public key that was used to sign it. This can
	 * then be compared to the expected public key to determine if the signature
	 * was correct.
	 *
	 * @param messageHash
	 *            a piece of human readable text that was signed
	 * @param signatureBase64
	 *            The Ethereum-format message signature in base64
	 *
	 * @return -
	 * @throws SignatureException
	 *             If the public key could not be recovered or if there was a
	 *             signature format error.
	 */
	public static byte[] signatureToKeyBytes(byte[] messageHash, String signatureBase64)
			throws SignatureException {
		byte[] signatureEncoded;
		try {
			signatureEncoded = Base64.decode(signatureBase64);
		} catch (RuntimeException e) {
			// This is what you get back from Bouncy Castle if base64 doesn't
			// decode :(
			throw new SignatureException("Could not decode base64", e);
		}
		// Parse the signature bytes into r/s and the selector value.
		if (signatureEncoded.length < 65) {
            throw new SignatureException("Signature truncated, expected 65 bytes and got "
                    + signatureEncoded.length);
        }

		return signatureToKeyBytes(messageHash, ECDSASignature.fromComponents(Arrays.copyOfRange(
				signatureEncoded, 1, 33), Arrays.copyOfRange(signatureEncoded, 33, 65),
				(byte) (signatureEncoded[0] & 0xFF)));
	}

	public static byte[] signatureToKeyBytes(byte[] messageHash, ECDSASignature sig)
			throws SignatureException {
		check(messageHash.length == 32, "messageHash argument has length " + messageHash.length);
		int header = sig.v;
		// The header byte: 0x1B = first key with even y, 0x1C = first key with
		// odd y,
		// 0x1D = second key with even y, 0x1E = second key with odd y
		if (header < 27 || header > 34) {
			throw new SignatureException("Header byte out of range: " + header);
		}
		if (header >= 31) {
			header -= 4;
		}
		int recId = header - 27;
		byte[] key = ECKey.recoverPubBytesFromSignature(recId, sig, messageHash);
		if (key == null) {
			throw new SignatureException("Could not recover public key from signature");
		}
		return key;
	}


	/**
	 * Compute the key that signed the given signature.
	 *
	 * @param messageHash
	 *            32-byte hash of message
	 * @param signatureBase64
	 *            Base-64 encoded signature
	 * @return ECKey
	 */
	public static ECKey signatureToKey(byte[] messageHash, String signatureBase64) throws SignatureException {
		final byte[] keyBytes = signatureToKeyBytes(messageHash, signatureBase64);
		return ECKey.fromPublicOnly(keyBytes);
	}

	/**
	 * Compute the key that signed the given signature.
	 *
	 * @param messageHash
	 *            32-byte hash of message
	 * @param sig
	 *            -
	 * @return ECKey
	 */
	public static ECKey signatureToKey(byte[] messageHash, ECDSASignature sig) throws SignatureException {
		final byte[] keyBytes = signatureToKeyBytes(messageHash, sig);
		return ECKey.fromPublicOnly(keyBytes);
	}

	public BigInteger keyAgreement(ECPoint otherParty) {
		if (privKey == null) {
			throw new MissingPrivateKeyException();
		} else if (privKey instanceof BCECPrivateKey) {
			final ECDHBasicAgreement agreement = new ECDHBasicAgreement();
			agreement.init(new ECPrivateKeyParameters(((BCECPrivateKey) privKey).getD(), CURVE));
			return agreement.calculateAgreement(new ECPublicKeyParameters(otherParty, CURVE));
		} else {
			try {
				final KeyAgreement agreement = ECKeyAgreement.getInstance(this.provider);
				agreement.init(this.privKey);
				agreement.doPhase(ECKeyFactory.getInstance(this.provider).generatePublic(
						new ECPublicKeySpec(otherParty, CURVE_SPEC)),
				/* lastPhase */true);
				return new BigInteger(1, agreement.generateSecret());
			} catch (IllegalStateException | InvalidKeyException | InvalidKeySpecException ex) {
				throw new RuntimeException("ECDH key agreement failure", ex);
			}
		}
	}

	/**
	 * Decrypt cipher by AES in SIC(also know as CTR) mode
	 *
	 * @param cipher
	 *            -proper cipher
	 * @return decrypted cipher, equal length to the cipher.
	 * @deprecated should not use EC private scalar value as an AES key
	 */
	public byte[] decryptAES(byte[] cipher) {

		if (privKey == null) {
			throw new MissingPrivateKeyException();
		}
		if (!(privKey instanceof BCECPrivateKey)) {
			throw new UnsupportedOperationException("Cannot use the private key as an AES key");
		}

		AESEngine engine = new AESEngine();
		SICBlockCipher ctrEngine = new SICBlockCipher(engine);

		KeyParameter key = new KeyParameter(BigIntegers
				.asUnsignedByteArray(((BCECPrivateKey) privKey).getD()));
		ParametersWithIV params = new ParametersWithIV(key, new byte[16]);

		ctrEngine.init(false, params);

		int i = 0;
		byte[] out = new byte[cipher.length];
		while (i < cipher.length) {
			ctrEngine.processBlock(cipher, i, out, i);
			i += engine.getBlockSize();
			if (cipher.length - i < engine.getBlockSize()) {
                break;
            }
		}

		// process left bytes
		if (cipher.length - i > 0) {
			byte[] tmpBlock = new byte[16];
			System.arraycopy(cipher, i, tmpBlock, 0, cipher.length - i);
			ctrEngine.processBlock(tmpBlock, 0, tmpBlock, 0);
			System.arraycopy(tmpBlock, 0, out, i, cipher.length - i);
		}

		return out;
	}

	/**
	 * <p>
	 * Verifies the given ECDSA signature against the message bytes using the
	 * public key bytes.
	 * </p>
	 *
	 * <p>
	 * When using native ECDSA verification, data must be 32 bytes, and no
	 * element may be larger than 520 bytes.
	 * </p>
	 *
	 * @param data
	 *            Hash of the data to verify.
	 * @param signature
	 *            signature.
	 * @param pub
	 *            The public key bytes to use.
	 *
	 * @return -
	 */
	public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
		ECDSASigner signer = new ECDSASigner();
		ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
		signer.init(false, params);
		try {
			return signer.verifySignature(data, signature.r, signature.s);
		} catch (NullPointerException npe) {
			// Bouncy Castle contains a bug that can cause NPEs given specially
			// crafted signatures.
			// Those signatures are inherently invalid/attack sigs so we just
			// fail them here rather than crash the thread.
			logger.error("Caught NPE inside bouncy castle", npe);
			return false;
		}
	}

	/**
	 * Verifies the given ASN.1 encoded ECDSA signature against a hash using the
	 * public key.
	 *
	 * @param data
	 *            Hash of the data to verify.
	 * @param signature
	 *            signature.
	 * @param pub
	 *            The public key bytes to use.
	 *
	 * @return -
	 */
	public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
		return verify(data, ECDSASignature.decodeFromDER(signature), pub);
	}

	/**
	 * Verifies the given ASN.1 encoded ECDSA signature against a hash using the
	 * public key.
	 *
	 * @param data
	 *            Hash of the data to verify.
	 * @param signature
	 *            signature.
	 *
	 * @return -
	 */
	public boolean verify(byte[] data, byte[] signature) {
		return ECKey.verify(data, signature, getPubKey());
	}

	/**
	 * Verifies the given R/S pair (signature) against a hash using the public
	 * key.
	 *
	 * @param sigHash
	 *            -
	 * @param signature
	 *            -
	 * @return -
	 */
	public boolean verify(byte[] sigHash, ECDSASignature signature) {
		return ECKey.verify(sigHash, signature, getPubKey());
	}

	/**
	 * Returns true if this pubkey is canonical, i.e. the correct length taking
	 * into account compression.
	 *
	 * @return -
	 */
	public boolean isPubKeyCanonical() {
		return isPubKeyCanonical(pub.getEncoded(/* uncompressed */false));
	}

	/**
	 * Returns true if the given pubkey is canonical, i.e. the correct length
	 * taking into account compression.
	 * 
	 * @param pubkey
	 *            -
	 * @return -
	 */
	public static boolean isPubKeyCanonical(byte[] pubkey) {
		if (pubkey[0] == 0x04) {
			// Uncompressed pubkey
			if (pubkey.length != 65) {
                return false;
            }
		} else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
			// Compressed pubkey
			if (pubkey.length != 33) {
                return false;
            }
		} else {
            return false;
        }
		return true;
	}

	/**
	 * <p>
	 * Given the components of a signature and a selector value, recover and
	 * return the public key that generated the signature according to the
	 * algorithm in SEC1v2 section 4.1.6.
	 * </p>
	 *
	 * <p>
	 * The recId is an index from 0 to 3 which indicates which of the 4 possible
	 * keys is the correct one. Because the key recovery operation yields
	 * multiple potential keys, the correct key must either be stored alongside
	 * the signature, or you must be willing to try each recId in turn until you
	 * find one that outputs the key you are expecting.
	 * </p>
	 *
	 * <p>
	 * If this method returns null it means recovery was not possible and recId
	 * should be iterated.
	 * </p>
	 *
	 * <p>
	 * Given the above two points, a correct usage of this method is inside a
	 * for loop from 0 to 3, and if the output is null OR a key that is not the
	 * one you expect, you try again with the next recId.
	 * </p>
	 *
	 * @param recId
	 *            Which possible key to recover.
	 * @param sig
	 *            the R and S components of the signature, wrapped.
	 * @param messageHash
	 *            Hash of the data that was signed.
	 * @return 65-byte encoded public key
	 */
	@Nullable
	public static byte[] recoverPubBytesFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
		check(recId >= 0, "recId must be positive");
		check(sig.r.signum() >= 0, "r must be positive");
		check(sig.s.signum() >= 0, "s must be positive");
		check(messageHash != null, "messageHash must not be null");
		// 1.0 For j from 0 to h (h == recId here and the loop is outside this
		// function)
		// 1.1 Let x = r + jn
		BigInteger n = CURVE.getN(); // Curve order.
		BigInteger i = BigInteger.valueOf((long) recId / 2);
		BigInteger x = sig.r.add(i.multiply(n));
		// 1.2. Convert the integer x to an octet string X of length mlen using
		// the conversion routine
		// specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen =
		// ⌈m/8⌉.
		// 1.3. Convert the octet string (16 set binary digits)||X to an
		// elliptic curve point R using the
		// conversion routine specified in Section 2.3.4. If this conversion
		// routine outputs “invalid”, then
		// do another iteration of Step 1.
		//
		// More concisely, what these points mean is to use X as a compressed
		// public key.
		ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
		BigInteger prime = curve.getQ(); // Bouncy Castle is not consistent
											// about the letter it uses for the
											// prime.
		if (x.compareTo(prime) >= 0) {
			// Cannot have point co-ordinates larger than this as everything
			// takes place modulo Q.
			return null;
		}
		// Compressed keys require you to know an extra bit of data about the
		// y-coord as there are two possibilities.
		// So it's encoded in the recId.
		ECPoint R = decompressKey(x, (recId & 1) == 1);
		// 1.4. If nR != point at infinity, then do another iteration of Step 1
		// (callers responsibility).
		if (!R.multiply(n).isInfinity()) {
            return null;
        }
		// 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature
		// verification.
		BigInteger e = new BigInteger(1, messageHash);
		// 1.6. For k from 1 to 2 do the following. (loop is outside this
		// function via iterating recId)
		// 1.6.1. Compute a candidate public key as:
		// Q = mi(r) * (sR - eG)
		//
		// Where mi(x) is the modular multiplicative inverse. We transform this
		// into the following:
		// Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
		// Where -e is the modular additive inverse of e, that is z such that z
		// + e = 0 (mod n). In the above equation
		// ** is point multiplication and + is point addition (the EC group
		// operator).
		//
		// We can find the additive inverse by subtracting e from zero then
		// taking the mod. For example the additive
		// inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 =
		// 8.
		BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
		BigInteger rInv = sig.r.modInverse(n);
		BigInteger srInv = rInv.multiply(sig.s).mod(n);
		BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
		ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
		return q.getEncoded(/* compressed */false);
	}

	/**
	 *
	 * @param recId
	 *            Which possible key to recover.
	 * @param sig
	 *            the R and S components of the signature, wrapped.
	 * @param messageHash
	 *            Hash of the data that was signed.
	 * @return ECKey
	 */
	@Nullable
	public static ECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
		final byte[] pubBytes = recoverPubBytesFromSignature(recId, sig, messageHash);
		if (pubBytes == null) {
			return null;
		} else {
			return ECKey.fromPublicOnly(pubBytes);
		}
	}

	/**
	 * Decompress a compressed public key (x co-ord and low-bit of y-coord).
	 *
	 * @param xBN
	 *            -
	 * @param yBit
	 *            -
	 * @return -
	 */
	private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
		X9IntegerConverter x9 = new X9IntegerConverter();
		byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
		compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
		return CURVE.getCurve().decodePoint(compEnc);
	}

	/**
	 * Returns a 32 byte array containing the private key, or null if the key is
	 * encrypted or public only
	 *
	 * @return -
	 */
	@Nullable
	public byte[] getPrivKeyBytes() {
		if (privKey == null) {
			return null;
		} else if (privKey instanceof BCECPrivateKey) {
			return ByteUtil.bigIntegerToBytes(((BCECPrivateKey) privKey).getD(), 32);
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (o == null || !(o instanceof ECKey)) {
            return false;
        }

		ECKey ecKey = (ECKey) o;

		if (privKey != null && !privKey.equals(ecKey.privKey)) {
            return false;
        }
		if (pub != null && !pub.equals(ecKey.pub)) {
            return false;
        }

		return true;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getPubKey());
	}

	@SuppressWarnings("serial")
	public static class MissingPrivateKeyException extends RuntimeException {
	}

	private static void check(boolean test, String message) {
		if (!test) {
            throw new IllegalArgumentException(message);
        }
	}

	/**
     * This is borrowed from ethereumJ
     */
    public static final class ECAlgorithmParameters {

      public static final String ALGORITHM = "EC";
      public static final String CURVE_NAME = "secp256k1";

      private ECAlgorithmParameters() { }

      private static class Holder {
        private static final AlgorithmParameters INSTANCE;

        private static final ECGenParameterSpec SECP256K1_CURVE
            = new ECGenParameterSpec(CURVE_NAME);

        static {
          try {
            INSTANCE = AlgorithmParameters.getInstance(ALGORITHM);
            INSTANCE.init(SECP256K1_CURVE);
          } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(
                "Assumed the JRE supports EC algorithm params", ex);
          } catch (InvalidParameterSpecException ex) {
            throw new AssertionError(
                "Assumed correct key spec statically", ex);
          }
        }
      }

      public static java.security.spec.ECParameterSpec getParameterSpec() {
        try {
          return Holder.INSTANCE.getParameterSpec(java.security.spec.ECParameterSpec.class);
        } catch (InvalidParameterSpecException ex) {
          throw new AssertionError(
              "Assumed correct key spec statically", ex);
        }
      }

      public static byte[] getASN1Encoding() {
        try {
          return Holder.INSTANCE.getEncoded();
        } catch (IOException ex) {
          throw new AssertionError(
              "Assumed algo params has been initialized", ex);
        }
      }
    }

	/**
     * This is borrowed from ethereumJ
     */
    public static final class ECKeyAgreement {

      public static final String ALGORITHM = "ECDH";

      private static final String algorithmAssertionMsg =
          "Assumed the JRE supports EC key agreement";

      private ECKeyAgreement() { }

      public static KeyAgreement getInstance() {
        try {
          return KeyAgreement.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        }
      }

      public static KeyAgreement getInstance(final String provider) throws NoSuchProviderException {
        try {
          return KeyAgreement.getInstance(ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        }
      }

      public static KeyAgreement getInstance(final Provider provider) {
        try {
          return KeyAgreement.getInstance(ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        }
      }
    }

	/**
     * This is borrowed from ethereumJ
     */
    public static final class ECKeyFactory {

      public static final String ALGORITHM = "EC";

      private static final String algorithmAssertionMsg =
          "Assumed the JRE supports EC key factories";

      private ECKeyFactory() { }

      private static class Holder {
        private static final KeyFactory INSTANCE;

        static {
          try {
            INSTANCE = KeyFactory.getInstance(ALGORITHM);
          } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(algorithmAssertionMsg, ex);
          }
        }
      }

      public static KeyFactory getInstance() {
        return Holder.INSTANCE;
      }

      public static KeyFactory getInstance(final String provider) throws NoSuchProviderException {
        try {
          return KeyFactory.getInstance(ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        }
      }

      public static KeyFactory getInstance(final Provider provider) {
        try {
          return KeyFactory.getInstance(ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        }
      }
    }

	/**
     * This is borrowed from ethereumJ
     */
    public static final class ECKeyPairGenerator {

      public static final String ALGORITHM = "EC";
      public static final String CURVE_NAME = "secp256k1";

      private static final String algorithmAssertionMsg =
          "Assumed JRE supports EC key pair generation";

      private static final String keySpecAssertionMsg =
          "Assumed correct key spec statically";

      private static final ECGenParameterSpec SECP256K1_CURVE
          = new ECGenParameterSpec(CURVE_NAME);

      private ECKeyPairGenerator() { }

      private static class Holder {
        private static final KeyPairGenerator INSTANCE;

        static {
          try {
            INSTANCE = KeyPairGenerator.getInstance(ALGORITHM);
            INSTANCE.initialize(SECP256K1_CURVE);
          } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(algorithmAssertionMsg, ex);
          } catch (InvalidAlgorithmParameterException ex) {
            throw new AssertionError(keySpecAssertionMsg, ex);
          }
        }
      }

      public static KeyPair generateKeyPair() {
        return Holder.INSTANCE.generateKeyPair();
      }

      public static KeyPairGenerator getInstance(final String provider, final SecureRandom random) throws NoSuchProviderException {
        try {
          final KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, provider);
          gen.initialize(SECP256K1_CURVE, random);
          return gen;
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        } catch (InvalidAlgorithmParameterException ex) {
          throw new AssertionError(keySpecAssertionMsg, ex);
        }
      }

      public static KeyPairGenerator getInstance(final Provider provider, final SecureRandom random) {
        try {
          final KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM, provider);
          gen.initialize(SECP256K1_CURVE, random);
          return gen;
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(algorithmAssertionMsg, ex);
        } catch (InvalidAlgorithmParameterException ex) {
          throw new AssertionError(keySpecAssertionMsg, ex);
        }
      }
    }

	/**
     * This is borrowed from ethereumJ
     */
    public static final class ECSignatureFactory {

      public static final String RAW_ALGORITHM = "NONEwithECDSA";

      private static final String rawAlgorithmAssertionMsg =
          "Assumed the JRE supports NONEwithECDSA signatures";

      private ECSignatureFactory() { }

      public static Signature getRawInstance() {
        try {
          return Signature.getInstance(RAW_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(rawAlgorithmAssertionMsg, ex);
        }
      }

      public static Signature getRawInstance(final String provider) throws NoSuchProviderException {
        try {
          return Signature.getInstance(RAW_ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(rawAlgorithmAssertionMsg, ex);
        }
      }

      public static Signature getRawInstance(final Provider provider) {
        try {
          return Signature.getInstance(RAW_ALGORITHM, provider);
        } catch (NoSuchAlgorithmException ex) {
          throw new AssertionError(rawAlgorithmAssertionMsg, ex);
        }
      }
    }

	public static final class SpongyCastleProvider {

      private static class Holder {
        private static final Provider INSTANCE;
        static{
            Provider p = Security.getProvider("SC");

            INSTANCE = (p != null) ? p : new BouncyCastleProvider();

            INSTANCE.put("MessageDigest.ETH-KECCAK-256", "com.medici.firestar.crypto.cryptohash.Keccak256");
            INSTANCE.put("MessageDigest.ETH-KECCAK-512", "com.medici.firestar.crypto.cryptohash.Keccak512");
        }
      }

      public static Provider getInstance() {
        return Holder.INSTANCE;
      }
    }
}
