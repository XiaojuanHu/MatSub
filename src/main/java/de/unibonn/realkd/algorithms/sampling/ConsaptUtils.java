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

package de.unibonn.realkd.algorithms.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import de.unibonn.realkd.data.propositions.Proposition;
import de.unibonn.realkd.data.propositions.PropositionalContext;
import ua.ac.be.mime.plain.PlainTransactionDB;
import ua.ac.be.mime.plain.weighting.PosNegTransactionDb;

/**
 * Provides factory methods for obtaining Consapt data objects from realKD data
 * objects.
 * 
 * @author Sandy Moens
 * 
 * @since 0.1.0
 * 
 * @version 0.6.0
 *
 */
public class ConsaptUtils {

	public static PlainTransactionDB createTransactionDbFromPropositionalLogic(PropositionalContext propositionalLogic) {
		return createTransactionDbFromPropositionalLogic(propositionalLogic, p -> true);
	}

	public static PlainTransactionDB createTransactionDbFromPropositionalLogic(PropositionalContext propositionalLogic,
			Predicate<Proposition> filter) {
		PlainTransactionDB transactionDB = new PlainTransactionDB();

		for (int i = 0; i < propositionalLogic.population().size(); i++) {
			List<String> transactionList = new ArrayList<>();
			for (int j = 0; j < propositionalLogic.propositions().size(); j++) {
				Proposition proposition = propositionalLogic.propositions().get(j);
				if (filter.test(proposition) && proposition.holdsFor(i)) {
					transactionList.add(String.valueOf(j));
				}
			}
			String[] transaction = new String[transactionList.size()];
			transactionList.toArray(transaction);
			transactionDB.addTransaction(transaction);
		}

		return transactionDB;
	}

	public static PosNegTransactionDb createPosNegDb(PropositionalContext propLogic, IntPredicate isPositive,
			Predicate<Proposition> filter) {
		PosNegTransactionDb db = new PosNegTransactionDb();
		for (int i = 0; i < propLogic.population().size(); i++) {

			List<String> transactionList = new ArrayList<>();
			for (int j = 0; j < propLogic.propositions().size(); j++) {
				Proposition proposition = propLogic.propositions().get(j);
				if (filter.test(proposition) && proposition.holdsFor(i)) {
					transactionList.add(String.valueOf(j));
				}
			}
			if (isPositive.test(i)) {
				db.addTransaction(transactionList.toArray(new String[] {}), true);
			} else {
				db.addTransaction(transactionList.toArray(new String[] {}), false);
			}
		}
		return db;
	}

}
