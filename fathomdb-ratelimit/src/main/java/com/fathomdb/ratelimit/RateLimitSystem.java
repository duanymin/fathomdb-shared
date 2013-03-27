package com.fathomdb.ratelimit;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.inject.Inject;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;

public class RateLimitSystem implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(RateLimitSystem.class);

	final List<InetSocketAddress> memcacheAddresses;

	private MemcachedClient client;

	final ConnectionFactoryBuilder builder;

	@Inject
	public RateLimitSystem(Configuration config) {
		this.memcacheAddresses = config.lookupList("memcache.servers", new InetSocketAddress("127.0.0.1", 11211));

		ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
		builder.setProtocol(Protocol.BINARY);
		builder.setOpTimeout(2000);

		builder.setFailureMode(FailureMode.Cancel);
		
		String username = config.find("memcache.username");
		if (username != null) {
			String password = config.get("memcache.password");
			AuthDescriptor auth = new AuthDescriptor(new String[] { "CRAM-MD5" }, new PlainCallbackHandler(username,
					password));

			builder.setAuthDescriptor(auth);
		}

		this.builder = builder;

		try {
			this.client = new MemcachedClient(builder.build(), memcacheAddresses);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error building memcache client", e);
		}
	}

	@Override
	public void close() {
		if (client != null) {
			client.shutdown();
			client = null;
		}
	}

	MemcachedClient getClient() {
		return client;
	}
}