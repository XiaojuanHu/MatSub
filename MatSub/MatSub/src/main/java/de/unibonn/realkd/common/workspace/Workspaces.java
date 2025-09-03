/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-16 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package de.unibonn.realkd.common.workspace;

import static de.unibonn.realkd.common.JsonSerialization.deserialization;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.unibonn.realkd.common.JsonSerialization;
import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;

/**
 * Provides static factory methods for the creation of workspaces.
 * 
 * @author Mario Boley
 * 
 * @since 0.1.2
 * 
 * @version 0.3.0
 *
 */
public class Workspaces {

	private static Logger LOGGER = Logger.getLogger(Workspace.class.getName());

	/**
	 * Creates a new empty workspace with no persistent storage of entities.
	 * 
	 * @return a new empty workspace
	 * 
	 */
	public static Workspace workspace() {
		return new DefaultWorkspace();
	}

	/**
	 * Creates a new workspace backed by a specified path that contains all entities
	 * stored at this path. All entities added to workspace afterwards will also be
	 * persisted at path.
	 * 
	 * @param path
	 *            the path under which entities are persisted
	 * @return workspace backed by path
	 */
	public static Workspace workspace(Path path) {
		WorkspaceWithFileBackups workspace = new WorkspaceWithFileBackups(path);
		return workspace;
	}

	private static class DefaultWorkspace implements Workspace {

		private final Map<Identifier, Identifiable> entities = new HashMap<>();

		private final Map<Identifier, Future<Identifiable>> futureEntities = new HashMap<>();

		@Override
		public void add(Identifiable entity) {
			if (entities.containsKey(entity.identifier()) || futureEntities.containsKey(entity.identifier())) {
				throw new IllegalArgumentException(
						"workspace already contains entity with id '" + entity.identifier() + "'");
			}
			entities.put(entity.identifier(), entity);
			entity.dependencies().forEach(d -> {
				if (!this.contains(d.identifier()))
					add(d);
			});
		}

		@Override
		public void overwrite(Entity entity) {
			if (futureEntities.containsKey(entity.identifier())) {
				throw new IllegalArgumentException(
						"workspace is already waiting for future entity with id '" + entity.identifier() + "'");
			}
			entities.put(entity.identifier(), entity);
			entity.dependencies().forEach(d -> {
				if (!this.contains(d.identifier()))
					add(d);
			});
		}

		@Override
		public void deserializeAll(Collection<? extends IdentifiableSerialForm<?>> serialForms) {
			Consumer<IdentifiableSerialForm<?>> action = f -> {
				this.deserialize(f, Sets.difference(ImmutableSet.copyOf(serialForms), ImmutableSet.of(f)));
			};
			serialForms.forEach(action);
		}

		private void deserialize(IdentifiableSerialForm<?> form, Set<IdentifiableSerialForm<?>> others) {
			if (this.contains(form.identifier())) {
				LOGGER.info("Skipping deserialization of '" + form.identifier()
						+ "' (entity with same id already in workspace)");
				return;
			}
			for (Identifier dependency : form.dependencyIds()) {
				if (!this.contains(dependency)) {
					Optional<IdentifiableSerialForm<?>> serialFormOfDependency = others.stream()
							.filter(o -> o.identifier().equals(dependency)).findFirst();
					if (!serialFormOfDependency.isPresent()) {
						LOGGER.warning("Cannot deserialize '" + form.identifier() + "' because of missing dependency '"
								+ dependency + "'");
						return;
					}
					deserialize(serialFormOfDependency.get(),
							Sets.difference(others, ImmutableSet.of(serialFormOfDependency)));
				}
			}
			Identifiable entity = form.build(this);
			this.add(entity);
			// entities.put(entity.identifier(), entity);
			LOGGER.info("Restored entity " + entity.identifier());
		}

		@Override
		public <T extends Identifiable> List<Identifiable> entitiesOfType(Class<T> clazz) {
			List<Identifiable> result = new ArrayList<Identifiable>();
			for (Object object : entities.values()) {
				if (clazz.isInstance(object)) {
					result.add(clazz.cast(object));
				}
			}
			return result;
		}

		@Override
		public List<DataTable> datatables() {
			List<DataTable> result = new ArrayList<>();
			for (Object dataTable : entitiesOfType(DataTable.class)) {
				result.add((DataTable) dataTable);
			}
			return result;
		}

