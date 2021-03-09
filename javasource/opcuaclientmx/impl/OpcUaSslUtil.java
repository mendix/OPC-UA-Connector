package opcuaclientmx.impl;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;

import opcuaclientmx.proxies.OpcUaServerCfg;


/**
 * This class encapsulates the SSL functionality modified to work with the
 * latest versions of Bouncy Castle.
 *
 * @author Fabio Silva (silfabio@amazon.com)
 * Extended by Jos Verbeek to work with OPC UA Client/Server
 */
public class OpcUaSslUtil {
	
	private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;
  
    
	public OpcUaSslUtil loadCertFiles( final OpcUaServerCfg serverCfg, IContext context, char[] keyPassword) throws CoreException {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			
			InputStream inp = Core.getFileDocumentContent(context, serverCfg.getMendixObject());
			
			keyStore.load(inp, keyPassword);

			//If a certificate contains more aliases, fetch the correct one.
			String alias = keyStore.aliases().nextElement().toString();
			Key keyPrivate = keyStore.getKey(alias, keyPassword);
				 
			this.clientCertificate = (X509Certificate) keyStore.getCertificate(alias);
			PublicKey keyPublic = this.clientCertificate.getPublicKey();
			this.clientKeyPair = new KeyPair(keyPublic, (PrivateKey)keyPrivate);
				         
			return this;
		} catch (Exception e) {
			throw new CoreException("Unable to load the authentication certificate from server: " + serverCfg.getServerID() + ". Exception: " + e.getMessage(), e);
		}
	}
  
    X509Certificate returnClientCertificate() {
  		return this.clientCertificate;
     }

    KeyPair getClientKeyPair() {
        return this.clientKeyPair;
    }
}