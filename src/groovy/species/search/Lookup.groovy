package species.search

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.apache.lucene.util.PriorityQueue;

public class Lookup<E> {

	TernaryTreeNode root;
	TSTAutocomplete autocomplete = new TSTAutocomplete();

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

	public static final class LookupPriorityQueue extends PriorityQueue<LookupResult> {

		public LookupPriorityQueue(int size) {
			initialize(size);
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

	public boolean add(String key, Object value) {
		if(key) {
			root = autocomplete.insert(root, key.trim(), value, 0);
			return true;
		} else
		return false;
	}

	public boolean remove(String key) {
		autocomplete.insert(root, key, null, 0);
		return false;
	}

	public List<LookupResult> lookup(String key, boolean onlyMorePopular, int num) {
		List<TernaryTreeNode> list = autocomplete.prefixCompletion(root, key, 0);

		List<LookupResult> res = new ArrayList<LookupResult>();
		if (list == null || list.size() == 0) {
			return res;
		}

		int maxCnt = Math.min(num, list.size());
		if (onlyMorePopular) {
			LookupPriorityQueue queue = new LookupPriorityQueue(num);
			for (TernaryTreeNode ttn : list) {
				queue.insertWithOverflow(new LookupResult(ttn.token, ttn.val));
			}
			for (LookupResult lr : queue.getResults()) {
				res.add(lr);
			}
		} else {
			for (int i = 0; i < maxCnt; i++) {
				TernaryTreeNode ttn = list.get(i);
				res.add(new LookupResult(ttn.token, ttn.val));
			}
		}
		return res;
	}


	public static final String FILENAME = "tstLookup.dat";

	private static final byte LO_KID = 0x01;
	private static final byte EQ_KID = 0x02;
	private static final byte HI_KID = 0x04;
	private static final byte HAS_TOKEN = 0x08;
	private static final byte HAS_VALUE = 0x10;

	public synchronized boolean load(File storeDir) throws IOException {
		File data = new File(storeDir, FILENAME);
		if (!data.exists() || !data.canRead()) {
			return false;
		}
		data.withObjectInputStream(getClass().classLoader){
			ois ->
			root = ois.readObject( )
		}
		return true;
	}

	public synchronized boolean store(File storeDir) throws IOException {
		if (!storeDir.exists() || !storeDir.isDirectory() || !storeDir.canWrite()) {
			return false;
		}
		File data = new File(storeDir, FILENAME);
		data.withObjectOutputStream { oos ->
			oos.writeObject(root);
		}
		return true;
	}
}

