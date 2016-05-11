package com.github.kdvolder.cfv2sample;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

public class EmitterProcessorSample {

	final int producer_delay = 100;
	final int consumer_delay = 200;

	Executor exec = Executors.newCachedThreadPool();

	void reactiveMain() {
		Flux<String> results = searchAsFlux(100);
		for (String result : results.toIterable()) {
			System.out.println("rfound: "+result);
		}
		System.out.println("=== no more results ====");
	}

	/**
	 * Calls the 'legacy' search engine and returns its results as a Flux.
	 */
	Flux<String> searchAsFlux(int query) {
		ReplayProcessor<String> emitter = ReplayProcessor.<String>create(10).connect();
		search(query, new SearchRequestor() {
			@Override
			public void accept(String result) {
				emitter.onNext(result);
			}

			@Override
			public void done() {
				emitter.onComplete();
			}
		});
		return emitter;
	}

	void legacyMain() {
		search(100, new SearchRequestor() {
			@Override
			public void accept(String result) {
				System.out.println("found: "+result);
			}

			@Override
			public void done() {
				System.out.println("=== no more results ====");
			}
		});
	}

	/**
	 * Mimicks behavior of 'legacy' search engine.
	 */
	void search(int query, SearchRequestor requestor) {
		exec.execute(() -> {
			for (int i = 1; i <= query; i++) {
				requestor.accept("Result "+i);
				sleep(producer_delay);
			}
			requestor.done();
		});
	}

	/**
	 * Callback interface where 'legacy' search engine sends results.
	 */
	interface SearchRequestor {
		void accept(String result);
		void done();
	}

	///////////////////////////////////////////////////// cruft //////////////////////////////////////

	public static void main(String[] args) {
		new EmitterProcessorSample().reactiveMain();
	}


	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
