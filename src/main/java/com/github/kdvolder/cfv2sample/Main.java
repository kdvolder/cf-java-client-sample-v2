package com.github.kdvolder.cfv2sample;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SchedulerGroup;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		SchedulerGroup async = SchedulerGroup.async();
		Flux.just(1,2,3)
		.log("before")
		.publishOn(async)
		.log("after")
		.consume((it) -> {
			System.out.println(Thread.currentThread());
			System.out.println(it);
		});


//		System.out.println("main thread: "+Thread.currentThread());
//		CompletableFuture<String> providedLater = new CompletableFuture<>();
//
//		Mono<String> result = Mono.defer(() -> {
//			return Mono.fromFuture(providedLater);
//		})
//		.retry()
//		.cache();
//
//		System.out.println("Before consuming");
//		result
//		.publishOn(SchedulerGroup.async())
//		.log("before println")
//		.consume((it) -> {
//			System.out.println("consuming thread: "+Thread.currentThread());
//		});
//
//		System.out.println("After consuming");
//
//		providedLater.complete("foo");

	}

}
