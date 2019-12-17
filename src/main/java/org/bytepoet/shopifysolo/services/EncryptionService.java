package org.bytepoet.shopifysolo.services;



import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;

@Service
public class EncryptionService {

	@Value("${solofy.encryption.transformation}")
	private String transformation;
	
	@Value("${solofy.encryption.key}")
	private String secretKeyValue;
	
	@Value("${solofy.encryption.algorithm}")
	private String algorithm;
	
	
	public void encrypt(InputStream input, OutputStream output) throws Exception {
		SecretKey secretKey = new SecretKeySpec(secretKeyValue.getBytes(), algorithm);
		Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	    byte[] iv = cipher.getIV();
	 
	    try (CipherOutputStream cipherOut = new CipherOutputStream(output, cipher)) {
	    	byte[] bytes = ByteStreams.toByteArray(input);
		    output.write(iv);
		    cipherOut.write(bytes);
	    } catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
	    
	}
	
	public void decrypt(InputStream input, OutputStream output) throws Exception {
		SecretKey secretKey = new SecretKeySpec(secretKeyValue.getBytes(), algorithm);
		Cipher cipher = Cipher.getInstance(transformation);
		byte[] iv = new byte[16];
		input.read(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
 
        try (CipherInputStream cipherIn = new CipherInputStream(input, cipher)) {
	    	byte [] bytes = ByteStreams.toByteArray(cipherIn);
		    output.write(bytes);
	    } catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
	}
	
}
