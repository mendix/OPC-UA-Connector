// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package opcuaclientmx.actions;

import java.util.List;
import java.util.Scanner;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import opcuaclientmx.impl.OpcUaClientManager;
import opcuaclientmx.impl.OpcUaClientReadReferences;

public class OpcUaBrowse extends CustomJavaAction<java.lang.String>
{
	private IMendixObject __OpcUaServerCfg;
	private opcuaclientmx.proxies.OpcUaServerCfg OpcUaServerCfg;
	private java.lang.String nodeId;
	private java.lang.Boolean IsRoot;

	public OpcUaBrowse(IContext context, IMendixObject OpcUaServerCfg, java.lang.String nodeId, java.lang.Boolean IsRoot)
	{
		super(context);
		this.__OpcUaServerCfg = OpcUaServerCfg;
		this.nodeId = nodeId;
		this.IsRoot = IsRoot;
	}

	@java.lang.Override
	public java.lang.String executeAction() throws Exception
	{
		this.OpcUaServerCfg = this.__OpcUaServerCfg == null ? null : opcuaclientmx.proxies.OpcUaServerCfg.initialize(getContext(), __OpcUaServerCfg);

		// BEGIN USER CODE
		OpcUaClient client = OpcUaClientManager.retrieve(context(), this.OpcUaServerCfg);
		
			NodeId nodeId; //NodeId creation offers 11 different flavors, pick what your need. 
			if(IsRoot)
			{
				nodeId = Identifiers.RootFolder; //The aforementioned 11 flavors do not include everything available under Identifiers. I didn't count it. 
			}
			
			else {	
				nodeId = NodeId.parse(this.nodeId);
			};	
		
		List<ReferenceDescription> arr = new OpcUaClientReadReferences().getNodes(client,nodeId);
		
		
		//TODO:20201001: discuss if we should replace this wit Gson	
		return new ObjectMapper().writeValueAsString(arr);

		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "OpcUaBrowse";
	}

	// BEGIN EXTRA CODE
	public static boolean isNumeric(String inputData) {
		
		  Scanner sc = new Scanner(inputData);
		  boolean checkNumeric = sc.hasNextInt();
		  sc.close();
		  //This prevents a resource leak 
		  return checkNumeric;
		  
		}
	// END EXTRA CODE
}