		@Override
		public List<PropositionalContext> propositionalContexts() {
			List<PropositionalContext> result = new ArrayList<>();
			for (Object propLogic : entitiesOfType(PropositionalContext.class)) {
				result.add((PropositionalContext) propLogic);
			}
			return result;
		}

		@Override
		public boolean contains(Identifier id) {
			return entities.containsKey(id) || futureEntities.containsKey(id);
		}

		@Override
		public boolean contains(Identifier id, Class<? extends Identifiable> type) {
			return contains(id) && type.isInstance(get(id));
		}

		// TODO: return type should be changed to validation (there are three
		// different kinds of errors: no such entity, entity computation failed,
		// entity computation interrupted)
		@Override
		public Identifiable get(Identifier id) {
			if (entities.containsKey(id)) {
				return entities.get(id);
			} else if (futureEntities.containsKey(id)) {
				Future<Identifiable> future = futureEntities.get(id);
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new IllegalArgumentException(e);
				}
			}
			throw new IllegalArgumentException("No enitity with id '" + id + "'");
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Identifiable> Optional<T> get(Identifier id, Class<T> type) {
			if (contains(id, type)) {
				return (Optional<T>) Optional.of(get(id));
			} else {
				return Optional.empty();
			}
		}

		@Override
		public Future<Identifiable> getAsFuture(Identifier id) {
			if (entities.containsKey(id)) {
				Identifiable result = entities.get(id);
				return new FutureTask<>(() -> result);
			} else if (futureEntities.containsKey(id)) {
				return futureEntities.get(id);
			}
			throw new IllegalArgumentException("No enitity with id '" + id + "'");
		}

		@Override
		public void addFuture(Identifier id, Future<Identifiable> futureEntity) {
			if (entities.containsKey(id) || futureEntities.containsKey(id)) {
				throw new IllegalArgumentException("workspace already contains entity with id '" + id + "'");
			}
			futureEntities.put(id, futureEntity);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(() -> {
				try {
					LOGGER.info("thread started waiting for computation of entity " + id);
					Identifiable entity = futureEntity.get();
					if (!id.equals(entity.identifier())) {
						throw new IllegalArgumentException("id of computed entity '" + entity.identifier()
								+ "' does not match reserved id '" + id + "'");
					}
					LOGGER.info("adding entity " + id);

					// TODO does 'this' really refer to the outer workspace
					// object here?
					synchronized (this) {
						futureEntities.remove(id);
						this.add(entity);
					}
				} catch (ExecutionException | InterruptedException e) {
					LOGGER.warning(() -> e.getMessage());
					LOGGER.info("removing future entity " + id);
					futureEntities.remove(id);
					// no entity to add, future will still be returned; for
					// non-future aware clients, get will throw illegal
					// argument exception
				}
			});
			executor.shutdown();
		}

		@Override
		public Set<Identifier> ids() {
			return Sets.union(entities.keySet(), futureEntities.keySet());
		}

	}

	private final static class WorkspaceWithFileBackups extends DefaultWorkspace {

		private final Path path;

		private WorkspaceWithFileBackups(Path folder) {
			this.path = folder;
			List<IdentifiableSerialForm<?>> serialForms = readSerialForms();
			deserializeAll(serialForms);
		}

		private List<IdentifiableSerialForm<?>> readSerialForms() {
			List<IdentifiableSerialForm<?>> serialForms = new ArrayList<>();
			try {
				List<Path> jsonFiles = Files.list(path).filter(p -> p.toString().endsWith(".jrke")).collect(toList());
				for (Path file : jsonFiles) {
					try (BufferedReader reader = Files.newBufferedReader(file)) {
						serialForms.add(deserialization(reader, IdentifiableSerialForm.class));
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("could not read directory content");
			}
			return serialForms;
		}

		@Override
		public void add(Identifiable entity) {
			super.add(entity);
			storeInFile(entity);
		}

		public void overwrite(Entity entity) {
			super.overwrite(entity);
			storeInFile(entity);
		}

		private void storeInFile(Identifiable value) {
			if (value instanceof HasSerialForm) {
				Path backupPath = path.resolve(value.identifier() + ".jrke");
				LOGGER.info("writing backup: "+value.identifier());
				try (BufferedWriter writer = Files.newBufferedWriter(backupPath, StandardCharsets.UTF_8)) {
					SerialForm<?> serialForm = ((HasSerialForm<?>) value).serialForm();
					JsonSerialization.serialize(writer, serialForm);
					writer.close();
				} catch (IOException e) {
					LOGGER.severe(e.getMessage());
				}
			}
		}

	}

}
