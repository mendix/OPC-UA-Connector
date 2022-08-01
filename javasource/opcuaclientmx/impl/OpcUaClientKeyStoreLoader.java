package opcuaclientmx.impl;
import java.io.File;
/*JV May/June 2020
 * 
 * FOR TEST PURPOSES ONLY!
 * 
 * */
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;

import opcuaclientmx.proxies.constants.Constants;

public class OpcUaClientKeyStoreLoader {
	
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String CLIENT_ALIAS = "client-ai";
    private static final char[] PASSWORD = "passwordTest".toCharArray();

    private static final ILogNode logger = Core.getLogger("OpcUA");

    private X509Certificate[] clientCertificateChain;
    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;
  

    OpcUaClientKeyStoreLoader load() throws CoreException {
    	try {
	        KeyStore keyStore = KeyStore.getInstance("PKCS12");

	        Path baseDir = Paths.get( Core.getConfiguration().getResourcesPath().getAbsolutePath(), "opc-certs");
	        (new File(baseDir.toString())).mkdirs();
	        
	        Path serverKeyStore = baseDir.resolve("local-client.pfx");
	
	        logger.info("Loading KeyStore at " + serverKeyStore);
	
	        if (!Files.exists(serverKeyStore)) {
	            keyStore.load(null, PASSWORD);
	
	            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
	
	            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
	                .setCommonName(Constants.getUA_ApplicationName())
	                .setOrganization("Mendix")
//	                .setOrganizationalUnit("dev")
//	                .setLocalityName("Rotterdam")
//	                .setStateName("Zuid-Holland")
//	                .setCountryCode("NL")
					.setApplicationUri(Constants.getUA_ApplicationURI())
	                .addDnsName(Core.getConfiguration().getApplicationRootUrl());
//	                .addIpAddress("127.0.0.1");
	
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
	            try (OutputStream out = Files.newOutputStream(serverKeyStore)) {
	                keyStore.store(out, PASSWORD);
	            }
	        } else {
	            try (InputStream in = Files.newInputStream(serverKeyStore)) {
	                keyStore.load(in, PASSWORD);
	            }
	        }

	        Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
	        if (clientPrivateKey instanceof PrivateKey) {
	            this.clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);

	            this.clientCertificateChain = Arrays.stream(keyStore.getCertificateChain(CLIENT_ALIAS))
	                .map(X509Certificate.class::cast)
	                .toArray(X509Certificate[]::new);

	            PublicKey serverPublicKey = this.clientCertificate.getPublicKey();
	            this.clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) clientPrivateKey);
	        }
	        return this;
    	}catch (Exception e) {
			throw new CoreException("Unable to load certificates", e);
		}
    }

    X509Certificate getClientCertificate() {
        return this.clientCertificate;
    }

    KeyPair getClientKeyPair() {
        return this.clientKeyPair;
    }

    public X509Certificate[] getClientCertificateChain() {
        return clientCertificateChain;
    }
}



