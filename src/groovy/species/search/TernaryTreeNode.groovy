package species.search

/**
* The class creates a TST node.
*/

class TernaryTreeNode implements Serializable {
	
	private static final long serialVersionUID = 7526472295622776148L;
	
	  /** the character stored by a node. */
		char splitchar;
		
		/** a reference object to the node containing character smaller than this node's character. */
		TernaryTreeNode loKid;
		/**
		 *  a reference object to the node containing character next to this node's character as
		 *  occurring in the inserted token.
		 */
		TernaryTreeNode eqKid;
		/** a reference object to the node containing character higher than this node's character. */
		TernaryTreeNode hiKid;
		/**
		 * used by leaf nodes to store the complete tokens to be added to suggest list while
		 * auto-completing the prefix.
		 */
		String token;
		Object val;
			
}
