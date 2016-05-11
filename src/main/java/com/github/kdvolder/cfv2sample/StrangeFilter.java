package com.github.kdvolder.cfv2sample;

import java.util.function.Consumer;

import reactor.core.publisher.Flux;

public class StrangeFilter {

	public static void main(String[] args) {

		Flux<Integer> nums = Flux.range(1, Integer.MAX_VALUE)
//		.doOnNext(print("beg"))
		.take(20)
//		.doOnNext(print("took"))
		.cache(20)
		.doOnNext(print("nums"))
		;


		Flux<Integer> filtered = nums
				.filter((i) -> i%5==0)
				.doOnNext(print("filt"))
//				.take(20)
				.cache(20)
				;

		filtered.consume(print("end"));
	}

	private static <T> Consumer<T> print(String msg) {
		return (e) -> System.out.println(msg+": "+e);
	}

}
