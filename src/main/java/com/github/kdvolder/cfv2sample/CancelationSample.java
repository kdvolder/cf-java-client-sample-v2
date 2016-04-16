package com.github.kdvolder.cfv2sample;

import java.time.Duration;

import reactor.core.publisher.Mono;

public class CancelationSample {

	interface CancelationToken {
		boolean isCanceled();
	}

	/**
	 * Convert a {@link CancelationToken} into a Mono that raises
	 * an {@link OperationCanceledException} when the token is canceled.
	 */
	public static <T> Mono<T> toMono(CancelationToken cancelToken) {
		return Mono.delay(Duration.ofSeconds(1))
		.then((ping) ->
			cancelToken.isCanceled()
				? Mono.<T>error(
						new OperationCanceledException()
				)
				: Mono.empty()
		)
		.repeatWhenEmpty((x) -> x);
	}

	public static void main(String[] args) {

		CancelationToken cancelToken = new CancelationToken() {
			long created = System.currentTimeMillis();
			@Override
			public boolean isCanceled() {
				System.out.println("Checking cancelation");
				//cancels itself after 10s
				return System.currentTimeMillis() - created > 5_000;
			}
		};

		Mono<String> foo = Mono.just("foo").delaySubscription(Duration.ofSeconds(10));
		Mono<String> canceler = toMono(cancelToken);

		try {
			String result = Mono.any(foo, canceler).get();
			System.out.println("result = "+result);
		} catch (OperationCanceledException e) {
			System.out.println("Canceled!");
		}


	}

}
