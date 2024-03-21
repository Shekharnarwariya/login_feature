package com.hti.listener;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hti.util.GlobalVar;

public class ClientEventListener implements ClientListener {
	private Logger logger = LoggerFactory.getLogger(ClientEventListener.class);

	@Override
	public void clientConnected(Client client) {
		logger.info("Client Connected: " + client.getSocketAddress().getHostString() + ":"
				+ client.getSocketAddress().getPort() + " - " + client.getUuid());
		Collection<Client> clients = GlobalVar.hazelInstance.getClientService().getConnectedClients();
		if (!clients.isEmpty()) {
			logger.info("Connected Clients: " + clients.size());
			for (Client member_client : clients) {
				logger.info("Client: " + member_client.getSocketAddress().getAddress() + ":"
						+ member_client.getSocketAddress().getPort() + " - " + member_client.getUuid());
			}
		}
	}

	@Override
	public void clientDisconnected(Client client) {
		logger.info("Client DisConnected: " + client.getSocketAddress().getHostString() + ":"
				+ client.getSocketAddress().getPort() + " - " + client.getUuid());
		Collection<Client> clients = GlobalVar.hazelInstance.getClientService().getConnectedClients();
		if (!clients.isEmpty()) {
			logger.info("Connected Clients: " + clients.size());
			for (Client member_client : clients) {
				logger.info("Client: " + member_client.getSocketAddress().getAddress() + ":"
						+ member_client.getSocketAddress().getPort() + " - " + member_client.getUuid());
			}
		} else {
			logger.info("*** No Client Connected ***");
		}
	}
}
