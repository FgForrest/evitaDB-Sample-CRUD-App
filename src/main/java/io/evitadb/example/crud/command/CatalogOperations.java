package io.evitadb.example.crud.command;

import io.evitadb.api.EvitaContract;
import io.evitadb.api.requestResponse.schema.CatalogSchemaEditor.CatalogSchemaBuilder;
import io.evitadb.example.crud.context.EvitaSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.ComponentFlow.ComponentFlowResult;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Class contains operations on catalog level.
 *
 * @author Jan Novotný (novotny@fg.cz), FG Forrest a.s. (c) 2023
 */
@ShellComponent
@ShellCommandGroup("Catalog operations")
public class CatalogOperations {
	@Autowired ComponentFlow.Builder flowBuilder;
	@Autowired EvitaContract evita;
	@Autowired EvitaSessionHolder sessionHolder;

	@ShellMethod(key = "create-catalog", value = "defines a new catalog in evitaDB")
	public String createCatalog(
		@ShellOption(defaultValue = ShellOption.NULL) String catalogName
	) {
		if (catalogName == null) {
			catalogName = readCatalogName("Catalog name:");
		}

		if (evita.getCatalogNames().contains(catalogName)) {
			return "Catalog `" + catalogName + "` already exists.";
		} else {
			final CatalogSchemaBuilder catalogSchema = evita.defineCatalog(catalogName);
			return "Catalog `" + catalogSchema.getName() + "` was created.";
		}
	}

	@ShellMethod(key = "open-catalog", value = "opens catalog for work with entities")
	public String openCatalog(
		@ShellOption(defaultValue = ShellOption.NULL) String catalogName
	) {
		if (catalogName == null) {
			catalogName = readCatalogName("Catalog name:");
		}

		if (evita.getCatalogNames().contains(catalogName)) {
			final StringBuilder result = new StringBuilder();
			if (sessionHolder.getSession() != null) {
				if (Objects.equals(catalogName, sessionHolder.getSession().getCatalogName())) {
					return "There is already opened session to catalog `" + catalogName + "`.";
				} else {
					sessionHolder.getSession().close();
					result.append("Existing session to catalog `").append(sessionHolder.getSession().getCatalogName()).append("` terminated.\n");
				}
			}
			sessionHolder.setSession(
				evita.createReadWriteSession(catalogName)
			);
			return result + "Session to catalog `" + catalogName + "` opened.";
		} else {
			return "Catalog `" + catalogName + "` doesn't exist.";
		}
	}

	@ShellMethod(key = "close-catalog", value = "closes opened session to catalog")
	public String closeCatalog() {
		if (sessionHolder.getSession() != null) {
			final String catalogName = sessionHolder.getSession().getCatalogName();
			sessionHolder.getSession().close();
			sessionHolder.setSession(null);
			return "Existing session to catalog" + catalogName + "` terminated.";
		} else {
			return "No session is currently opened.";
		}
	}

	@ShellMethod(key = "delete-catalog", value = "deletes an existing catalog in evitaDB (irreversibly)")
	public String deleteCatalog(
		@ShellOption(defaultValue = ShellOption.NULL) String catalogName
	) {
		if (catalogName == null) {
			catalogName = readCatalogName("Catalog name to drop:");
		}

		if (evita.deleteCatalogIfExists(catalogName)) {
			return "Catalog `" + catalogName + "` was deleted.";
		} else {
			return "Catalog `" + catalogName + "` doesn't exist.";
		}
	}

	@ShellMethod(key = "list-catalogs", value = "lists all available catalogs in evitaDB")
	public String listCatalogs() {
		final StringBuilder output = new StringBuilder();
		int no = 1;
		for (String catalogName : evita.getCatalogNames()) {
			output.append("   ").append(no++).append(". ").append(catalogName).append("\n");
		}
		return output.toString();
	}

	/**
	 * Reads the name of the catalog from the command prompt from the user.
	 */
	@Nonnull
	private String readCatalogName(@Nonnull String inputName) {
		final ComponentFlowResult result = flowBuilder
			.clone()
			.reset()
			.withStringInput("userInput")
			.name(inputName)
			.and()
			.build()
			.run();
		return result.getContext().get("userInput");
	}

}
