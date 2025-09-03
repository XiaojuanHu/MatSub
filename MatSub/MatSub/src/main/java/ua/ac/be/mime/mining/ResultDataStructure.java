package ua.ac.be.mime.mining;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ua.ac.be.mime.plain.PlainItem;
import ua.ac.be.mime.plain.PlainItemDB;
import ua.ac.be.mime.plain.PlainItemSet;

/**
 * This interface represents a basic datastructure that is used to keep data
 * that is the result of a mining algorithm. This datastructure is not sorted.
 * 
 * @author Sandy Moens
 */
public interface ResultDataStructure {

	public static class PlainListDataFactory implements DataStructureFactory {
		@Override
		public ResultDataStructure newDataStructure() {
			return new PlainList();
		}
	}

	public static class PlainListWithTimesDataFactory implements
			DataStructureFactory {
		@Override
		public ResultDataStructure newDataStructure() {
			return new PlainListWithTimes();
		}
	}

	public static class IdsListWithTimesDataFactory implements
			DataStructureFactory {

		private PlainItemDB itemDb;

		public void setItemDb(PlainItemDB itemDb) {
			this.itemDb = itemDb;
		}

		@Override
		public ResultDataStructure newDataStructure() {
			IdsListWithTimes data = new IdsListWithTimes();
			data.setItemDb(itemDb);
			return data;
		}
	}

	public static class IdsListDataFactory implements DataStructureFactory {

		private PlainItemDB itemDb;

		public void setItemDb(PlainItemDB itemDb) {
			this.itemDb = itemDb;
		}

		@Override
		public ResultDataStructure newDataStructure() {
			IdsList data = new IdsList();
			data.setItemDb(itemDb);
			return data;
		}
	}

	public static interface DataStructureFactory {
		public ResultDataStructure newDataStructure();
	}

	public static class PlainListWithTimes extends PlainList {
		List<Long> times = newArrayList();

		private final long startTime = System.currentTimeMillis();

		@Override
		public boolean add(PlainItemSet itemSet) {
			times.add(System.currentTimeMillis() - startTime);
			return super.add(itemSet);
		}

		public List<Long> getTimes() {
			return times;
		}
	}

	public static class PlainList implements ResultDataStructure {

		private final List<PlainItemSet> sets = newArrayList();

		@Override
		public boolean add(PlainItemSet itemSet) {
			return sets.add(itemSet);
		}

		@Override
		public int size() {
			return sets.size();
		}

		@Override
		public Iterator<PlainItemSet> iterator() {
			return sets.iterator();
		}

		@Override
		public boolean add(Collection<PlainItem> itemSet, int support) {
			return add(new PlainItemSet(itemSet));
		}

		public List<PlainItemSet> getList() {
			return sets;
		}

		@Override
		public void writeToFile(String outputFile) {
			try {
				PrintWriter writer = new PrintWriter(outputFile);
				int maxBufSize = 1024, bufSize = 0;
				StringBuffer buf = new StringBuffer(maxBufSize);
				for (PlainItemSet set : sets) {
					appendSetToBuffer(buf, set);
					if (bufSize / maxBufSize == 1) {
						bufSize = 0;
						writer.write(buf.toString());
					}
				}
				if (buf.length() != 0) {
					writer.write(buf.toString());
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		private void appendSetToBuffer(StringBuffer buf, PlainItemSet set) {
			Iterator<PlainItem> it = set.iterator();
			if (it.hasNext()) {
				buf.append(it.next().getId());
				while (it.hasNext()) {
					buf.append(" " + it.next().getId());
				}
			}
			buf.append("\n");
		}
	}

	public static class IdsListWithTimes extends IdsList {
		List<Long> times = newArrayList();

		private final long startTime = System.currentTimeMillis();

		@Override
		public boolean add(PlainItemSet itemSet) {
			times.add(System.currentTimeMillis() - startTime);
			return super.add(itemSet);
		}

		public List<Long> getTimes() {
			return times;
		}
	}

	public static class IdsList implements ResultDataStructure {

		private PlainItemDB itemDb;
		private final List<HashSet<Integer>> sets = newArrayList();

		private static HashSet<Integer> getSet(Iterable<PlainItem> items) {
			HashSet<Integer> set = newHashSet();
			for (PlainItem item : items) {
				set.add(item.getId());
			}
			return set;
		}

		public void setItemDb(PlainItemDB itemDb) {
			this.itemDb = itemDb;
		}

		@Override
		public boolean add(PlainItemSet itemSet) {
			return sets.add(getSet(itemSet));
		}

		@Override
		public boolean add(Collection<PlainItem> itemSet, int support) {
			return sets.add(getSet(itemSet));
		}

		@Override
		public int size() {
			return sets.size();
		}

		@Override
		public Iterator<PlainItemSet> iterator() {
			if (itemDb == null) {
				return null;
			}
			return new Iterator<PlainItemSet>() {
				private final Iterator<HashSet<Integer>> it = sets.iterator();

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public PlainItemSet next() {
					if (!it.hasNext()) {
						return null;
					}
					PlainItemSet itemSet = new PlainItemSet();
					HashSet<Integer> ints = it.next();
					for (int id : ints) {
						itemSet.add(itemDb.get(id));
					}
					return itemSet;
				}

				@Override
				public void remove() {
					sets.remove(it);
				}
			};
		}

		public Iterator<HashSet<Integer>> integersIterator() {
			return sets.iterator();
		}

		@Override
		public void writeToFile(String outputFile) {
			try {
				PrintWriter writer = new PrintWriter(outputFile);
				int maxBufSize = 1024, bufSize = 0;
				StringBuffer buf = new StringBuffer();
				for (HashSet<Integer> set : sets) {
					appendSetToBuffer(buf, set);
					if (bufSize / maxBufSize == 1) {
						bufSize = 0;
						writer.write(buf.toString());
					}
				}
				if (buf.length() != 0) {
					writer.write(buf.toString());
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		private void appendSetToBuffer(StringBuffer buf, HashSet<Integer> set) {
			Iterator<Integer> it = set.iterator();
			if (it.hasNext()) {
				buf.append(it.next());
				while (it.hasNext()) {
					buf.append(" " + it.next());
				}
			}
			buf.append("\n");
		}
	}

	public boolean add(PlainItemSet itemSet);

	public boolean add(Collection<PlainItem> set, int support);

	public int size();

	public Iterator<PlainItemSet> iterator();

	public void writeToFile(String outputFile);
}
