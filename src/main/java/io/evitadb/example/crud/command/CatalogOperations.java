package io.evitadb.example.crud.command;

import io.evitadb.api.EvitaSessionContract;
import io.evitadb.example.crud.context.EvitaHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.util.Optional;
import java.util.Set;

/**
 * Class contains operations within single opened catalog.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@ShellComponent
@ShellCommandGroup("Catalog operations")
public class CatalogOperations {
	@Autowired ComponentFlow.Builder flowBuilder;
	@Autowired EvitaHolder evitaHolder;

	@ShellMethod(key = "setup-example-collections", value = "creates example collections for categories, brands and products")
	@ShellMethodAvailability("evitaSessionOpened")
	public String setupExampleCollections() {
		final EvitaSessionContract session = evitaHolder.getSession();
		return "Example entity collection schemas has been set up.";
	}

	@ShellMethod(key = "list-collections", value = "lists all available entity collections in opened catalog")
	@ShellMethodAvailability("evitaSessionOpened")
	public String listCatalogs() {
		final StringBuilder output = new StringBuilder();
		int no = 1;

		final EvitaSessionContract session = evitaHolder.getSession();
		final Set<String> allEntityTypes = session.getAllEntityTypes();
		if (allEntityTypes.isEmpty()) {
			return "No entity collections are present in the catalog.";
		} else {
			for (String entityType : allEntityTypes) {
				output.append("   ").append(no++).append(". ")
					.append(entityType)
					.append(" (").append(session.getEntityCollectionSize(entityType)).append(" entities)")
					.append("\n");
			}
		}
		return output.toString();
	}

	private Availability evitaSessionOpened() {
		return Optional.ofNullable(evitaHolder.getSession())
			.map(it -> Availability.available())
			.orElseGet(() -> Availability.unavailable("you need to open some catalog, first"));
	}

}
