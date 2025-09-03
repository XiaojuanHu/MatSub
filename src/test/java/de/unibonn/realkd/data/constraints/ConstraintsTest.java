/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
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
package de.unibonn.realkd.data.constraints;

import static de.unibonn.realkd.data.constraints.Constraints.equalTo;
import static de.unibonn.realkd.data.constraints.Constraints.greaterOrEquals;
import static de.unibonn.realkd.data.constraints.Constraints.greaterThan;
import static de.unibonn.realkd.data.constraints.Constraints.inClosedInterval;
import static de.unibonn.realkd.data.constraints.Constraints.lessOrEquals;
import static de.unibonn.realkd.data.constraints.Constraints.lessThan;
import static de.unibonn.realkd.data.constraints.Constraints.namedConstraint;
import static java.util.Comparator.naturalOrder;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;

import de.unibonn.realkd.common.testing.JsonSerializationTesting;
import de.unibonn.realkd.data.table.attribute.FiniteOrder;

/**
 * @author Mario Boley
 * 
 * @since 0.6.0
 * 
 * @version 0.6.0
 *
 */
public class ConstraintsTest {

	@Test
	public void equalsToConstraintConsistencyTest() {
		new EqualsTester().addEqualityGroup(equalTo("foo"), equalTo("foo")).addEqualityGroup(equalTo("ba"))
				.addEqualityGroup(equalTo(3.5), equalTo(3.5)).addEqualityGroup(equalTo(2.0))
				.addEqualityGroup(greaterThan(3.5), greaterThan(3.5))
				.addEqualityGroup(greaterThan("foo", naturalOrder()), greaterThan("foo", naturalOrder()))
				.addEqualityGroup(lessThan(3.5), lessThan(3.5)).addEqualityGroup(lessOrEquals(3.5), lessOrEquals(3.5))
				.addEqualityGroup(greaterOrEquals(3.5), greaterOrEquals(3.5))
				.addEqualityGroup(inClosedInterval(2.0, 3.5), inClosedInterval(2.0, 3.5))
				.addEqualityGroup(namedConstraint(greaterOrEquals(3.5), ">=3.5", "greater or equals 3.5"),
						namedConstraint(greaterOrEquals(3.5), ">=3.5", "greater or equals 3.5"))
				.testEquals();
	}

	@Test
	public void serializationTest() throws IOException {
		JsonSerializationTesting.testJsonSerialization(equalTo("foo"), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(greaterThan("foo", naturalOrder()), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(greaterThan(-1), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(greaterThan(-1, "somewhat low"), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(lessThan(-13.5), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(lessThan(-13.5, "low"), Constraint.class);
	}

	@Test
	public void serializationTestWithNonStandardComparator() throws IOException {
		FiniteOrder comparator = new FiniteOrder(ImmutableList.of("a little, a bit, some more, a lot"));
		JsonSerializationTesting.testJsonSerialization(lessThan("someMore", comparator), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(greaterThan("a bit", comparator), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(lessOrEquals("someMore", comparator), Constraint.class);
		JsonSerializationTesting.testJsonSerialization(greaterOrEquals("a bit", comparator), Constraint.class);
	}

}
