/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
	static int cuts = 0, links = 0;
	private int size, marks, numOfTrees;
	private HeapNode min, trees;

	public FibonacciHeap()
	{
		this.size = 0;
		this.marks = 0;
		this.numOfTrees = 0;
		this.min = null;
		this.trees = null;
	}

	/**
	 * public boolean isEmpty()
	 *
	 * precondition: none
	 *
	 * The method returns true if and only if the heap
	 * is empty.
	 *
	 */
	public boolean isEmpty()
	{
		return this.trees == null ? true : false;
	}

	/**
	 * private void update_min(HeapNode node)
	 *
	 * Compares node to the heap's minimum and updates the node to the new minimum if necessary.
	 * If the heap was empty node becomes the new min.
	 */
	private void update_min(HeapNode node)
	{
		if (this.min == null || node.key < this.min.key)
			this.min = node;
	}

	/**
	 * private void addFirst(HeapNode node)
	 *
	 * Adds node as the first element in the heap's tree linked list
	 */
	private void addFirst(HeapNode node)
	{
		this.update_min(node);
		this.numOfTrees++;
		if (this.trees == null) {
			node.next = node;
			node.prev = node;
			this.trees = node;
			return;
		}
		node.next = this.trees;
		node.prev = this.trees.prev;
		node.next.prev = node;
		node.prev.next = node;
		this.trees = node;
	}

	/**
	 * private HeapNode remove(HeapNode node)
	 *
	 * removes node from the heap's trees linked list and updates the pointers
	 * to preserve the linked lists' structure.
	 */
	private HeapNode remove(HeapNode node)
	{
		this.numOfTrees--;
		if (this.numOfTrees == 0) {
			this.trees = null;
			this.min = null;
			return node;
		}
		if (node == this.trees)
			this.trees = this.trees.next;
		node.next.prev = node.prev;
		node.prev.next = node.next;
		node.next = null;
		node.prev = null;
		return node;
	}

	/**
	 * private HeapNode removeFirst()
	 *
	 * removes the first node from the heap's trees linked list.
	 */
	private HeapNode removeFirst()
	{
		HeapNode first = this.trees;

		if (this.trees == null)
			return null;

		this.remove(first);
		return first;
	}

	/**
	 * HeapNode trees()
	 *
	 * returns the first tree in the heaps list (mainly for testing).
	 */
	HeapNode trees()
	{
		return this.trees;
	}

	/**
	 * int numOfTrees()
	 *
	 * returns the number of trees currently in the heap.
	 */
	int numOfTrees()
	{
		return this.numOfTrees;
	}

	/**
	 * private HeapNode insert(HeapNode node)
	 *
	 * inserts node into the heap.
	 */
	private HeapNode insert(HeapNode node)
	{
		this.addFirst(node);
		this.size += 1;
		return node;
	}

	/**
	 * private HeapNode insert(int key, HeapNode value)
	 *
	 * Creates a node (of type HeapNode) which contains the given key and value, and inserts it into the heap.
	 */
	private HeapNode insert(int key, HeapNode value)
	{
		HeapNode node = new HeapNode(key, value);

		return this.insert(node);
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
	 */
	public HeapNode insert(int key)
	{
		HeapNode node = new HeapNode(key);

		return this.insert(node);
	}

	/**
	 * private void consolidate()
	 *
	 * Successive-linking: runs through all the trees in the heap and links them
	 * in order to reach the minimal number of legal trees that can contain all the
	 * heap data.
	 */
	private void consolidate()
	{
		int i, max_rank = 0;
		HeapNode current;
		/* keep O(log(n)) trees after the linkage */
		HeapNode[] targets = new HeapNode[Math.max(0, (int)Math.ceil(2.5 * Math.log(this.size))) + 1];

		if (this.isEmpty())
			return;
		while (!this.isEmpty()) {
			current = this.removeFirst();
			/* handle child of deleted min */
			if (current.parent != null)
				current.parent = null;
			if (current.mark) {
				current.mark = false;
				this.marks--;
			}
			while(targets[current.rank] != null) {
				current = current.link(targets[current.rank]);
				links++;
				targets[current.rank - 1] = null;
			}
			max_rank = Math.max(current.rank, max_rank);
			targets[current.rank] = current;
		}
		for (i = max_rank; i >= 0; i--) {
			if (targets[i] == null)
				continue;
			this.addFirst(targets[i]);
		}
	}

	/**
	 * private static void concatenate(HeapNode first1, HeapNode first2)
	 *
	 * concatenates two HeapNodes linked list in order:
	 * first1->...->last1->first2->...last2
	 */
	private static void concatenate(HeapNode first1, HeapNode first2)
	{
		HeapNode last1 = first1.prev, last2 = first2.prev;

		last1.next = first2;
		last2.next = first1;
		first1.prev = last2;
		first2.prev = last1;
	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */
	public void deleteMin()
	{
		HeapNode min, next;

		if (this.min == null)
			return;
		next = this.min.next;
		min = this.remove(this.min);
		if (this.trees == null)
			this.trees = min.child;
		else if (min.child != null)
			/* Make sure the childs are concatenated where the parent originally was */
			concatenate(next, min.child);
		this.numOfTrees += min.rank;
		this.min = null;
		this.size -= 1;
		this.consolidate();
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal.
	 *
	 */
	public HeapNode findMin()
	{
		return this.min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 * concatenate heap2.trees at the end of this.trees
	 * update new minimum if needed
	 * update number of trees, marks, and size with values of heap2.
	 *
	 */
	public void meld(FibonacciHeap heap2)
	{
		if (heap2.numOfTrees() == 0)
			return;

		if(this.numOfTrees() == 0)
			trees = heap2.trees;

		else
			concatenate(this.trees, heap2.trees);

		HeapNode newMin = (min != null && min.key < heap2.min.key) ?  min: heap2.min;
		update_min(newMin);

		numOfTrees += heap2.numOfTrees;
		marks += heap2.marks;
		size += heap2.size;
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 *
	 */
	public int size()
	{
		return this.size;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap.
	 * iterate trees list to find the largest tree rank.
	 * create array of this size, and then do one more iteration over the trees to create the Counters array.
	 *
	 */
	public int[] countersRep()
	{
		int[] arr;
		int maxRank = 0;
		HeapNode current = this.trees;

		if (this.numOfTrees == 0)
			return new int[0];
		do {
			maxRank = Math.max(maxRank, current.rank);
			current = current.next;
		} while(current != this.trees);
		arr = new int[maxRank + 1];
		do {
			arr[current.rank]++;
			current = current.next;
		} while(current != this.trees);
		return arr;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap.
	 * decrease x's key to smallest in the heap and call deleteMin.
	 *
	 */
	public void delete(HeapNode x)
	{
		decreaseKey(x, x.key - this.min.key + 1);
		deleteMin();
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta.
	 * if x is root then update heap minimum if needed and return.
	 * otherwise, if heap order is violated then call cascadingCut on x.
	 */
	public void decreaseKey(HeapNode x, int delta)
	{

		x.setKey(x.getKey() - delta);

		// if x in a root update min
		if (x.parent == null) {
			update_min(x);
			return;
		}

		// otherwise start cascading cuts from x if heap is not valid
		if (x.parent.getKey() >= x.getKey())
			cascadingCut(x);
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is:
	 * Potential = #trees + 2*#marked
	 * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap.
	 */
	public int potential()
	{
		return numOfTrees() + 2 * marks;
	}


	/**
	 * public void cascadingCut(HeapNode node)
	 *
	 * @param node: first node to cut
	 * precondition: node.parent != null, meaning node is not a root
	 *
	 * starts cascading cuts from node. calls cut on node and its parent.
	 * continues recursively until root is reached or unmarked parent was marked.
	 *
	 */
	public void cascadingCut(HeapNode node) {

		HeapNode parent = node.parent;
		cut(node, parent);

		// continue if parent is not root
		if (parent.parent != null) {
			if (!parent.mark) {
				parent.mark = true;
				marks++;
			}
			else
				cascadingCut(parent);
		}
	}

	/**
	 * public void cut(HeapNode node, HeapNode parent)
	 *
	 * cuts node from parent, unmark if needed and adds to roots list.
	 * update parent child pointer, and node siblings linked list.
	 * increment cuts by 1.
	 *
	 * precondition: parent != null
	 */
	public void cut(HeapNode node, HeapNode parent) {

		// cut node from its parent
		node.parent = null;
		parent.rank -= 1;

		// if node was marked then remove mark
		if (node.mark) {
			node.mark = false;
			marks--;
		}

		// update children linked list
		if (node.next == node)
			parent.child = null;
		else {
			if (parent.child == node)
				parent.child = node.next;
			node.prev.next = node.next;
			node.next.prev = node.prev;
		}

		// add node to roots
		this.addFirst(node);
		update_min(node);

		cuts++;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during the run-time of the program.
	 * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of
	 * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value
	 * in its root.
	 */
	public static int totalLinks()
	{
		return links;
	}

	public static void resetLinks()
	{
		links = 0;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during the run-time of the program.
	 * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods).
	 */
	public static int totalCuts()
	{
		return cuts;
	}

	public static void resetCuts()
	{
		cuts = 0;
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k)
	 *
	 * This static function returns the k minimal elements in a binomial tree H.
	 * The function should run in O(k(logk + deg(H)).
	 */
	public static int[] kMin(FibonacciHeap H, int k)
	{
		HeapNode[] arr = new HeapNode[k];
		HeapNode current;
		FibonacciHeap minimals = new FibonacciHeap();
		int[] result = new int[k];
		int max = 0;

		minimals.insert(H.findMin().getKey(), H.findMin());
		for (int i = 0; i < k; i++) {
			arr[i] = minimals.findMin().getValue();
			result[i] = arr[i].getKey();
			minimals.deleteMin();
			if(arr[i].child == null)
				continue;
			current = arr[i].child;
			do {
				minimals.insert(current.getKey(), current);
				current = current.next;
			} while (current != arr[i].child);
		}
		return result;
	}

	/**
	 * public class HeapNode
	 *
	 * If you wish to implement classes other than FibonacciHeap
	 * (for example HeapNode), do it in this file, not in
	 * another file
	 *
	 */
	public class HeapNode {

		public int key;
		protected int rank;
		protected boolean mark;
		protected HeapNode child, next, prev, parent;
		private HeapNode value;

		public HeapNode(int key)
		{
			this.key = key;
			this.rank = 0;
			this.mark = false;
			this.child = null;
			this.next = null;
			this.prev = null;
			this.parent = null;
			this.value = null;
		}

		private HeapNode(int key, HeapNode value)
		{
			this(key);
			this.value = value;
		}

		public int getKey()
		{
			return this.key;
		}

		private HeapNode getValue()
		{
			return this.value;
		}

		public void setKey(int k)
		{
			this.key = k;
		}

		/**
		 * public HeapNode link(HeapNode other)
		 *
		 * Links this node and other into one tree, returns the new root node.
		 */
		public HeapNode link(HeapNode other)
		{
			HeapNode root, child;

			root = this.key < other.key ? this: other;
			child = this.key < other.key ? other: this;
			child.parent = root;
			if (root.child != null) {
				child.next = root.child;
				child.prev = root.child.prev;
				child.next.prev = child;
				child.prev.next = child;
			} else {
				child.next = child;
				child.prev = child;
			}
			root.child = child;
			root.rank += 1;
			return root;
		}
	}
}
