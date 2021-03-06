package com.fathomdb.cli.formatter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.output.OutputSink;

public class DefaultFormatter implements Formatter {
	public static final DefaultFormatter INSTANCE = new DefaultFormatter();

	@Override
	public List<Class<?>> getHandledClasses() {
		return null;
	}

	@Override
	public void visitObject(CliContext context, Object o, OutputSink sink) throws IOException {
		LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("class", o.getClass().getSimpleName());
		values.put("value", o);

		sink.outputRow(values);
	}

}
