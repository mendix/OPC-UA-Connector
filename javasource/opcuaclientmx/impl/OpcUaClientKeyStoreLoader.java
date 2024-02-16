package opcuaclientmx.impl;
import java.io.IOException;
/*JV May/June 2020
 * 
 * FOR TEST PURPOSES ONLY!
 * 
 * */
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

public class OpcUaClientKeyStoreLoader {
	
     
    private static final ILogNode logger = Core.getLogger("OpcUA");

    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;
    private X509Certificate serverCertificate;
  

    public X509Certificate getServerCertificate() {
		return serverCertificate;
	}

    
	OpcUaClientKeyStoreLoader load(IContext context, OpcUaServerCfg connector) throws CoreException {
    	
    	              
        List <ClientSettings> clientSettingsList = ClientSettings.load(connector.getContext(), "");
        ClientSettings clientSettings = clientSettingsList.get(0);
        if(clientSettings == null) throw new CoreException("Missing clientSettings");
        
        Certificate clientCertificate = clientSettings.getClientCertificate();
        Certificate privateKey =clientSettings.getPrivateKey();
        
        InputStream certificateFileInputStream = Core.getFileDocumentContent(context, clientCertificate.getMendixObject());
        InputStream privateKeyFileInputStream = Core.getFileDocumentContent(context, privateKey.getMendixObject());
              
        try {
	        // Parse the private key from PEM
	        PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyFileInputStream));        		
	        Object keyObject = pemParser.readObject();
	        PEMKeyPair pemKeyPair = (PEMKeyPair) keyObject;
	        this.clientKeyPair  = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
	
	        // Parse the certificate from PEM
	        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
	        this.clientCertificate = (X509Certificate) certificateFactory.generateCertificate(certificateFileInputStream);
	        
	        
	        Certificate serverCertifcate = connector.getServerCertificate(context);
	        InputStream serverCertificateFileInputStream = Core.getFileDocumentContent(context, serverCertifcate.getMendixObject());      
	        this.serverCertificate = (X509Certificate) certificateFactory.generateCertificate(serverCertificateFileInputStream);
        } catch(IOException e) {
        	logger.error(e);
        	throw new CoreException(e);
        } catch (CertificateException ex) {
        	logger.error(ex);
        	throw new CoreException(ex);
		}
    	return this;
    }
    
    
   
    X509Certificate getClientCertificate() {
        return this.clientCertificate;
    }

    KeyPair getClientKeyPair() {
        return this.clientKeyPair;
    }


}



