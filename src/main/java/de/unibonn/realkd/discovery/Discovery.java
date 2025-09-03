package de.unibonn.realkd.discovery;

import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.common.workspace.Entity;
import de.unibonn.realkd.common.workspace.HasSerialForm;
import de.unibonn.realkd.common.workspace.IdentifiableSerialForm;
import de.unibonn.realkd.common.workspace.SerialForm;
import de.unibonn.realkd.common.workspace.Workspace;
import de.unibonn.realkd.data.propositions.AttributeBasedProposition;
import de.unibonn.realkd.patterns.Pattern;
import de.unibonn.realkd.patterns.subgroups.Subgroup;

/**
 * 
 * @author Bo Kang
 * 
 * @author Mario Boley
 * 
 * @since 0.1.0
 * 
 * @version 0.5.0
 * 
 */
public class Discovery implements Entity, HasSerialForm<Discovery> {

	private static class DiscoverySerialForm implements IdentifiableSerialForm<Discovery> {

		@JsonProperty("number")
		private final int number;

		@JsonProperty("content")
		private final SerialForm<? extends Pattern<?>> content;

		@JsonProperty("identifier")
		private final Identifier identifier;

		@JsonProperty("name")
		private final String name;

		@JsonProperty("description")
		private final String description;

		private DiscoverySerialForm(@JsonProperty("identifier") Identifier identifier, @JsonProperty("name") String name,
				@JsonProperty("description") String description, @JsonProperty("number") int number,
				@JsonProperty("content") SerialForm<? extends Pattern<?>> content) {
			this.number = number;
			this.content = content;
			this.identifier = identifier;
			this.name = name;
			this.description = description;
		}

		@Override
		public Discovery build(Workspace workspace) {
			return new Discovery(identifier, name, description, number, content.build(workspace));
		}

		@Override
		public Identifier identifier() {
			return identifier;
		}

		@Override
		public Collection<Identifier> dependencyIds() {
			return content.dependencyIds();
		}

	}

	private final int number;

	private final Identifier identifier;

	private final Pattern<?> pattern;

	private String description = "";

	private String name = "";

	public Discovery(Identifier identifier, String name, String description, int number, Pattern<?> pattern) {
		this.number = number;
		this.pattern = pattern;
		this.identifier = identifier;
		this.description = description;
		this.name = name;
	}

	public Pattern<?> content() {
		return pattern;
	}

	/**
	 * 
	 * @return the number of the discovery within discovery process
	 */
	public int number() {
		return number;
	}

	private String extensionString() {
		if (pattern.descriptor() instanceof Subgroup) {
			return ",\n extension: " + ((Subgroup<?>) pattern.descriptor()).extensionDescriptor().supportSet();
		}
		return "";
	}

	private String descriptorDetailsString() {
		if (pattern.descriptor() instanceof Subgroup) {
			return ",\n descriptor details: " + ((Subgroup<?>) pattern.descriptor()).extensionDescriptor().elements()
					.stream().filter(p -> p instanceof AttributeBasedProposition<?>)
					.map(p -> (AttributeBasedProposition<?>) p)
					.map(p -> p.attribute().caption() + "=" + p.constraint().description())
					.collect(Collectors.toList());
		}
		return "";
	}

	@Override
	public String toString() {
		return "{pattern: " + pattern.toString() + descriptorDetailsString() + extensionString() + ",\n annotation: \""
				+ description() + "}";
	}

	public void description(String description) {
		this.description = description;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Identifier identifier() {
		return identifier;
	}

	@Override
	public String caption() {
		return name;
	}

	@Override
	public IdentifiableSerialForm<? extends Discovery> serialForm() {
		return new DiscoverySerialForm(identifier, name, description, number, pattern.serialForm());
	}

}
