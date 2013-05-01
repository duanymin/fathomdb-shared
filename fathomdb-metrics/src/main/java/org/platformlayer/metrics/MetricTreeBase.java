package org.platformlayer.metrics;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class MetricTreeBase {
	final String key;

	protected MetricTreeBase(String key) {
		this.key = key;
	}

	public static class MetricTreeString extends MetricTreeBase {
		private final String value;

		public MetricTreeString(String key, String value) {
			super(key);
			this.value = value;
		}

		@Override
		public void accept(MetricTreeVisitor visitor) {
			visitor.visit(this);
		}

		public String getValue() {
			return value;
		}
	}

	public static class MetricTreeFloat extends MetricTreeBase {
		private final double value;

		public MetricTreeFloat(String key, double value) {
			super(key);
			this.value = value;
		}

		@Override
		public void accept(MetricTreeVisitor visitor) {
			visitor.visit(this);
		}

		public double getValue() {
			return value;
		}
	}

	public static class MetricTreeInteger extends MetricTreeBase {
		private final long value;

		public MetricTreeInteger(String key, long value) {
			super(key);
			this.value = value;
		}

		@Override
		public void accept(MetricTreeVisitor visitor) {
			visitor.visit(this);
		}

		public long getValue() {
			return value;
		}
	}

	public static class MetricTreeArray extends MetricTreeBase {
		final List<MetricTreeBase> items = Lists.newArrayList();

		public MetricTreeArray(String key) {
			super(key);
		}

		@Override
		public void accept(MetricTreeVisitor visitor) {
			visitor.visit(this);
		}

		public void visitItems(MetricTreeVisitor visitor) {
			for (MetricTreeBase child : items) {
				child.accept(visitor);
			}
		}

		public void add(MetricTreeBase item) {
			items.add(item);
		}
	}

	protected String sanitizeString(String s) {
		return s.replace(' ', '-');
	}

	public abstract void accept(MetricTreeVisitor visitor);

	public String getKey() {
		return key;
	}

}
