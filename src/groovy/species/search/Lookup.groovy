package species.search

import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.lucene.util.PriorityQueue;

abstract class Lookup <E>{
	
	/**
	 * 
	 * @author sravanthi
	 *
	 */
	public static final class LookupResult implements Comparable<LookupResult> {
		public final String key;
		public final Object value;

		public LookupResult(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return key + "/" + value;
		}

		/** Compare alphabetically. */
		public int compareTo(LookupResult o) {
			return this.key.compareTo(o.key);
		}
	}

	/**
	 * Priority Q to sort on the wt
	 * @author sravanthi
	 *
	 */
	public static final class LookupPriorityQueue extends PriorityQueue<LookupResult> {

		public LookupPriorityQueue(int size) {
			super(size);
		}

		@Override
		protected boolean lessThan(LookupResult a, LookupResult b) {
			return a.value.wt < b.value.wt;
		}

		public LookupResult[] getResults() {
			int size = size();
			LookupResult[] res = new LookupResult[size];
			for (int i = size - 1; i >= 0; i--) {
				res[i] = pop();
			}
			return res;
		}
	}

	abstract boolean add(String key, Object value);
		
	abstract List<LookupResult> lookup(String key, boolean onlyMorePopular, int num, String nameFilter);
	
	abstract List<Object> get(String key);
	
}

