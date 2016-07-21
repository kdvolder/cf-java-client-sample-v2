package com.github.kdvolder.cfv2sample;

import java.time.Duration;

import reactor.core.publisher.Mono;

/**
 * Let's see if we can still reproduce the problem if we only use reactor and some timed operations, but not
 * cf client.
 * 
 * @author Kris De Volder
 */
public class SimplerSample {

	public static void main(String[] args) {
		Mono.just("Hello")
		.delaySubscription(Duration.ofSeconds(2))
		.subscribe(System.out::println);
		
		while (true) {
			try {
				Thread.sleep(2*60*1000);
			} catch (InterruptedException e) {
			}
		}
	}
}
