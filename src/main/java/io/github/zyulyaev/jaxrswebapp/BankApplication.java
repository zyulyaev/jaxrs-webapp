package io.github.zyulyaev.jaxrswebapp;

import io.github.zyulyaev.jaxrswebapp.service.inmemory.InMemoryBankService;
import io.github.zyulyaev.jaxrswebapp.web.AccountsResource;
import io.github.zyulyaev.jaxrswebapp.web.TransactionsResource;
import io.github.zyulyaev.jaxrswebapp.web.serialization.JacksonProvider;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * JAX-RS bank application
 */
public class BankApplication extends Application {
    private final Set<Object> singletons = new CopyOnWriteArraySet<>();

    public BankApplication() {
        this(Clock.systemUTC());
    }

    public BankApplication(Clock clock) {
        InMemoryBankService bankService = new InMemoryBankService(clock);
        singletons.add(new AccountsResource(bankService));
        singletons.add(new TransactionsResource(bankService));
    }

    public static void main(String[] args) {
        UndertowJaxrsServer server = new UndertowJaxrsServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        // listens at 8081 by default
        // can be overridden by RESTEASY_PORT env value or org.jboss.resteasy.port system property
        // see org.jboss.resteasy.util.PortProvider
        server.deploy(new BankApplication());
        server.start();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(JacksonProvider.class);
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
