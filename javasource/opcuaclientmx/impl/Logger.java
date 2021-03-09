package opcuaclientmx.impl;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

public class Logger {
    private static final ILogNode logger = Core.getLogger("OpcUA");

	public static void info(String msg, String customDetails ) {
		logger.error(
				String.format("%s. Message [%s]", 
						msg, customDetails)
				);
	}

//	public static void info(String msg, String customDetails, String args[]) {
//		logger.error(
//				String.format("%s. Message [%s]", 
//						msg, customDetails)
//				);
//	}
}
