package org.platformlayer.auth;

import org.platformlayer.http.HttpRequest;

public interface AuthenticationToken {
	void populateRequest(HttpRequest httpRequest);
}
