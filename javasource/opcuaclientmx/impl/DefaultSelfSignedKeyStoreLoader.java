package opcuaclientmx.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;

import com.mendix.core.CoreException;

class DefaultSelfSignedKeyStoreLoader {

	 private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
		        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	    private static final String CLIENT_ALIAS = "client-ai";
	    private static final char[] PASSWORD = "password".toCharArray();
	    
	    private X509Certificate[] clientCertificateChain;
	    private X509Certificate clientCertificate;
	    private KeyPair clientKeyPair;

	    DefaultSelfSignedKeyStoreLoader load(Path baseDir, String appURI) throws CoreException {
	    	try {
		        KeyStore keyStore = KeyStore.getInstance("PKCS12");
	
		        Path clientKeyStore = baseDir.resolve("example-client.pfx");
	
		        if (!Files.exists(clientKeyStore)) {
		            keyStore.load(null, PASSWORD);
	
		            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
	
		            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
		                .setCommonName("Siemens MX Example Client")
		                .setOrganization("digitalpetri")
		                .setOrganizationalUnit("dev")
		                .setLocalityName("Folsom")
		                .setStateName("CA")
		                .setCountryCode("US")
		                .setApplicationUri(appURI)
		                .addDnsName("localhost")
		                .addIpAddress("127.0.0.1");
	
		            // Get as many hostnames and IP addresses as we can listed in the certificate.
		            for (String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
		                if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
		                    builder.addIpAddress(hostname);
		                } else {
		                    builder.addDnsName(hostname);
		                }
		            }
	
		            X509Certificate certificate = builder.build();
	
		            keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
		            try (OutputStream out = Files.newOutputStream(clientKeyStore)) {
		                keyStore.store(out, PASSWORD);
		            }
		        } else {
		            try (InputStream in = Files.newInputStream(clientKeyStore)) {
		                keyStore.load(in, PASSWORD);
		            }
		        }
	
		        Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
		        if (clientPrivateKey instanceof PrivateKey) {
		            clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);
	
		            clientCertificateChain = Arrays.stream(keyStore.getCertificateChain(CLIENT_ALIAS))
		                .map(X509Certificate.class::cast)
		                .toArray(X509Certificate[]::new);
	
		            PublicKey clientPublicKey = clientCertificate.getPublicKey();
		            clientKeyPair = new KeyPair(clientPublicKey, (PrivateKey) clientPrivateKey);
		        }
	    	}
	        catch (Exception e) {
	        	throw new CoreException(e);
	        }

	        return this;
	    }

	    X509Certificate getClientCertificate() {
	        return clientCertificate;
	    }

	    public X509Certificate[] getClientCertificateChain() {
	        return clientCertificateChain;
	    }

	    KeyPair getClientKeyPair() {
	        return clientKeyPair;
	    }
}
