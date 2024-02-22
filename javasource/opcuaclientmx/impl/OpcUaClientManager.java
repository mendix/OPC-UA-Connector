package opcuaclientmx.impl;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*JV May/June 2020*/
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;

import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import encryption.proxies.microflows.Microflows;
import opcuaclientmx.proxies.AuthenticationType;
import opcuaclientmx.proxies.OpcUaServerCfg;
import opcuaclientmx.proxies.SecurityMode;
import opcuaclientmx.proxies.constants.Constants;


public class OpcUaClientManager {

    private static final ILogNode logger = Core.getLogger("OpcUA");
	private static HashMap<String, OpcUaClient> clientCache = new HashMap<>();
	
	
	public static OpcUaClient retrieve(IContext context, OpcUaServerCfg serverHelper) throws CoreException 
	{
		//1. Check for an existing client for the serverHelper object in cache
		//2. If no client is found, a new one is generated.
		if (clientCache.get(serverHelper.getURL()) != null) {
			OpcUaClient client = clientCache.get(serverHelper.getURL());
			
			return client;
		}
		else {
			OpcUaClient client = buildNewClient(context, serverHelper);
			clientCache.put(serverHelper.getURL(), client); //URL functions as retrieval key

			//The code below enables Debug logginf for slf4j in the console. 
			//ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
			//root.setLevel(Level.DEBUG);
			
			return client;
		}
	}
	public static OpcUaClient updateConfigIfExists(IContext context, OpcUaServerCfg serverHelper) throws CoreException 
	{
		//1. Check for an existing client for the serverHelper object in cache
		//2. If no client is found, a new one is generated.
		if (clientCache.get(serverHelper.getURL()) != null) {
			
			//Disconnect the old client config
			OpcUaClient client = clientCache.get(serverHelper.getURL());
			client.disconnect();
			
			//Create a new client with the current server configuration
			client = buildNewClient(context, serverHelper);
			clientCache.put(serverHelper.getURL(), client); 
			
			return client;
		}
		
		return null;
	}
	public static void disconnect(OpcUaServerCfg serverHelper) throws CoreException {
		if (clientCache.get(serverHelper.getURL()) != null) {
			try {
				clientCache.remove(serverHelper.getURL()).disconnect().get();
			}
			catch (Exception e) {
				throw new CoreException("Unable to disconnect the session: " + serverHelper.getServerID(), e);
			}
		}
	}

	private static OpcUaClient buildNewClient(IContext context, OpcUaServerCfg serverHelper) throws CoreException  {	
		try {
			EndpointDescription endpoint = opcUaCreateEndpoint(serverHelper);
			
			logger.info("Initializing new Client for Server: " + serverHelper.getURL());
			OpcUaClientConfigBuilder cfg =  buildOpcUaCfg(endpoint, serverHelper, context) ; 
			OpcUaClient client = OpcUaClient.create(cfg.build());
			client.connect().get();
			//Initiates the session.
			
			addSubscriptionListener(client, serverHelper.getServerID());
			
			return client;
		}
		catch (InterruptedException|UaException|ExecutionException e) { throw new CoreException(e); }
	}	
	
	/**
	 * Add a SubscriptionManager to the client, this will log any special events related to Subscriptions
	 * @param client
	 */
	private static void addSubscriptionListener(OpcUaClient client, String serverId) {
		client.getSubscriptionManager()
			.addSubscriptionListener(new UaSubscriptionManager.SubscriptionListener()
			{
				@Override
				public void onKeepAlive(UaSubscription subscription, DateTime publishTime) {
					Core.getLogger("OpcUA-Subscription")
						.debug(String.format("onKeepAlive event for [Server:%s|Subscription:%s]",
								serverId,
								subscription.getSubscriptionId()));
				}

				@Override
				public void onStatusChanged(UaSubscription subscription, StatusCode status) {
					Core.getLogger("OpcUA-Subscription")
						.info(String.format("onStatusChanged event for [Server:%s|Subscription:%s], status = %s",
								serverId,
								subscription.getSubscriptionId(),
								status.toString()));
				}

				@Override
				public void onPublishFailure(UaException exception) {
					Core.getLogger("OpcUA-Subscription").error("onPublishFailure exception on server: " + serverId, exception);
				}

				@Override
				public void onNotificationDataLost(UaSubscription subscription) {
					Core.getLogger("OpcUA-Subscription")
						.warn(String.format("onNotificationDataLost event for [Server:%s|Subscription:%s]",
								serverId,
								subscription.getSubscriptionId()));
				}
				
				@Override
				public void onSubscriptionTransferFailed(UaSubscription subscription, StatusCode statusCode) {
					Core.getLogger("OpcUA-Subscription")
						.error(String.format("onSubscriptionTransferFailed event for [Server:%s|Subscription:%s], status = %s",
								serverId,
								subscription.getSubscriptionId(),
								statusCode.toString()));
				}
			});		
	}

