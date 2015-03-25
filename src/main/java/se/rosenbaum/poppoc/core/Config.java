package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.NetworkParameters;

public class Config {
    public static final NetworkParameters NETWORK_PARAMETERS = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    public static final String POP_DESTINATION="http://www.rosenbaum.se:8888";
}
