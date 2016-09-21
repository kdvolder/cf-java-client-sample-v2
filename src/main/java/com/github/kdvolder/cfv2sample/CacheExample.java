package com.github.kdvolder.cfv2sample;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class CacheExample {
	
	static AtomicInteger i = new AtomicInteger(0);
	
	static Mono<String> hello(String name) {
		return Mono.fromCallable(() -> {
			//Simulate expensive operation.
			wasteTimeAndCPU(1000);
			return "Hello "+name +" "+(i.getAndIncrement());
		}).subscribeOn(Schedulers.elastic());
	}
	
	static void wasteTimeAndCPU(long millis) {
		long endTime = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis()<endTime) {
			//just wasting time :-)
		}
	}

	/**
	 * Turn asynchronous function into a cached version of the function.
	 */
	static <A, R> Function<A, Mono<R>> cache(Function<A, Mono<R>> f) {
		HashMap<A, Mono<R>> functionResults = new HashMap<>();
		return (a) -> {
			Mono<R> result = null;
			synchronized (functionResults) {
				result = functionResults.get(a);
				if (result==null) {
					functionResults.put(a, result = Mono.defer(() -> f.apply(a)).cache());
				}
			}
			return result;
		};
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Function<String,Mono<String>> sayHello = cache(CacheExample::hello); 
//		Function<String,Mono<String>> sayHello = CacheExample::hello; 

		Flux.range(0, 300)
		.map((i) -> "World")
		.flatMap(sayHello)
		.doOnComplete(() -> System.out.println("======\nTook "+(System.currentTimeMillis()-startTime) + " ms"))
		.subscribe(System.out::println);
		
		//Prevent JVM from exiting right away
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}
}
