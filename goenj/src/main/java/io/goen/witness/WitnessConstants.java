package io.goen.witness;

public class WitnessConstants {

	/**
	 *
	 */
	public final static long GENESIS_TIME = System.currentTimeMillis() / 1000 + 5;
	public final static String GENESIS_TIME_STR = "2017-05-09 16:20:00";

	/**
	 */
	public final static int GOEN_BLOCK_INTERVAL = 3;

	/**
	 */
	public final static int GOEN_NUM_INIT_MINERS = 3;
	public final static String GOEN_INIT_MINER_NAME = "initminer";
	// public final static String GOEN_INIT_PUBLIC_KEY_STR =
	// "STM8GC13uCZbP44HzMLV6zPZGwVQ8Nt4Kji8PapsPiNq1BK153XTX";
	public final static String GOEN_INIT_PUBLIC_KEY_STR = "0450fee691ebd2af5175159d215aed1584fea285a7d642e0862c2e25fbf4f059702729a38a53379c34382d853f1c4f60496878676f8d4d27ecbfbead2e87e1e844";

	/**
	 *
	 */
	public final static int GOEN_MAX_WITNESSES = 21;
	public final static int GOEN_MAX_VOTED_WITNESSES_HF0 = 19;
	public final static int GOEN_MAX_MINER_WITNESSES_HF0 = 1;
	public final static int GOEN_MAX_RUNNER_WITNESSES_HF0 = 1;

	public final static long MAX_SECONDS_PER_YEAR = 365 * 24 * 60 * 60L;
	public final static long BLOCKS_PER_YEAR = (MAX_SECONDS_PER_YEAR / GOEN_NUM_INIT_MINERS);

	public final static long MAX_GOEN_PER_YEAR = 2000000000L;

	//
	public final static String REWARD_FUND_NAME = "REWARD_FUND";
	public final static String REWARD_CONTENT_CONSTANT = "2000000000000";

}
