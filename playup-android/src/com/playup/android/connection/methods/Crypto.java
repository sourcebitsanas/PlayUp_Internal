package com.playup.android.connection.methods;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Base64;
import com.playup.android.util.Constants;
import com.playup.android.util.Logs;

public class Crypto {
	
	private final static String HEX = "0123456789ABCDEF";

	public static String encrypt64(String secret, final String text)
			throws Exception {
		byte[] encrypted = encrypt(secret, text);
		return Base64.encodeBytes(encrypted);
	}

	public String getAPIPath(String url) {

		try {
			URI mUri = null;
			if (url != null)
				mUri = new URI(url);

			if (mUri != null)
				return mUri.getPath();

		} catch (URISyntaxException e) {
			Logs.show(e);
		}

		return "";
	}

	public String createSignature(String token, String url) {
		try {

			String path = getAPIPath(url);
			// final String text = new Random().nextInt(32) + ";" + token + ";"
			// + path;
			final String text = 10 + ";" + token + ";" + path;
			String signature = encrypt64(Constants.PLAYUP_API_SECRET, text);
			return signature;
		} catch (Exception e) {
			return "COULD NOT CREATE SIGNATURE";
		}
	}


	
 
 /**
  * Praveen : 
  * Convert received byte array to hex format
  * @param input
  * @return result hex string 
  */
 public  String hexEncode(byte[] input)
 {
     if (input == null || input.length == 0)
     {
         return "";
     }

     int inputLength = input.length;
     StringBuilder output = new StringBuilder(inputLength * 2);

     for (int i = 0; i < inputLength; i++)
     {
         int next = input[i] & 0xff;
         if (next < 0x10)
         {
             output.append("0");
         }

         output.append(Integer.toHexString(next));
     }

     return output.toString();
 }
	
 /**
  * Praveen:
  * Create MD5 hash using received secretToken
  * @param secretToken
  * @return digest : created MD5 hash / null
  */
 public String createMD5Hash(String secretToken) {
		// TODO Auto-generated method stub
 	byte[] digest = null ;
 	try {
			
 		
 			byte[] bytesOfMessage = secretToken.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			digest = md.digest(bytesOfMessage);
			
			return hexEncode(digest);
		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}
 
 
 /**
  * Praveen:
  * Create SHA256 hash using received secretToken
  * @param secretToken
  * @return byteData : created SHA256 hash / null
  */
 public  byte[] createSHA256Hash(String secretToken) {
		// TODO Auto-generated method stub
	byte byteData[] = null;
 	try {
			String password = secretToken;
			 
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			md.update(password.getBytes("UTF-8"));

			byteData = md.digest();
			
			return byteData;
		
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e ){
			e.printStackTrace();
		}
	
		return byteData;
		
		
	}
 
 /**
  * Praveen: 
  * AES 256 CBC Algorithm for token replacement
  * @param vProviderToken
  * @param vMacSecretToken
  * @return final String value / null
  */
 public String createAES256CBCString(String vProviderValue,String vMacSecretToken){
	 
	 String finalString = null;
	 byte[] messageInBytes = null;
	try {
		 // initialize the cipher for encrypt mode
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
		//decode bnase64  of provider token
		messageInBytes = com.playup.android.util.Base64.decode(vProviderValue);
			
		//get Sha256 hash of mac_secret Token 
		byte[] SHA256codedString = 	createSHA256Hash(vMacSecretToken);
		System.out.println("SHA256codedString--------------->>>>"+SHA256codedString);
		System.out.println("SHA256codedString.length--------------->>>>"+SHA256codedString.length);
		    
		//get MD5 hash of mac_secret Token
		String MD5codedString 	 =  createMD5Hash(vMacSecretToken);
		byte[] MD5codedBytes = null;
		try {
				MD5codedBytes = MD5codedString.substring(0, 16).getBytes("UTF-8");
				  
		} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				Logs.show(e1);
		}
		System.out.println("MD5codedString.len()--------------->>>>"+MD5codedString.length());
		System.out.println("MD5codedBytes.len()--------------->>>>"+MD5codedBytes.length+"\n");
		    
		
		//Create SecretKey and Initialization vector for AES 256 CBC
		SecretKeySpec skeySpec = new SecretKeySpec(SHA256codedString,0,32, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(MD5codedBytes);
		    
		try {
				// reinitialize the cipher for decryption
						cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
						
				// decrypt the message
						byte[] decrypted = cipher.doFinal(messageInBytes);
						
						System.out.println("Plaintext::decrypted::::::::::::::::::::::: " + decrypted + "\n");
						System.out.println("Plaintext::String:::::::::::::::::::::::::: " + new String(decrypted) + "\n");
						System.out.println("Base64.encodeBase64String(decrypted):::::::::::::::::::::::::: " + com.playup.android.util.Base64.encodeBytes(decrypted) );
						
						//do BAse65 encode and then URL encoding of the receieved value
						finalString = URLEncoder.encode(  com.playup.android.util.Base64.encodeBytes(decrypted));
						System.out.println(" Base64.encodeBase64String(decrypted)+URL encoded:::::::::::::::::::::::::: "+ finalString);
						
						return finalString;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	} catch (NoSuchAlgorithmException e) {
		Logs.show(e);
	} catch (NoSuchPaddingException e) {
		Logs.show(e);
	} catch (IOException e) {
		Logs.show(e);
	}catch(Exception e){
		Logs.show(e);
	}
	catch(Error e){
		Logs.show(e);
	}
	return finalString;
 }
 
 
	


	
	public static byte[] encrypt(String secret, String text) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(),
				"AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Create an 8-byte initialization vector
		byte[] iv = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
		byte[] encrypted = cipher.doFinal((text).getBytes());

		return encrypted;
	}

	public  String encryptCredentials(String cleartext) throws Exception {
		
		setPlayUpKey_For_Credentials();
		//byte[] rawKey = Constants.PLAYUP_API_SECRET.getBytes();
		byte[] rawKey = Constants.PlayUpKey_For_Credentials.getBytes();

		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public  String decryptCredentials(String encrypted) throws Exception {

		setPlayUpKey_For_Credentials();
		//byte[] rawKey = Constants.PLAYUP_API_SECRET.getBytes();
		byte[] rawKey = Constants.PlayUpKey_For_Credentials.getBytes();
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
		
	}
	
	public void setPlayUpKey_For_Credentials(){
	
		
		if(Constants.PlayUpKey_For_Credentials == null || Constants.PlayUpKey_For_Credentials.trim().length()	==	0){
			
			TelephonyManager tManager = (TelephonyManager)PlayUpActivity.context.getSystemService(Context.TELEPHONY_SERVICE);
			String DEVICE_ID = tManager.getDeviceId();
			

			if(DEVICE_ID != null && DEVICE_ID.trim().length() > 0){
				
	
			Constants.PlayUpKey_For_Credentials	=	createMD5Hash(DEVICE_ID);
		
			
			}else{
				
			
				Constants.PlayUpKey_For_Credentials	=	createMD5Hash(Constants.PLAYUP_API_SECRET);
			
				
				}
			
			
		}
	}

	private  byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private  byte[] decrypt(byte[] raw, byte[] encrypted)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public  byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
					16).byteValue();
		return result;
	}

	public  String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {

			result.append(HEX.charAt((buf[i] >> 4) & 0x0f));
			result.append(HEX.charAt(buf[i] & 0x0f));
		}
		return result.toString();
	}

}
