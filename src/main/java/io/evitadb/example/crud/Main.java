package io.evitadb.example.crud;

import io.evitadb.api.EvitaContract;
import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;
import io.evitadb.example.crud.context.EvitaSessionHolder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import org.jline.terminal.Terminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.style.TemplateExecutor;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Main class of the evitaDB CRUD demo. Initializes shared beans and starts the application.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@SpringBootApplication
public class Main {
	private final static Logger log = Logger.getLogger(Main.class.getName());

	/**
	 * Start Spring Boot application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	/**
	 * Displays list of available catalogs in evitaDB and welcoming header.
	 */
	private static void printEvitaDbClientStatus(EvitaClient evita) {
		final StringBuilder status = new StringBuilder("""
			             _ _        ____  ____ \s
			   _____   _(_) |_ __ _|  _ \\| __ )\s
			  / _ \\ \\ / / | __/ _` | | | |  _ \\\s
			 |  __/\\ V /| | || (_| | |_| | |_) |
			  \\___| \\_/ |_|\\__\\__,_|____/|____/\s
			                                   \s
			  ... server connected 😉\n\n
			"""
		);
		final Set<String> catalogNames = evita.getCatalogNames();
		if (!catalogNames.isEmpty()) {
			status.append("Catalogs already available in evitaDB instance:\n\n");
			for (String catalogName : catalogNames) {
				status.append("   - ").append(catalogName).append("\n");
			}
		}
		System.out.println(status);
	}

	/**
	 * Initialize evitaDB client that connects to the remote server.
	 */
	@Bean
	EvitaContract evita() {
		try {
			// we want to disable gRPC client logging to avoid cluttering the output to the console
			InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
			// now set up the client on default gRPC port on localhost
			final EvitaClient evita = new EvitaClient(
				EvitaClientConfiguration.builder()
					.host("localhost")
					.port(5556)
					.build()
			);
			// instruct client to shutdown on JVM exit
			Runtime.getRuntime().addShutdownHook(
				new Thread() {
					@Override
					public void run() {
						evita.close();
					}
				}
			);

			printEvitaDbClientStatus(evita);

			return evita;
		} catch (Exception ex) {
			if (ex instanceof StatusRuntimeException statusException && statusException.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())) {
				log.severe("Evita client failed connecting to evitaDB server.");
			} else {
				log.severe("Evita client failed to start: " + ex.getMessage());
			}
			throw ex;
		}
	}

	/**
	 * Session holder bean is used to track the reference to the single opened evitaDB session.
	 */
	@Bean
	EvitaSessionHolder sessionHolder() {
		return new EvitaSessionHolder();
	}

	/**
	 * Prepare flow builder for commands to use for interactive wizards.
	 */
	@Bean
	ComponentFlow.Builder flowBuilder(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
		return ComponentFlow.builder()
			.terminal(terminal)
			.resourceLoader(resourceLoader)
			.templateExecutor(templateExecutor);
	}

}
