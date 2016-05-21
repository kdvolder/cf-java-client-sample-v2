package com.github.kdvolder.cfv2sample;

import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import reactor.core.publisher.Flux;
import reactor.core.tuple.Tuple2;

public class ReactorUtils {

	/**
	 * Infinite stream of timestamps which come as fast as subscriber wants (its up to subscriber to
	 * to not demand too much.
	 */
	public static final Flux<Long> timestamps = Flux.fromIterable(() -> new Iterator<Long>() {
		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public Long next() {
			return System.currentTimeMillis();
		}
	});

	/**
	 * Sorts the elements in a flux in a moving time window. I.e. this assumes element order may be
	 * scrambled but the scrambling has a certain 'time localilty' to it. So we only need to consider
	 * sorting of elements that arrive 'close to eachother'.
	 * <p>
	 * WARNING: The returned flux is intended for a single subscriber. It only maintains a
	 * single buffer for sorting stream elements. This buffer is consumed when elements
	 * are released to any subscriber. Therefore if one subscriber received a element it is gone
	 * from the buffer and will not be delivered to the other subscribers.
	 *
	 * @param stream The stream to be sorted
	 * @param comparator Compare function to sort with
	 * @param bufferTime The 'window' of time beyond which we don't need to compare elements.
	 */
	public static <T> Flux<T> sort(Flux<T> stream, Comparator<T> comparator, Duration bufferTime) {

		class SorterAccumulator {

			final PriorityQueue<Tuple2<T, Long>> holdingPen = new PriorityQueue<>((Tuple2<T, Long> o1, Tuple2<T, Long> o2) -> {
				return comparator.compare(o1.t1, o2.t1);
			});

			final Flux<T> released = Flux.fromIterable(() -> new Iterator<T>() {
				@Override
				public boolean hasNext() {
					Tuple2<T, Long> nxt = holdingPen.peek();
					return nxt!=null && isOldEnough(nxt);
				}

				private boolean isOldEnough(Tuple2<T, Long> nxt) {
					long age = System.currentTimeMillis() - nxt.t2;
					return age > bufferTime.toMillis();
				}

				@Override
				public T next() {
					return holdingPen.remove().t1;
				}
			})
			.delaySubscription(bufferTime);

			public SorterAccumulator next(Flux<Tuple2<T, Long>> window) {
				window.subscribe(holdingPen::add);
				return this;
			}

			public Flux<T> getReleased() {
				return released;
			}
		}

		return stream
		.zipWith(timestamps)
		.window(bufferTime)
		.scan(new SorterAccumulator(), SorterAccumulator::next)
		.flatMap(SorterAccumulator::getReleased, 1);
	}

}
