package opcuaclientmx.impl;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
/*JV May/June 2020*/
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import opcuaclientmx.proxies.OpcUaServerCfg;

public class OpcUaClientWrite {

    private static final ILogNode logger = Core.getLogger("OpcUA");

	public StatusCode writeData(String valueToWrite, OpcUaServerCfg OpcUaServerCfg, IContext context, String nodeId) throws CoreException {
		OpcUaClient client = OpcUaClientManager.retrieve(context, OpcUaServerCfg);
		
		if( nodeId == null || "".equals(nodeId) )
			throw new CoreException("[Write] Please provide a NodeId to write the data too.");
		NodeId node = NodeId.parse(nodeId); 

		try {
			logger.trace(String.format("[Write-%s] Writing to [Server:%s|NodeId:%s]; Value: %s", context.getExecutionId(), OpcUaServerCfg.getServerID(),nodeId,valueToWrite) );

			StatusCode code = client.writeValue(node, DataValue.valueOnly( getVariantByCachedDataType(client, node, valueToWrite, context) )).get();

			if( logger.isTraceEnabled())
				logger.trace(String.format("[Write-%s] Response from [Server:%s|NodeId:%s]; Response Data: %s",context.getExecutionId(), OpcUaServerCfg.getServerID(),nodeId,code.toString()) );

			
			return code;
			//Read back of the written data, for test & control purposes

		} catch (NumberFormatException |InterruptedException| ExecutionException e) {
			logger.error(e);
		}
		
		return null;
	}

	private static Variant getVariantByCachedDataType(OpcUaClient client, NodeId node, String valueToWrite, IContext context) throws InterruptedException, ExecutionException, CoreException {
		DataValue response = client.readValue(0, TimestampsToReturn.Both, node).get();
		logger.trace(String.format("[Write-%s] Getting node info from: [Server:%s|NodeId:%s]; Response: %s", context.getExecutionId(), "",node.toParseableString(),response) );  //TODO:20201210: Fix log message

		Object val = null;
		Object identifier = response.getValue().getDataType().get().getIdentifier();
        UInteger id = UInteger.valueOf(0);

        if(identifier instanceof UInteger) {
            id = (UInteger) identifier;
        }
        
		//TODO:20201210: complete parser for additional data types
        //TODO:20201210: create better way to pass through typed values into the Node 
		switch( id.intValue()) {
            // Based on the Identifiers class in org.eclipse.milo.opcua.stack.core; 
			
			case 1:		//Boolean(1, Boolean.class),
				val = Boolean.valueOf(valueToWrite);
				break;
			case 4:		//Int16(4, Short.class),
				val = Short.valueOf(valueToWrite);
				break;
			case 5:		//UInt16(5, UShort.class),
				val = Unsigned.ushort(valueToWrite);
				break;
	        case 6: 	//Int32(6, Integer.class),
	            val = Integer.valueOf(valueToWrite);
	            break;
	        case 8:		//Int64(8, Long.class),
	        	val = Long.valueOf(valueToWrite);
	        	break;
	        case 10:	//Float(10, Float.class),
	        case 11: 	//Double(11, Double.class),
	            val = Double.valueOf(valueToWrite);
	            break;
	        case 12:	//String(12, String.class),
	        	val = valueToWrite;
	        	break;
            default:
	            throw new CoreException("[Write-" + context.getExecutionId() + "] Data Type " + id.intValue() + " isn't implemented yet.");	
			//SByte(2, Byte.class),
			//Byte(3, UByte.class),
			//UInt32(7, UInteger.class),
			//UInt64(9, ULong.class),
			//DateTime(13, DateTime.class),
			//Guid(14, UUID.class),
			//ByteString(15, ByteString.class),
			//XmlElement(16, XmlElement.class),
			//NodeId(17, NodeId.class),
			//ExpandedNodeId(18, org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId.class),
			//StatusCode(19, StatusCode.class),
			//QualifiedName(20, QualifiedName.class),
			//LocalizedText(21, org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText.class),
			//ExtensionObject(22, ExtensionObject.class),
			//DataValue(23, org.eclipse.milo.opcua.stack.core.types.builtin.DataValue.class),
			//Variant(24, Variant.class),
			//DiagnosticInfo(25, org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo.class);	
		}
		
		return new Variant( val );
	}
}