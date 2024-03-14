package com.hti.smpp.common.request;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD


import com.hti.rmi.LookupService;


import org.springframework.http.ResponseEntity;
=======
import java.util.Properties;
>>>>>>> d2ab55dce9146e612429ba4dd1a4ac770dfcf126

import com.hti.rmi.LookupReport;
import com.hti.rmi.LookupService;

<<<<<<< HEAD


=======
>>>>>>> d2ab55dce9146e612429ba4dd1a4ac770dfcf126
public class LookupServiceInvoker {

<<<<<<< HEAD
	public LookupServiceInvoker() throws RemoteException, NotBoundException {

	// Connect HLR Server on port 1098
		java.rmi.registry.Registry myRegistry = LocateRegistry.getRegistry("127.0.0.1", 1098);

	
		// search for service
=======
	private LookupService impl = null;

	public LookupServiceInvoker() throws IOException, NotBoundException {
		// Load RMI configuration from application.properties in the resources folder
		Properties prop = loadProperties("/application.properties");
		String rmiAddress = prop.getProperty("rmi.server.address");
		int rmiPort = Integer.parseInt(prop.getProperty("rmi.server.port"));

		// Connect to RMI Registry
		connectToRegistry(rmiAddress, rmiPort);
	}

	private Properties loadProperties(String resourcePath) throws IOException {
		Properties prop = new Properties();
		try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IOException("Unable to load properties file: " + resourcePath);
			}
			prop.load(is);
		}
		return prop;
	}

	private void connectToRegistry(String address, int port) throws RemoteException, NotBoundException {
		Registry myRegistry = LocateRegistry.getRegistry(address, port);
>>>>>>> d2ab55dce9146e612429ba4dd1a4ac770dfcf126
		impl = (LookupService) myRegistry.lookup("lookupService");
	}

	public List<LookupReport> getLookupReport(Map<String, String> params) throws RemoteException {
		System.out.println("LookupReport Params: " + params);
		return impl.getLookupReport(params);
	}

	public int reCheckStatus(String sql) throws RemoteException {
		return impl.reCheckStatus(sql);
	}
}
