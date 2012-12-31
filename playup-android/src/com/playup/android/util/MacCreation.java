package com.playup.android.util;



import java.net.URI;
import java.net.URISyntaxException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;





public class MacCreation {
	final static String HMAC_SHA1_ALGORITHM = "HMACSHA256";
	
	String MAC_ID = null;
		
		String MAC_Secret = null;
		
		String url = null;

		private String methodType = "";
		
		private boolean isHttpsRequest = false;
		
		private int iUriPortNumber 	;
		private String vHostBasePath	= "";
	
	public MacCreation(String vId,String vSecret,String vUrl, String METHOD_TYPE, boolean isHttps){
		
		MAC_ID = vId; 
		MAC_Secret = vSecret;
		url = vUrl;
		methodType  = METHOD_TYPE;
	
		isHttpsRequest= isHttps;
	}
		

	
	public String getAPIPath(){
		
		
		try {
			URI mUri	=	null;
			String fullURL	= "";
			if(url!=null)
			{
				
				mUri	=	new URI(url);
				iUriPortNumber	= mUri.getPort();
				vHostBasePath 	 = mUri.getHost();
			}
				
			if(mUri!=null)
				{ 
					fullURL = mUri.getRawPath().toString();
					if(mUri.getQuery()!=null){
						 fullURL = mUri.getRawPath().toString().concat("?").concat(mUri.getRawQuery().toString());
					}
				
			
				}
				
			return fullURL;
			
		} catch (URISyntaxException e) {
			//Logs.show ( e );
		}
		
	
		return "";
	}

	
	


	
	public String getMacTokens(){
		String macId = null;
		try {
				String hmac = null;
				
				Long timeStamp =  System.currentTimeMillis();
				
				
				
		/*		String nounce  = UUID.randomUUID().toString();*/
				String nounce  = getSecureRandomValue();
				
			
				String apiPath = getAPIPath();
				
			
				
				
				
				String eol = System.getProperty("line.separator");
				
				
				
				String normalizedString = timeStamp.toString()+eol+nounce+eol+methodType+eol+apiPath+eol+vHostBasePath+eol;
			
				if(iUriPortNumber!=-1){
					normalizedString+=iUriPortNumber+eol+""+eol;
				}
				else{
					if(isHttpsRequest){
						normalizedString+="443"+eol+""+eol;
					}else{
						normalizedString+="80"+eol+""+eol;
					}
				}
	
				
				
				

			

				
				
				
				try {
					
					
					
					hmac = calculateRFC2104HMAC(normalizedString, MAC_Secret);
					 
					
					

				
					
					
				
					
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				} catch (SignatureException e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
				
				
				


				
				macId = "MAC id=\""+MAC_ID+"\",ts=\""+timeStamp+"\",nonce=\""+nounce+"\",mac=\""+hmac+"\"";

				



		} catch (Exception e) {
			//Logs.show(e);
		}
			
			return macId;
		}

		
		
		
		
		
		
		
	
	


	private String getSecureRandomValue() {
		// TODO Auto-generated method stub
		String secureValue = "";
		try{
		 
			
			Random ranGen = new SecureRandom();
			byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
			ranGen.nextBytes(aesKey);
			
			
			Integer value = ranGen.nextInt();
			//System.out.println("ranGen----->>>"+ranGen.nextInt());
			//System.out.println("ranGen----->>>"+value);
		//	System.out.println("ranGen----->>>"+ranGen.nextLong());
			
			secureValue = value.toString();
			//System.out.println("secureValue---%%%%%%%%%%%%%%%%%%%%%%%%%%%%%-->>>"+secureValue);
			} catch (Exception e) {
			// Logs.show(e);
			 return(UUID.randomUUID().toString());
		  }
		return secureValue;
	}



	public static String calculateRFC2104HMAC(String data, String key)
	throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		
		
			
		
			
			String encoded64 = null;
			 try {
				 

					SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), HMAC_SHA1_ALGORITHM);
					Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
					mac.init(signingKey);
				
					encoded64 = new String(Base64.encodeBytes(mac.doFinal(data.getBytes("UTF-8"))));
				 

				 
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				//Logs.show(e);
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				//Logs.show(e);
			} 
			 return encoded64;


			
	}
	
	
	
		
	
	
}
