package species.search

import org.apache.lucene.search.suggest.tst.TSTAutocomplete;
import org.apache.lucene.search.suggest.tst.TernaryTreeNode;

import species.TaxonomyDefinition;

class Record implements Comparable<Record>, Serializable {
	
	private static final long serialVersionUID = 7526472295622776147L;
	
	String name;
	String originalName;
	String icon;
	int wt = 0;
	String canonicalForm;
	
	int compareTo(Record r) {
		if(this.equals(r)) return 0;
		return this.name.compareTo(r.name)
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((originalName == null) ? 0 : originalName.hashCode());
		result = prime * result + wt;
		result = prime * result
				+ ((canonicalForm == null) ? 0 : canonicalForm.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this.is(obj)) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Record)) {
			return false;
		}
		Record other = (Record) obj;
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (originalName == null) {
			if (other.originalName != null) {
				return false;
			}
		} else if (!originalName.equals(other.originalName)) {
			return false;
		}
		if (wt != other.wt) {
			return false;
		}
		if (canonicalForm == null) {
			if (other.canonicalForm != null) {
				return false;
			}
		} else if (!canonicalForm.equals(other.canonicalForm)) {
			return false;
		}
		return true;
	}
	
	
	
}
