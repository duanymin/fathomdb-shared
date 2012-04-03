package com.fathomdb.proxy.openstack;

public interface ObjectMetadataListener {

	void gotObjectDetails(String objectName, byte[] objectHash,
			long objectBytes, String objectContentType,
			long objectLastModified);

	Object endObjects();

}
