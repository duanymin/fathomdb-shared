package com.fathomdb.proxy.openstack.fs;

import java.util.Map;

import com.fathomdb.proxy.cache.HashKey;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class OpenstackItem {
	private final String name;
	private final HashKey contentHash;
	private final long length;
	private final String contentType;
	private final String lastModified;

	final Map<String, OpenstackItem> children = Maps.newHashMap();

	public OpenstackItem(String name, HashKey contentHash, long length,
			String contentType, String lastModified) {
		this.name = name;
		this.contentHash = contentHash;
		this.length = length;
		this.contentType = contentType;
		this.lastModified = lastModified;
	}

	public OpenstackItem(String name) {
		this(name, null, -1, null, null);
	}

	public OpenstackItem getChild(String key) {
		return children.get(key);
	}

	public boolean isDirectory() {
		if (length > 0)
			return false;

		if (contentType == null || Objects.equal(contentType, "application/x-directory"))
			return true;
		return false;
	}

	public String getContentType() {
		return contentType;
	}

	public HashKey getContentHash() {
		return contentHash;
	}

}
