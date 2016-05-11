package com.github.kdvolder.cfv2sample;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Computations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class Main {

	private static final Scheduler scheduler = Computations.concurrent("blocking-io");
	
	private static Logger foo = LoggerFactory.getLogger("foo");

	public static void main(String[] a) {
		Flux<Integer> nums = Flux.range(1, 20);
		nums
//		.log("a")
		.flatMap((x) -> readData(x))
//		.log("c")
		.subscribe(print("%out"))
		;
		System.out.println("=====================");
	}



	private static Mono<String> readData(Integer x) {
		return Mono.fromCallable(() -> {
			foo.info("reading data ["+x+"]...");
			Thread.sleep(1000);
			return "[data "+x+"]";
		})
		.subscribeOn(scheduler);
	}



	private static <T> Consumer<T> print(String string) {
		return (x) -> {
			foo.info(string+": "+x);
		};
	}

//	public static void main(String[] args) {
//		Timer.global();
//
//		Flux.range(0, 1000000000)
//		.doOnNext(print("msg created"))
//		.delay(Duration.ofMillis(100))
//		.filter((x) -> x%3==0)
//		.take(Duration.ofMillis(500))
//		.consume(print("end"));
//	}
//
//	private static Consumer<? super Integer> print(String string) {
//		return (x) -> {
//			System.out.println(string+": "+x);
//		};
//	}
}
