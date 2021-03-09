// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package opcuaclientmx.actions;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import opcuaclientmx.impl.OpcUaClientManager;
import opcuaclientmx.impl.SubscriptionManager;

public class OpcUaUnsubscribe extends CustomJavaAction<java.lang.Void>
{
	private IMendixObject __OpcUaServerCfg;
	private opcuaclientmx.proxies.OpcUaServerCfg OpcUaServerCfg;
	private IMendixObject __MonitoredItemObject;
	private opcuaclientmx.proxies.MonitoredItem MonitoredItemObject;
	private java.lang.Boolean RestartSubscriptionOnNextReboot;

	public OpcUaUnsubscribe(IContext context, IMendixObject OpcUaServerCfg, IMendixObject MonitoredItemObject, java.lang.Boolean RestartSubscriptionOnNextReboot)
	{
		super(context);
		this.__OpcUaServerCfg = OpcUaServerCfg;
		this.__MonitoredItemObject = MonitoredItemObject;
		this.RestartSubscriptionOnNextReboot = RestartSubscriptionOnNextReboot;
	}

	@java.lang.Override
	public java.lang.Void executeAction() throws Exception
	{
		this.OpcUaServerCfg = __OpcUaServerCfg == null ? null : opcuaclientmx.proxies.OpcUaServerCfg.initialize(getContext(), __OpcUaServerCfg);

		this.MonitoredItemObject = __MonitoredItemObject == null ? null : opcuaclientmx.proxies.MonitoredItem.initialize(getContext(), __MonitoredItemObject);

		// BEGIN USER CODE

		OpcUaClient client = OpcUaClientManager.retrieve(context(), this.OpcUaServerCfg);
		
		SubscriptionManager._getInstance().endSubscription(client, this.MonitoredItemObject.getMonitoredItem_Subscription(), this.MonitoredItemObject, this.RestartSubscriptionOnNextReboot, getContext());
		
		return null;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "OpcUaUnsubscribe";
	}

	// BEGIN EXTRA CODE
	


	// END EXTRA CODE
}