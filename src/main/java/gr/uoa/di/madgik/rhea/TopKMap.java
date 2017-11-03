package gr.uoa.di.madgik.rhea;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.PriorityQueue;


public class TopKMap<K, V extends Comparable<V>> {

	private int K;
	private PriorityQueue<ComparableSimpleEntry<K, V>> pq;
	private HashMap<K, V> hm;
	private V low;

	public TopKMap() {
		this(100);
		this.pq = new PriorityQueue<ComparableSimpleEntry<K, V>>(100);
		this.hm = new HashMap<K, V>(100);
	}

	public TopKMap(int K) {
		super();
		this.K = K;
		this.pq = new PriorityQueue<ComparableSimpleEntry<K, V>>(this.K);
		this.hm = new HashMap<K, V>(K);
	}

	public boolean add(K key, V value) {
		// if queue has less than K items, simply add
		if(this.pq.size() < K){
			// if key is already used  remove queue entry
			if(this.hm.containsKey(key)){
				ComparableSimpleEntry<K, V> entry = new ComparableSimpleEntry<K, V>(key, this.hm.get(key));
				this.pq.remove(entry);
			}
			ComparableSimpleEntry<K, V> entry = new ComparableSimpleEntry<K, V>(key, value);
			this.pq.add(entry);
			this.hm.put(key, value); // replaces if present
			this.low = this.pq.peek().getValue();
			return true;
		}
		// else (if queue has K items)
		else{
			// if key is already used remove old queue entry, add the new one, and replace hash map value
			if(this.hm.containsKey(key)){
				ComparableSimpleEntry<K, V> entry = new ComparableSimpleEntry<K, V>(key, this.hm.get(key));
				this.pq.remove(entry);
				entry = new ComparableSimpleEntry<K, V>(key, value);
				this.pq.add(entry);
				this.hm.replace(key, value);
				this.low = this.pq.peek().getValue();
				return true;
			}
			// if value is larger than the smallest item, remove smallest and add
			else if (value.compareTo(this.pq.peek().getValue()) > 0){
				ComparableSimpleEntry<K, V> cse = this.pq.poll();
				this.hm.remove(cse.getKey());
				ComparableSimpleEntry<K, V> entry = new ComparableSimpleEntry<K, V>(key, value);
				this.pq.add(entry);
				this.hm.put(key, value);
				this.low = this.pq.peek().getValue();
				return true;
			}
		}
		return false;
	}
	
	
	public ComparableSimpleEntry<K, V> poll(){
		ComparableSimpleEntry<K, V> result = this.pq.poll();
		this.hm.remove(result.getKey());
		this.low = this.pq.peek().getValue();
		return result;
	}
	

	public int size() {
		return pq.size();
	}
	
	public boolean contains(K key, V value) {
		return this.pq.contains(new ComparableSimpleEntry<K, V>(key, value));
	}
	
	public boolean contains(K key){
		return this.hm.containsKey(key);
	}
	
	public V getLow(){
		return (V) ((this.low == null ) ? 0.0 : this.low);
	}
	
	public class ComparableSimpleEntry<E, T extends Comparable<T>> extends SimpleEntry<E, T> implements Comparable {

		private static final long serialVersionUID = -1207375594963870227L;

		private E key;
		
		public ComparableSimpleEntry(E key, T value) {
			super(key, value);
			this.key = key;
		}

		@Override
		public int compareTo(Object o) {
			return this.getValue().compareTo(((ComparableSimpleEntry<E, T>)o).getValue());  
			
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ComparableSimpleEntry other = (ComparableSimpleEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

		private TopKMap getOuterType() {
			return TopKMap.this;
		}
		
		

	}


}
