package species.search

import org.apache.lucene.search.suggest.tst.TSTAutocomplete;
import org.apache.lucene.search.suggest.tst.TernaryTreeNode;

import species.TaxonomyDefinition;

class Record implements Comparable<Record>, Serializable {
	
	private static final long serialVersionUID = 7526472295622776147L;
	
	Long recoId
	
	//normalized name for searched string
	String originalName;
	//if name is synonym or common name then storing normalized form of accepted name
	String acceptedName;
	//if name is common name of synonym then storing synonym normalized form
	String synName;
	
	// for ordering
	int wt = 0;
	
	//additional display info
	Long speciesId;
	String icon;
	boolean isScientificName;
	//if record represent common name then languageId is useful
	Long languageId;
	
	
	int compareTo(Record r) {
		if(this.equals(r))return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return recoId.hashCode()
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
		
		return (recoId.hashCode() == other.recoId.hashCode())
	}
	
	public String toString() {
		return this.class.canonicalName + ":" + recoId
	}
	
}
