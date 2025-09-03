package de.unibonn.realkd.patterns;

import java.util.List;

import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;

/**
 * Pattern descriptor that refers to a table via referencing a subset of its
 * attributes.
 * 
 * @author Mario Boley
 * 
 * @author Sandy Moens
 * 
 * @since 0.2.0
 * 
 * @version 0.6.0
 *
 */
public interface TableSubspaceDescriptor extends PatternDescriptor {

	public DataTable table();

	public List<Attribute<?>> getReferencedAttributes();

}
