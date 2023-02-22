package io.evitadb.example.crud.api;

import io.evitadb.driver.EvitaClient;
import io.evitadb.driver.config.EvitaClientConfiguration;

import javax.annotation.Nonnull;

/**
 * Example class showing how to create {@link EvitaClient}.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
public class CreateEvitaClient {

	/**
	 * Creates new {@link EvitaClient} targeting remote evitaDB server on localhost and port 5556 (default gRPC port).
	 * The client is created regardless of whether the server is available.
	 */
	@Nonnull
	public static EvitaClient createEvitaClient() {
		return new EvitaClient(
			EvitaClientConfiguration.builder()
				.host("localhost")
				.port(5556)
				.build()
		);
	}

}
