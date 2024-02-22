package opcuaclientmx.impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;


import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;

import org.eclipse.milo.opcua.stack.core.AttributeId;

import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteSubscriptionsResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

import com.google.gson.Gson;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.CoreRuntimeException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IDataType.DataTypeEnum;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import opcuaclientmx.proxies.Message;
import opcuaclientmx.proxies.MonitoredItem;
import opcuaclientmx.proxies.OpcUaServerCfg;
import opcuaclientmx.proxies.Subscription;
import opcuaclientmx.proxies.SubscriptionStatus;



public class SubscriptionManager 
{

    private static final ILogNode logger = Core.getLogger("OpcUA");

	private static LinkedHashMap<String, UaMonitoredItem> monitoredItems = new LinkedHashMap<>(); 
	private static LinkedHashMap<String, UaSubscription> subscriptions = new LinkedHashMap<>();  //TODO:20201211: Refactor to store the info through a combination of SubId+MiId
	private static SubscriptionManager _instance;


	// monitoring parameters
	private Double samplingInterval = 500.0; 				//ms
	private Double requestedPublishingInterval = 500.0;		//Publishing interval by default 1000 milliseconds
	private UInteger queueSize = UInteger.valueOf(10);
	private Boolean discardOldest = true;
	
	private SubscriptionManager() {
		
	}
	
	public static SubscriptionManager _getInstance() {
		if( _instance == null )
			_instance = new SubscriptionManager();
		
		return _instance;
	}

	public IMendixObject addSubscription(IContext context, OpcUaServerCfg opcUaServerCfg, Subscription subscriptionObject, String nodeId, String onMessageMicroflow) throws CoreException {
		OpcUaClient client = OpcUaClientManager.retrieve(context, opcUaServerCfg);
		
		//Setup the Subscription & Monitored Item in the OPC client
		UaSubscription subscription = findOrCreateSubscription(opcUaServerCfg, client, opcUaServerCfg.getServerID(), subscriptionObject, context);
		List<UaMonitoredItem> itemList =  SubscriptionManager._getInstance().createSubscriptionListener(subscription, NodeId.parse(nodeId), onMessageMicroflow, opcUaServerCfg, context);
		
		

		//Evaluate the response and update the subscription entity accordingly
		UaMonitoredItem first = itemList.get(0); 	//TODO:20201201: Add Support for multi value responses of subscription
		
		MonitoredItem monitoredItemObject = MonitoredItem.initialize(context, Core.instantiate(context, MonitoredItem.entityName));
			monitoredItemObject.setNodeId(nodeId);
			monitoredItemObject.setSubscriptionID(subscription.getSubscriptionId().toString());
			monitoredItemObject.setMonitoredItemID(first.getMonitoredItemId().toString());
			monitoredItemObject.setOnMessageMicroflow(onMessageMicroflow);
			monitoredItemObject.setLastSubscribedOn(new Date());
			monitoredItemObject.setLastStateChange(new Date());
			monitoredItemObject.setStatus(SubscriptionStatus.Active);
			monitoredItemObject.setMonitoredItem_OpcUaServerCfg(opcUaServerCfg);
			
			Core.commit(context, monitoredItemObject.getMendixObject());
			
			return monitoredItemObject.getMendixObject();
	}
	
