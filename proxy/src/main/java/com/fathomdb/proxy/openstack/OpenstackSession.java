package com.fathomdb.proxy.openstack;

import java.net.URI;

/**
 * It would be nice to use the same codebase as the main OpenStack Java binding.
 * However, this is pretty specialized for Swift & Async
 */
public class OpenstackSession implements AutoCloseable {
	final OpenstackClientPool openstackClientPool;
	private final OpenstackCredentials credentials;

	public OpenstackSession(OpenstackClientPool openstackClientPool,
			OpenstackCredentials credentials) {
		this.openstackClientPool = openstackClientPool;
		this.credentials = credentials;
	}

	ObjectListener httpListener;

	KeystoneClient keystone;

	public KeystoneClient getKeystoneClient() {
		if (keystone == null) {
			keystone = new KeystoneClient(openstackClientPool,
					credentials.getAuthUrl());
		}
		return keystone;
	}

	KeystoneAuthenticationOperation keystoneAuthentication;

	public KeystoneAuthenticationOperation getAuthentication() {
		if (keystoneAuthentication == null) {
			keystoneAuthentication = new KeystoneAuthenticationOperation(
					getKeystoneClient(), credentials);
		}
		return keystoneAuthentication;
	}

	SwiftClient swift;

	public SwiftClient getSwiftClient() {
		if (swift == null) {
			URI swiftUrl = getAuthentication().getSwiftUrl();
			swift = new SwiftClient(openstackClientPool, swiftUrl);
		}
		return swift;
	}

	public String getAuthTokenId() {
		return getAuthentication().getAuthTokenId();
	}

	public OpenstackCredentials getCredentials() {
		return credentials;
	}

	public void close() {
		if (keystone != null) {
			keystone.close();
		}
		
		if (swift != null) {
			swift.close();
		}
		
	}

}
