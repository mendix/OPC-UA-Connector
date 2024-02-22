package opcuaclientmx.impl;

/*JV May/June 2020
 * 
 * FOR TEST PURPOSES ONLY!
 * 
 * */
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.security.KeyPair;
import java.security.KeyStore;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;


import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;


import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import opcuaclientmx.proxies.Certificate;
import opcuaclientmx.proxies.ClientSettings;
import opcuaclientmx.proxies.OpcUaServerCfg;

public class MxClientKeyStoreLoader {
	
     
    private static final ILogNode logger = Core.getLogger("OpcUA");
    private static final char[] PASSWORD = "password".toCharArray();
    private static final String CLIENT_ALIAS = "client-ai";

    private X509Certificate clientCertificate;
    private X509Certificate[] clientCertificateChain; 
    private KeyPair clientKeyPair;
    private X509Certificate serverCertificate;
    private boolean useSelfSignedGeneratedCertificate = false;
  

	MxClientKeyStoreLoader load(Path baseDir, IContext context, OpcUaServerCfg connector) throws CoreException {
    	
    	              
        List <ClientSettings> clientSettingsList = ClientSettings.load(connector.getContext(), "");
        ClientSettings clientSettings = clientSettingsList.get(0);
        if(clientSettings == null) throw new CoreException("Missing clientSettings");
        
        this.useSelfSignedGeneratedCertificate = clientSettings.getUseGenerateSelfSignedCertificates().booleanValue();
        Certificate clientCertificateFileObject = clientSettings.getClientCertificate();
        Certificate privateKeyFileObject =clientSettings.getPrivateKey();
        
        InputStream certificateFileInputStream = Core.getFileDocumentContent(context, clientCertificateFileObject.getMendixObject());
        InputStream privateKeyFileInputStream = Core.getFileDocumentContent(context, privateKeyFileObject.getMendixObject());
              
        try {
        	
        	KeyStore keyStore = KeyStore.getInstance("PKCS12");
        	
	        Path clientKeyStore = baseDir.resolve("temp-client.pfx");

	        if (!Files.exists(clientKeyStore)) {
	        	 keyStore.load(null, PASSWORD); 
	        }
	        else {
	            InputStream in = Files.newInputStream(clientKeyStore);
	            keyStore.load(in, PASSWORD);
	        }
	        
	        // Parse the private key from PEM
	        PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyFileInputStream));        		
	        Object keyObject = pemParser.readObject();
	        PEMKeyPair pemKeyPair = (PEMKeyPair) keyObject;
	        this.clientKeyPair  = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
	
	        // Parse the certificate from PEM or Der
	        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
	        this.clientCertificate = (X509Certificate) certificateFactory.generateCertificate(certificateFileInputStream);
	        
	        Certificate serverCertifcate = connector.getServerCertificate(context);
	        InputStream serverCertificateFileInputStream = Core.getFileDocumentContent(context, serverCertifcate.getMendixObject());      
	        this.serverCertificate = (X509Certificate) certificateFactory.generateCertificate(serverCertificateFileInputStream);
	        
	        keyStore.setKeyEntry(CLIENT_ALIAS, clientKeyPair.getPrivate(), PASSWORD, new X509Certificate[]{clientCertificate});
            try (OutputStream out = Files.newOutputStream(clientKeyStore)) {
                keyStore.store(out, PASSWORD);
            }
            
            this.clientCertificateChain = Arrays.stream(keyStore.getCertificateChain(CLIENT_ALIAS))
	                .map(X509Certificate.class::cast)
	                .toArray(X509Certificate[]::new);
            
        } catch(Exception e) {
        	logger.error(e);
        	throw new CoreException(e);
        } 
    	return this;
    }
    
	X509Certificate getServerCertificate() {
		return serverCertificate;
	}

	X509Certificate[] getClientCertificateChain() {
        return clientCertificateChain;
    }
   
    X509Certificate getClientCertificate() {
        return this.clientCertificate;
    }

    KeyPair getClientKeyPair() {
        return this.clientKeyPair;
    }
    
    boolean UseSelfSignedGeneratedClientCertificates () {
    	return this.useSelfSignedGeneratedCertificate;
    }


}



