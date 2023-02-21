package io.evitadb.example.crud.context;

import io.evitadb.api.EvitaSessionContract;
import org.springframework.beans.factory.DisposableBean;

/**
 * Simple wrapper around opened {@link io.evitadb.api.EvitaSessionContract} to selected catalog.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
public class EvitaSessionHolder implements DisposableBean {
	private EvitaSessionContract session;

	public EvitaSessionContract getSession() {
		return session;
	}

	public void setSession(EvitaSessionContract session) {
		this.session = session;
	}

	@Override
	public void destroy() throws Exception {
		if (session != null) {
			session.close();
		}
	}

}
