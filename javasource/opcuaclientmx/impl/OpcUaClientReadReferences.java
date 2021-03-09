package opcuaclientmx.impl;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

public class OpcUaClientReadReferences  {

    private static final ILogNode logger = Core.getLogger("OpcUA");

    public List<ReferenceDescription> getNodes(OpcUaClient client, NodeId nodeIdExt) throws Exception {
       
        // start browsing at root folder
        List<ReferenceDescription> refdesc =browseNode(/*"",*/ client, nodeIdExt);
		return refdesc;

        //future.complete(client);
    }
    
    private static  List<ReferenceDescription> browseNode(/*String indent,*/ OpcUaClient client, NodeId browseRoot) {
        BrowseDescription browse = new BrowseDescription(
            browseRoot,
            BrowseDirection.Forward,
            Identifiers.References,
            true,
            uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
            uint(BrowseResultMask.All.getValue())
        );
              
        try {
        	BrowseResult browseResult = client.browse(browse).get();
        	List<ReferenceDescription> references = toList(browseResult.getReferences());
           
            
            
        	//Use the loop below for browsing the whole three at once.  
            /*for (ReferenceDescription rd : references) {
            	int loopCounter = 0;
            	//Lameness intensifies 
            
            	//logger.info("{} Node={}", indent, rd.getBrowseName().getName());//< Activate this for logging purposes
                if (loopCounter <= 1 ) {
                	rd.getNodeId().local().ifPresent(nodeId -> browseNode(indent + "  ", client, nodeId, refreturn));                    
                    refreturn.add(rd); //<< Activate this for retrieving the whole tree at once
                    loopCounter++;
                    //Peak lameness
                
                // recursively browse to children
                
            }*/
            return references;
        
        } catch (InterruptedException | ExecutionException e) {
            logger.error(String.format("Browsing nodeId=%s failed: %s", browseRoot.toParseableString(), e.getMessage()), e);
        }
		return null;
    }
}
