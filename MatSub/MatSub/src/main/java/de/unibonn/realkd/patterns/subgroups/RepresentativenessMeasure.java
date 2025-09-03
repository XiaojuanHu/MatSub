package de.unibonn.realkd.patterns.subgroups;

import de.unibonn.realkd.common.base.Identifiable;
import de.unibonn.realkd.common.measures.Measure;
import de.unibonn.realkd.common.measures.Measurement;
import de.unibonn.realkd.patterns.models.Model;

public interface RepresentativenessMeasure<C extends Model> extends Measure, Identifiable {
	
	public Measurement measurement(ControlledSubgroup<?,C> subgroup);
	
}
