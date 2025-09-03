/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 University of Bonn
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
 */

package de.unibonn.realkd.common.workspace;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import de.unibonn.realkd.data.table.DataTable;

/**
 * <p>
 * Aggregation of entities that can be retrieved via their unique identifier.
 * This way, parameters can be set by string that can be resolved to domain
 * objects through a workspace.
 * </p>
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.4.1
 * 
 */
public interface Workspace {

	/**
	 * Adds a data artifact to workspace. Throws an IllegalArgumentException when
	 * artifact with same identifier already present.
	 * 
	 * @param entity
	 */
	public void add(Identifiable entity);

	public default void addAll(Identifiable... entities) {
		for (Identifiable entity : entities) {
			add(entity);
		}
	}

	/**
	 * Tries to deserialize and add all entities given by list of serial forms in an
	 * order corresponding to the declared dependencies of the serial forms.
	 * 
	 * @param serialForms
	 * 
	 * @since 0.4.1
	 */
	public void deserializeAll(Collection<? extends IdentifiableSerialForm<?>> serialForms);

	/**
	 * Adds an entity to workspace; potentially overwriting a previous entity with
	 * the same id.
	 * 
	 * @param entity
	 */
	public void overwrite(Entity entity);

	/**
	 * Checks whether a data artifact with specified identifier is contained in the
	 * workspace.
	 * 
	 * @param id
	 *            the identifier to be checked
	 * @return true if and only if artifact with identifier present
	 */
	public boolean contains(Identifier id);

	/**
	 * Checks whether a data artifact with specified identifier of a specified
	 * (Java) type is contained in the workspace.
	 * 
	 * @param id
	 *            the identifier to be checked
	 * @param type
	 *            the type to be checked
	 * @return true if and only if artifact of specified type with identifier is
	 *         present
	 */
	public boolean contains(Identifier id, Class<? extends Identifiable> type);

	/**
	 * Retrieves artifact with specified identifier or throws
	 * IllegalArgumentException of no artifact with identifier present.
	 * 
	 * @param id
	 *            the identifier of artifact to be retrieved
	 * @return data artifact with specified identifies
	 */
	public Identifiable get(Identifier id);

	/**
	 * Clients that are aware of asynchronous computations can request an entity as
	 * a future. If computation is not finished, they can offer to not block their
	 * execution thread.
	 * 
	 * @param id
	 * @return
	 */
	public Future<Identifiable> getAsFuture(Identifier id);

	/**
	 * Allows to add an entity that is computed asynchronously. If client requests
	 * entity of provided id via {@link #get(Identifier)}, it blocks until computation
	 * is finished, and if it requests via {@link #getAsFuture(Identifier)} the future
	 * will be returned as is.
	 * 
	 * @param id
	 * @param entity
	 */
	public void addFuture(Identifier id, Future<Identifiable> entity);

	/**
	 * Retrieves entity with specified identifier and type, if a matching entity
	 * exists in workspace.
	 * 
	 * @param id
	 *            the identifier of the requested entity
	 * @param type
	 *            the type of the requested entity
	 * @return Optional containing requested entity of found or empty optional
	 *         otherwise
	 */
	public <T extends Identifiable> Optional<T> get(Identifier id, Class<T> type);

	public List<DataTable> datatables();

	public List<PropositionalContext> propositionalContexts();

	public <T extends Identifiable> List<Identifiable> entitiesOfType(Class<T> clazz);

	public Set<Identifier> ids();

}