	private static OpcUaClientConfigBuilder buildOpcUaCfg(EndpointDescription endpoint,  
			OpcUaServerCfg connector,   IContext context ) throws CoreException  {
		
		OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder()
				.setApplicationName(LocalizedText.english(Constants.getUA_ApplicationName()))
				.setApplicationUri(Constants.getUA_ApplicationURI())
				.setEndpoint(endpoint) //filter endpoint based on security policy			
				.setRequestTimeout(uint(5000)) //Set timeout to what fits you.
				.setSessionTimeout(UInteger.valueOf(60000)); //Set timeout to what fits you.	
				//Standard configuration for all situations.
        
		    if (connector.getAuthenticationType() == AuthenticationType.CERTIFICATE ){
		    	String certPass_encrypted = connector.getAuthenticationCertificate(context).getCertifcatePassword_Encrypted();
		    	
		    	//Use this code for test/acceptance/production environments. Test code is available commented out in the bottom of this document.
		    	OpcUaSslUtil ssl = new OpcUaSslUtil().loadCertFiles(connector, context, Microflows.decrypt(context, certPass_encrypted).toCharArray());
		    	cfg.setIdentityProvider(
		    			new X509IdentityProvider(ssl.returnClientCertificate(), ssl.getClientKeyPair().getPrivate()));   				
        		
		    	cfg.setCertificate(ssl.returnClientCertificate()); 
        		cfg.setKeyPair(ssl.getClientKeyPair());		    	
	        		
		    }
		    else if (connector.getAuthenticationType() == AuthenticationType.CREDENTIALS){
			    	cfg.setIdentityProvider(
			    			new UsernameProvider(
			    					connector.getUsername(), 
			    					Microflows.decrypt(context, connector.getPassword_Encrypted()))
			    			); //<< Use this for user/pass identification.    		
		    }
		
		    else if (connector.getAuthenticationType() == AuthenticationType.NONE) {
		    		cfg.setIdentityProvider(new AnonymousProvider()); 
		    }
		    
		    if (connector.getSecurityMode() != SecurityMode.None)  {
		    	//load keys and certifcates		    	
		    	try {
			    	Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
			        Files.createDirectories(securityTempDir);
			        if (!Files.exists(securityTempDir)) {
			            throw new CoreException("unable to create security dir: " + securityTempDir);
			        }
	
			        File pkiDir = securityTempDir.resolve("pki").toFile();
	
			        MxClientKeyStoreLoader mxKeyStoreLoader = new MxClientKeyStoreLoader().load(securityTempDir, context, connector ); 
			        DefaultTrustListManager trustListManager = new DefaultTrustListManager(pkiDir);
			        DefaultClientCertificateValidator certificateValidator =
			            new DefaultClientCertificateValidator(trustListManager);
				
					trustListManager.addTrustedCertificate(mxKeyStoreLoader.getServerCertificate());
					
					
			        //Use this KeyStoreloader when to test with self generated selfSignedKeys
			        if(mxKeyStoreLoader.UseSelfSignedGeneratedClientCertificates()) {
					
			        	//Generate self signed certificates using App URI.    	
			        	DefaultSelfSignedKeyStoreLoader keyStoreLoader = new DefaultSelfSignedKeyStoreLoader().load(securityTempDir, Constants.getUA_ApplicationURI());
			        	cfg.setKeyPair(keyStoreLoader.getClientKeyPair());
				    	cfg.setCertificate(keyStoreLoader.getClientCertificate());
				    	cfg.setCertificateChain(keyStoreLoader.getClientCertificateChain());
				    	
			        }
			        else {
			        	cfg.setKeyPair(mxKeyStoreLoader.getClientKeyPair());
				    	cfg.setCertificate(mxKeyStoreLoader.getClientCertificate());
				    	cfg.setCertificateChain(mxKeyStoreLoader.getClientCertificateChain());
				    	
			        }
			        	
			    	cfg.setCertificateValidator(certificateValidator);
			    	
				} catch (IOException e) {
					throw new CoreException(e);
				}
		    }		    			   		   

        return cfg;
	}

	
	private static EndpointDescription opcUaCreateEndpoint(OpcUaServerCfg connector) throws CoreException {
		
		try {
			List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(connector.getURL()).get();

			EndpointDescription endpoint = endpoints.stream()
                       .filter(e -> e.getSecurityPolicyUri().equals("http://opcfoundation.org/UA/SecurityPolicy#" + connector.getSecurityPolicy().getCaption()))
                       .findFirst()
                       .orElseThrow(() -> new RuntimeException("No desired endpoints supported"));
			 if(endpoint == null) { 
				 throw new CoreException ("Endpoint with SecurityPolicy " + connector.getSecurityPolicy().getCaption() + " not found");
			 }
			return endpoint; 
		}
		catch (Exception e) {
			throw new CoreException("Unable to setup endpoints for Server: " + connector.getServerID() + ". Exception: " + e.getMessage(), e);
		}
	}
}