	public MonitoredItem refreshSubscription(IContext context, OpcUaServerCfg opcUaServerCfg, Subscription subscriptionObject, MonitoredItem monitoredItemObject) throws CoreException {		
		OpcUaClient client = OpcUaClientManager.retrieve(context, opcUaServerCfg);
		
		//Setup the Subscription & Monitored Item in the OPC client
		UaSubscription subscription = findOrCreateSubscription(opcUaServerCfg, client, opcUaServerCfg.getServerID(), subscriptionObject, context);
		List<UaMonitoredItem> itemList =  createSubscriptionListener(subscription, NodeId.parse(monitoredItemObject.getNodeId()), monitoredItemObject.getOnMessageMicroflow(), opcUaServerCfg, context);
		
		//Evaluate the response and update the subscription entity accordingly
		UaMonitoredItem first = itemList.get(0); 	//TODO:20201201: Add Support for multi value responses of subscription
		monitoredItemObject.setSubscriptionID(subscription.getSubscriptionId().toString());
		monitoredItemObject.setMonitoredItemID(first.getMonitoredItemId().toString());
		monitoredItemObject.setLastSubscribedOn(new Date());
		monitoredItemObject.setLastStateChange(new Date());
		monitoredItemObject.setStatus(SubscriptionStatus.Active);
		monitoredItemObject.commit();
		
		return monitoredItemObject;
	}
	
	
	private List<UaMonitoredItem> createSubscriptionListener(UaSubscription subscription, NodeId node, String OnMessageMicroflow, OpcUaServerCfg opcUaServerCfg, IContext context) throws CoreException {
		String serverID = opcUaServerCfg.getServerID();
		String subscriptionID = subscription.getSubscriptionId().toString();
		
		//TODO:20201210: This is where we specify what we want to receive about events on this node, in the Attribute type
		ReadValueId readValueId = new ReadValueId(node, AttributeId.Value.uid(), null, null);
		ExtensionObject filter = null; //ExtensionObject not used here

		MonitoringParameters parameters = new MonitoringParameters(subscription.nextClientHandle(), this.samplingInterval, filter, this.queueSize, this.discardOldest);
		MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

		
		UaMonitoredItem.ValueConsumer consumer = createConsumerFunction(OnMessageMicroflow, serverID, subscriptionID);
		// setting the consumer after the subscription creation
		UaSubscription.ItemCreationCallback onItemCreated = (monitoredItem, id) -> monitoredItem.setValueConsumer(consumer);    
		
		List<UaMonitoredItem> items;
		try {
			items = subscription.createMonitoredItems( TimestampsToReturn.Both, Arrays.asList(request), onItemCreated).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CoreException("Unable to create MonitoredItem: " + readValueId.toString(),e);
		}

		for( UaMonitoredItem item : items ) {
			monitoredItems.put(subscriptionID + "-" + item.getMonitoredItemId(), item);
		}
		
		if( logger.isTraceEnabled() )
			logger.trace(String.format("[Subscription-%s] Registered new MonitoredItem: %s [Server:%s|Subscription:%s|Microflow:%s]", context.getExecutionId(), readValueId.toString(), serverID,subscription.getSubscriptionId().toString(),OnMessageMicroflow));
		
		return items;
	}

