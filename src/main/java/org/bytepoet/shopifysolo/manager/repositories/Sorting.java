package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Comparator;
import java.util.function.Function;

public class Sorting {
	
	public static enum Direction {
		ASC,
		DESC
	}
	
	public static <T> Comparator<T> orderByAsc(Function<T, Comparable<?>> function) {
		return orderBy(function, Direction.ASC);
	}
	
	public static <T> Comparator<T> orderByDesc(Function<T, Comparable<?>> function) {
		return orderBy(function, Direction.DESC);
	}
	

	public static <T> Comparator<T> orderBy(Function<T, Comparable<?>> function, Direction direction) {
		Comparator<T> comparator = new Comparator<T>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public int compare(T o1, T o2) {
				Comparable<Object> o1Comp = (Comparable<Object>) function.apply(o1);
				Comparable<Object> o2Comp = (Comparable<Object>) function.apply(o2);
	
				if (direction == Direction.ASC) {
					return o2Comp.compareTo(o1Comp);
				}
				else {
					return o1Comp.compareTo(o2Comp);
				}
				
			}
		};
		return comparator;
	}
}
