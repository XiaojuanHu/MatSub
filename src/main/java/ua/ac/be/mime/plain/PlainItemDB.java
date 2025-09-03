package ua.ac.be.mime.plain;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Integer.parseInt;

import java.util.Iterator;
import java.util.Map;

public class PlainItemDB implements Iterable<PlainItem> {

	protected Map<Integer, PlainItem> items;
	protected Map<String, Integer> nameToIDMapping;

	public PlainItemDB() {
		this.items = newHashMap();
		this.nameToIDMapping = newHashMap();
	}

	public PlainItem get(int itemId) {
		PlainItem item = this.items.get(itemId);
		if (item == null) {
			item = newItem(itemId);
			this.items.put(itemId, item);
		}
		return item;
	}

	public PlainItem get(String itemId) {
		try {
			return get(parseInt(itemId));
		} catch (NumberFormatException e) {
			return getByName(itemId);
		}
	}

	public int size() {
		return this.items.size();
	}

	protected PlainItem newItem(int itemId) {
		return new PlainItem(itemId);
	}

	@Override
	public Iterator<PlainItem> iterator() {
		return this.items.values().iterator();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ItemDB: #items=" + items.size() + "\n");
		return buf.toString();
	}

	public PlainItem getByName(String name) {
		if (nameToIDMapping.containsKey(name)) {
			return get(nameToIDMapping.get(name));
		}
		PlainItem item = get(items.size());
		item.setName(name);
		nameToIDMapping.put(name, item.getId());
		return item;
	}
}
