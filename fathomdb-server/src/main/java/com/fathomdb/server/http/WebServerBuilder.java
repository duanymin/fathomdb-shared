package com.fathomdb.server.http;

import java.io.File;
import java.net.InetAddress;
import java.util.Set;

import org.eclipse.jetty.server.Server;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public interface WebServerBuilder {

    Server start() throws Exception;

    void addHttpConnector(InetAddress address, int port, boolean async);

    void addHttpConnector(int port, boolean async);

    void addGuiceContext(String path, Injector injector);

    void addGuiceContext(String path, GuiceServletContextListener servletConfig);

    void addHttpsConnector(InetAddress address, int port, Set<SslOption> options) throws Exception;

    void addHttpsConnector(int port, Set<SslOption> options) throws Exception;

    void addWar(String key, File file);

    /**
     * Enable request logging. Call early (before adding contexts)
     */
    void enableRequestLogging();

    /**
     * Enable request logging. Call early (before adding contexts)
     */
    void enableSessions();
}
