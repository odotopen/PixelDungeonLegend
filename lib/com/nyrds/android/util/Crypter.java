package com.nyrds.android.util;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class Crypter {
	private String encryptionKey;

	public Crypter(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String encrypt(String plainText) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.ENCRYPT_MODE);
			byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
			return android.util.Base64.encodeToString(encryptedBytes,
					Base64.NO_WRAP|Base64.URL_SAFE);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plainText;
	}

	public String decrypt(String encrypted) {
		Cipher cipher;
		try {
			cipher = getCipher(Cipher.DECRYPT_MODE);
			byte[] plainBytes = cipher.doFinal(Base64.decode(encrypted,
					Base64.NO_WRAP|Base64.URL_SAFE));
			return new String(plainBytes);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encrypted;
	}

	private Cipher getCipher(int cipherMode) throws Exception {		
		String encryptionAlgorithm = "AES";
		
		byte[] salt = {
			    (byte)0x95, (byte)0xaa, (byte)0x21, (byte)0x8c,
			    (byte)0xa9, (byte)0xc8, (byte)0xfe, (byte)0x99
			};
		
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), salt, 1, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		
		Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
		cipher.init(cipherMode, secret);
		return cipher;
	}
}