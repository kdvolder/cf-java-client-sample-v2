package com.github.kdvolder.cfv2sample;

import java.util.function.Consumer;

import reactor.core.publisher.Flux;

public class CacheBroken {

	public static void main(String[] args) {
		Flux<Integer> nums = Flux.range(1, Integer.MAX_VALUE)
		.take(20)
		.cache(20)
		;

		System.out.println(nums.toList().get());
	}

	private static <T> Consumer<T> print(String msg) {
		return (e) -> System.out.println(msg+": "+e);
	}

}
