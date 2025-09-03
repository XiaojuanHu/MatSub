package ua.ac.be.mime.plain;

import static de.unibonn.realkd.data.xarf.XarfImport.xarfImport;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.unibonn.realkd.common.base.Identifier;
import de.unibonn.realkd.data.table.DataTable;
import de.unibonn.realkd.data.table.attribute.Attribute;
import de.unibonn.realkd.data.table.attribute.CategoricAttribute;
import ua.ac.be.mime.plain.weighting.PosNegDbInterface;
import ua.ac.be.mime.plain.weighting.PosNegTransactionDb;
import ua.ac.be.mime.plain.weighting.WeightedTransactionDB;

public interface ArffConverter {

	public static class OnlyCategoricalConverter implements ArffConverter {

		public Map<String, Integer> idMapping = new HashMap<String, Integer>();

		@Override
		public TransactionDBInterface convert(String fileName) {
			PlainTransactionDB db = new PlainTransactionDB();
			
			convert(fileName, db);

			return db;
		}

		@Override
		public TransactionDBInterface convertWeighted(String fileName,
				String biasFileName) {
			WeightedTransactionDB db = new WeightedTransactionDB();
			
			convert(fileName, db);

			if (!biasFileName.isEmpty()) {
				db.readAndSetWeights(biasFileName);
			}

			return db;
		}

		@Override
		public PosNegDbInterface convertLabeled(String fileName,
				String attributeLabel, String[] posLabels, boolean keepLabels) {
			PosNegTransactionDb db = new PosNegTransactionDb();
			
			DataTable dataTable = xarfImport(fileName).get();
			
			Optional<? extends Attribute<?>> labelAttributeOpt = dataTable.attribute(Identifier.identifier(attributeLabel));
			if (!labelAttributeOpt.isPresent()) {
				return null;
			}
			
			Attribute<?> labelAttribute = labelAttributeOpt.get();
			
			Set<String> posLabelsSet = new HashSet<String>();
			for (String posLabel : posLabels) {
				posLabelsSet.add(posLabel);
			}
			
			for(int i = 0; i < dataTable.population().size(); i++) {
				int rowIndex = i;
				List<String> items = dataTable.attributes().stream()
						.filter(a -> a instanceof CategoricAttribute && (keepLabels || a != labelAttribute))
						.map(a -> {
							Optional<?> valueOption = a.getValueOption(rowIndex);
							if(valueOption.isPresent()) {
								return a.caption() + "=" + valueOption.get().toString();
							}
							return "";
						})
						.collect(toList());
				
				db.addTransaction(items.toArray(new String[] {}), posLabelsSet.contains(labelAttribute.value(i).toString()));
			}
			
			return db;
		}

		@Override
		public PosNegDbInterface convertLabeled(String fileNamePos,
				String fileNameNeg) {
			PosNegDbInterface db = new PosNegTransactionDb();

			convert(fileNamePos, db, true);
			convert(fileNamePos, db, false);

			return db;
		}
		
		private void convert(String fileName, PlainTransactionDB db) {
			DataTable dataTable = xarfImport(fileName).get();
			
			for(int i = 0; i < dataTable.population().size(); i++) {
				int rowIndex = i;
				List<String> items = dataTable.attributes().stream()
						.filter(a -> a instanceof CategoricAttribute)
						.map(a -> {
							Optional<?> valueOption = a.getValueOption(rowIndex);
							if(valueOption.isPresent()) {
								return a.caption() + "=" + valueOption.get().toString();
							}
							return "";
						})
						.collect(toList());
				
				db.addTransaction(items.toArray(new String[] {}));
			}
		}

		private void convert(String fileName, PosNegDbInterface db,
				boolean isPos) {
			DataTable dataTable = xarfImport(fileName).get();
			
			for(int i = 0; i < dataTable.population().size(); i++) {
				int rowIndex = i;
				List<String> items = dataTable.attributes().stream()
						.filter(a -> a instanceof CategoricAttribute)
						.map(a -> {
							Optional<?> valueOption = a.getValueOption(rowIndex);
							if(valueOption.isPresent()) {
								return a.caption() + "=" + valueOption.get().toString();
							}
							return "";
						})
						.collect(toList());
				
				db.addTransaction(items.toArray(new String[] {}), isPos);
			}
		}

		public static void main(String[] args) {
			String filenamePos = "src/main/resources/data/autos/autos.arff";
			String filenameNeg = "src/main/resources/data/autos/autos.arff";

			PosNegDbInterface db = new OnlyCategoricalConverter().convertLabeled(filenamePos, filenameNeg);
			System.out.println(db);
			
			for(PlainTransaction transaction: db.getTransactions()) {
				System.out.println(transaction);
			}
		}
	}

	public TransactionDBInterface convert(String fileName);

	public TransactionDBInterface convertWeighted(String fileName,
			String biasFileName);

	public PosNegDbInterface convertLabeled(String fileName,
			String labelAttribute, String[] posLabels, boolean keepLabels);

	public PosNegDbInterface convertLabeled(String fileNamePos,
			String fileNameNeg);

}
