package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Comparator;
import java.util.function.Function;

public class Sorting {
	
	public static enum Direction {
		ASC,
		DESC
	}
	
	public static <T> Comparator<T> orderByAsc(Function<T, Integer> function) {
		return orderBy(function, Direction.ASC);
	}
	
	public static <T> Comparator<T> orderByDesc(Function<T, Integer> function) {
		return orderBy(function, Direction.DESC);
	}
	

	public static <T> Comparator<T> orderBy(Function<T, Integer> function, Direction direction) {
		Comparator<T> comparator = new Comparator<T>() {
			
			@Override
			public int compare(T o1, T o2) {
				int o1Int = function.apply(o1);
				int o2Int = function.apply(o2);
				
				if (direction == Direction.ASC) {
					return o2Int - o1Int;
				}
				else {
					return o1Int -o2Int;
				}
				
			}
		};
		return comparator;
	}
}