	/**
	 * Establish a new subscription or re-use an existing one that has been cached in memory
	 * 
	 * @param opcUaServerCfg 
	 * 
	 * @param client
	 * @param serverID
	 * @param subscriptionObject 
	 * @throws CoreException
	 */
	private UaSubscription findOrCreateSubscription(OpcUaServerCfg opcUaServerCfg, OpcUaClient client, String serverID, Subscription subscriptionObject, IContext context) throws CoreException {
		UaSubscription subscription;
		
		if( subscriptionObject != null ) {
			String subId = subscriptionObject.getSubscriptionID();
			
			/* 
			 * The subscription ID that we've found exists already in memory, in this case we are either doing a re-try or the Subscription has been created previously in a microflow
			 * In either case we want to look up the Subscription Mendix Object along with the UA-Subscription and re-use that for all future logic
			 */
			if( subId != null && !"".equals(subId) && subscriptions.containsKey(subId) ) {
				subscription = subscriptions.get(subId);
				
				if( logger.isTraceEnabled() )
					logger.trace(String.format("[Subscription-%s] Re-using existing subscription: %s [Server:%s|Subscription:%s]", context.getExecutionId(), subscription.getSubscriptionId().toString(),serverID,subscription.getSubscriptionId().toString()));

				subscriptionObject.setStatus(SubscriptionStatus.Active);
				subscriptionObject.setSubscriptionID(subscription.getSubscriptionId().toString());
				subscriptionObject.commit();
			}
			
			/*
			 * The Subscription Id that we've found has never been used,  
			 * we need to create the UA-Subscription with the Requested Interval that passed along with the Subscription Mendix Object that was previously created in a microflow
			 */
			else {
				try {
					subscription = client.getSubscriptionManager().createSubscription(subscriptionObject.getRequestedPublishingInterval_ms().doubleValue()).get();
					subscriptions.put(subscription.getSubscriptionId().toString(), subscription);

					if( logger.isTraceEnabled() )
						logger.trace(String.format("[Subscription-%s] Registered new subscription: %s [Server:%s|Subscription:%s]", context.getExecutionId(), subscription.getSubscriptionId().toString(),serverID,subscription.getSubscriptionId().toString()));

				} catch (InterruptedException | ExecutionException e1) {
					throw new CoreException("[Subscription] Unable to create the subscription as configured for Server: " + serverID + " @ interval: " + subscriptionObject.getRequestedPublishingInterval_ms().doubleValue(), e1);
				}
				subscriptionObject.setSubscription_OpcUaServerCfg(opcUaServerCfg);
				subscriptionObject.setStatus(SubscriptionStatus.Active);
				subscriptionObject.setSubscriptionID(subscription.getSubscriptionId().toString());
				subscriptionObject.commit();
			}
		}
		

		/*
		 * No explicit Subscription Id has been passed that means we are going to create a new connection with the default values.
		 */
		else {
			try {
				subscription = client.getSubscriptionManager().createSubscription(this.requestedPublishingInterval).get();
				subscriptions.put(subscription.getSubscriptionId().toString(), subscription);
				//TODO:20210108: Create a Subscription Entity so that the module can show a complete overview of all Monitored Items and subscriptions as the are being used.
	
				if( logger.isTraceEnabled() )
					logger.trace(String.format("[Subscription-%s] Registered new subscription: %s [Server:%s|Subscription:%s]", context.getExecutionId(), subscription.getSubscriptionId().toString(),serverID,subscription.getSubscriptionId().toString()));

			} catch (InterruptedException | ExecutionException e1) {
				throw new CoreException("[Subscription] Unable to create a new subscription for Server: " + serverID + " @ interval: " + this.requestedPublishingInterval, e1);
			}
		}
		
		return subscription;
	}

	
	
	
	private static UaMonitoredItem.ValueConsumer createConsumerFunction(String OnMessageMicroflow, String serverID, String SubscriptionID) {
		
		boolean simpleMicroflow = true, hasPayloadParam = false;
		String msgParamName = null;
		
		Map<String, IDataType> params = Core.getInputParameters(OnMessageMicroflow);	
		for(Entry<String, IDataType> entry : params.entrySet() ) {
			if( entry.getValue().getType() == DataTypeEnum.Object && Message.entityName.equals( entry.getValue().getObjectType() ) ) {
				simpleMicroflow = false;
				msgParamName = entry.getKey();
				break;
			}
			else if( entry.getValue().getType() == DataTypeEnum.String && entry.getKey().equals("Payload") )
				hasPayloadParam = true;
		}

		if( simpleMicroflow == true && hasPayloadParam == false || simpleMicroflow == false && msgParamName == null ) 
			throw new CoreRuntimeException("Cannot create subscription, Microflow: " + OnMessageMicroflow + " doesn't have the right input parameters. Requiers parameter 'Payload' of type String OR an Object parameter of type 'Message' (can be any name). ");
		
		final boolean execSimpleFlow = simpleMicroflow;
		final String execParamName = msgParamName;
		return (item, value) -> {
			String msgPayload = null;
			IContext context = Core.createSystemContext(); context.setExecutionId(SubscriptionID+"-"+item.getMonitoredItemId().toString());
			try { msgPayload = new Gson().toJson(value.getValue().getValue()); 	}
			finally {
				if( logger.isTraceEnabled() )
					logger.trace(String.format("[Subscription-" + context.getExecutionId() + "] Received Value: %s. [Server:%s|Subscription:%s|Item:%s|Message:%s]", msgPayload,serverID,SubscriptionID,item.getReadValueId(),value.toString()));
			}
			
			if( execSimpleFlow ) {
				Core.microflowCall(OnMessageMicroflow)
					.withParam("SubscriptionID", SubscriptionID)
					.withParam("MonitoredItemID", item.getMonitoredItemId().toString())
					.withParam("Payload", msgPayload)
					.execute(context);	
			}
			else {
				Message msg = Message.initialize(context, Core.instantiate(context, Message.entityName));
				msg.setSubscriptionID(SubscriptionID);
				msg.setServerID(serverID);
				msg.setMonitoredItemID(item.getMonitoredItemId().toString());
				msg.setPayload(msgPayload);
				msg.setFullMessage(value.toString());
				msg.setStatusCodeName(value.getStatusCode().toString());
				msg.setServerTime(value.getServerTime().getJavaDate());
				msg.setSourceTime(value.getSourceTime().getJavaDate());
				

				Core.microflowCall(OnMessageMicroflow)
					.withParam(execParamName, msg.getMendixObject())
					.execute(context);
			}
		};
	}

	
	//TODO:20201210: This is where we specify that we want to receive events on this node, in the Attribute type To enable HA: Refactor this to check for instructions every 10 seconds.
	public void endSubscription(OpcUaClient client, Subscription subscriptionObject, MonitoredItem monitoredItemObject, boolean restartSubscriptionOnNextReboot, IContext context) throws ExecutionException, CoreException {
		
		UaMonitoredItem monitoredItem = monitoredItems.get(monitoredItemObject.getSubscriptionID() + "-" + monitoredItemObject.getMonitoredItemID());
		UaSubscription subscription = subscriptions.get(monitoredItemObject.getSubscriptionID());
		UInteger subscriptionID = (subscription != null ? subscription.getSubscriptionId() : UInteger.valueOf(monitoredItemObject.getSubscriptionID()));
		
		if( monitoredItem != null && subscription != null ) {

			List<UaMonitoredItem> monitoredItemListToRemove = new ArrayList<UaMonitoredItem>();

			//First delete the monitored item
			monitoredItemListToRemove.add(monitoredItem);
			try {
				List<StatusCode> sCodeList = subscription.deleteMonitoredItems(monitoredItemListToRemove).get();
				for( int i = 0; i < sCodeList.size(); i++ ) {
					StatusCode sCode = sCodeList.get(i);
					UaMonitoredItem removedItem = monitoredItemListToRemove.get(i);

					if( !sCode.isGood() )
						logger.error("[Subscription-" + context.getExecutionId() + "] Unable to remove monitored item: " + removedItem.getMonitoredItemId().toString() + " response: " + sCode);
					else 
						logger.trace("[Subscription-" + context.getExecutionId() + "] Successfully removed monitored item: " + removedItem.getMonitoredItemId().toString() + " response: " + sCode);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(String.format("[Subscription-" + context.getExecutionId() + "] Error while deleting monitored item.  [Subscription:%s|Items:%s]", monitoredItemObject.getSubscriptionID(),monitoredItemListToRemove.toString()), e);
			}			//This needs more flexibility, we can now only add/remove just one subscription per request.
			

			monitoredItemObject.setLastStateChange(new Date());
			if( restartSubscriptionOnNextReboot ) 
				monitoredItemObject.setStatus(SubscriptionStatus._New);
			else 
				monitoredItemObject.setStatus(SubscriptionStatus.Deleted);
			monitoredItemObject.commit();
			
			monitoredItems.remove(monitoredItemObject.getSubscriptionID() + "-" + monitoredItemObject.getMonitoredItemID());
		}
		// There is no monitoredItem in our memory, so we can't do anything here
		else {
			logger.warn( String.format("[Subscription-%s] Unable to remove MonitoredItem: %s-%s. MonitoredItem was already Removed.", 
					context.getExecutionId(), monitoredItemObject.getSubscriptionID(), monitoredItemObject.getMonitoredItemID()) );
		}
		
		
		if( subscription != null ) {
			List<UaMonitoredItem> monitoredItems =  subscription.getMonitoredItems();
			
			if( monitoredItems.size() == 0 ) {
				
				if( logger.isTraceEnabled() )
					logger.trace( String.format("[Subscription-%s] After removing MonitoredItem, also removing Subscription: %s", 
							context.getExecutionId(), monitoredItemObject.getSubscriptionID()) );
				
				List<UInteger> IDs = new ArrayList<UInteger>();
				IDs.add(subscriptionID);
			
				try {
					DeleteSubscriptionsResponse responseSubscription = client.deleteSubscriptions(IDs).get();
					StatusCode[] sCodeList = responseSubscription.getResults();
					for( int i = 0; i < sCodeList.length; i++ ) {
						StatusCode sCode = sCodeList[i];
						UInteger id = IDs.get(i);
						
						if( !sCode.isGood() )
							logger.error("[Subscription-" + context.getExecutionId() + "] Unable to remove Subscription: " + id + " response: " + sCode);
						else 
							logger.trace("[Subscription-" + context.getExecutionId() + "] Successfully removed monitored item: " + id + " response: " + sCode);
					}
				} catch (InterruptedException | ExecutionException e) {
					logger.error(String.format("[Subscription-" + context.getExecutionId() + "] Error while deleting Subscriptions.  [Items:%s]", IDs.toString()), e);
				}
				
				if( subscriptionObject != null ) {
					subscriptionObject.setStatus(SubscriptionStatus.Deleted);
					subscriptionObject.commit();
				}
				subscriptions.remove(monitoredItemObject.getSubscriptionID());
			}
			else if( logger.isTraceEnabled() )
				logger.trace( String.format("[Subscription-%s] After removing MonitoredItem, keeping Subscription: %s. There are %d more MonitoredItems.", 
						context.getExecutionId(), monitoredItemObject.getSubscriptionID(), monitoredItems.size()) );

		}
		
		// There is no subscription in our memory, so we can't do anything here
		else {
			logger.warn( String.format("[Subscription-%s] Unable to remove Subscription: %s. Subscription was already Removed.", 
					context.getExecutionId(), monitoredItemObject.getSubscriptionID()) );
		}
	}
}
