package sizzle.aggregators;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * A Sizzle aggregator to log values in a dataset by weight.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "log", weightType = "string")
public class LogAggregator extends Aggregator {
	private static Logger logger = Logger.getLogger(LogAggregator.class);

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		if (metadata.equals("trace"))
			LogAggregator.logger.debug(data);
		else if (metadata.equals("debug"))
			LogAggregator.logger.debug(data);
		else if (metadata.equals("info"))
			LogAggregator.logger.info(data);
		else if (metadata.equals("warn"))
			LogAggregator.logger.warn(data);
		else if (metadata.equals("error"))
			LogAggregator.logger.error(data);
		else if (metadata.equals("fatal"))
			LogAggregator.logger.fatal(data);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}
}
