package org.platformlayer.http.jre;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.platformlayer.http.HttpMethod;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.http.HttpResponse;
import org.platformlayer.http.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.io.ByteSource;
import com.fathomdb.io.IoUtils;

public class JreHttpRequest implements HttpRequest {
	static final Logger log = LoggerFactory.getLogger(JreHttpRequest.class);

	final HttpURLConnection httpConn;
	final URI uri;
	final HttpMethod method;

	final SslConfiguration sslConfiguration;

	JreHttpRequest(HttpMethod method, URI uri, SslConfiguration sslConfiguration) throws IOException {
		this.method = method;
		this.sslConfiguration = sslConfiguration;
		this.uri = uri;

		URL url = uri.toURL();

		httpConn = (HttpURLConnection) url.openConnection();

		switch (method) {
		case GET:
		case DELETE:
			httpConn.setDoOutput(false);
			break;

		case POST:
		case PUT:
			httpConn.setDoOutput(true);
			break;
		default:
			throw new IllegalStateException();
		}
		httpConn.setDoInput(true);
		httpConn.setUseCaches(false);
		httpConn.setDefaultUseCaches(false);
		httpConn.setAllowUserInteraction(false);
		httpConn.setRequestMethod(method.getHttpMethod());

		configureSslParameters();
	}

	@Override
	public void setRequestHeader(String key, String value) {
		httpConn.setRequestProperty(key, value);
	}

	JreHttpResponse response;

	@Override
	public JreHttpResponse doRequest() throws IOException {
		if (response == null) {
			response = doRequest0();
		}
		return response;
	}

	protected JreHttpResponse doRequest0() throws IOException {
		return new JreHttpResponse();
	}

	public class JreHttpResponse implements HttpResponse {
		private final int responseCode;

		public JreHttpResponse() throws IOException {
			this(httpConn.getResponseCode());
		}

		public JreHttpResponse(int responseCode) {
			this.responseCode = responseCode;
		}

		@Override
		public int getHttpResponseCode() throws IOException {
			return responseCode;
		}

		@Override
		public Map<String, List<String>> getHeaderFields() {
			return httpConn.getHeaderFields();
		}

		@Override
		public InputStream getErrorStream() {
			return httpConn.getErrorStream();
		}

		public String getResponseMessage() throws IOException {
			return httpConn.getResponseMessage();
		}

		InputStream is = null;

		@Override
		public InputStream getInputStream() throws IOException {
			if (is == null) {
				is = httpConn.getInputStream();
			}
			return is;
		}

		@Override
		public void close() throws IOException {
			if (is != null) {
				is.close();
				is = null;
			}
		}

		@Override
		public String getResponseHeaderField(String name) {
			return httpConn.getHeaderField(name);
		}

		public Map<String, String> getHeadersRemoveDuplicates() {
			Map<String, String> allHeaders = new HashMap<String, String>();
			Map<String, List<String>> headerFields = httpConn.getHeaderFields();
			for (Entry<String, List<String>> headerEntry : headerFields.entrySet()) {
				// Collapse multiple values (we don't expect duplicates)
				for (String headerValue : headerEntry.getValue()) {
					allHeaders.put(headerEntry.getKey(), headerValue);
				}
			}
			return allHeaders;
		}

		@Override
		public String toString() {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
				return sb.toString();
			} catch (IOException e) {
				log.warn("Error in toString", e);
				return "Exception while calling toString";
			}

		}

	}

	@Override
	public URI getUrl() {
		return uri;
	}

	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMethod() + " " + getUrl() + "\n");
		for (Entry<String, List<String>> entry : httpConn.getRequestProperties().entrySet()) {
			sb.append(entry.getKey() + ": " + entry.getValue() + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	private void configureSslParameters() {
		if (sslConfiguration != null && !sslConfiguration.isEmpty()) {
			HttpsURLConnection https = (HttpsURLConnection) httpConn;
			try {
				https.setSSLSocketFactory(sslConfiguration.getSslSocketFactory());

				if (sslConfiguration.getHostnameVerifier() != null) {
					https.setHostnameVerifier(sslConfiguration.getHostnameVerifier());
				}
			} catch (GeneralSecurityException e) {
				throw new IllegalArgumentException("Error loading certificate", e);
			}
		}
	}

	@Override
	public void setRequestContent(ByteSource data) throws IOException {
		OutputStream os = httpConn.getOutputStream();
		InputStream is = data.open();

		try {
			IoUtils.copyToOutputStream(is, os);
		} finally {
			is.close();
		}
	}

}
