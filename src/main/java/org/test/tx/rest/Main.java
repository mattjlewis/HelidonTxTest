package org.test.tx.rest;

import java.io.IOException;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.microprofile.server.Server;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		setupLogging();
		LOGGER.info("Starting...");
		Server.create().start();
		LOGGER.info("Ready.");
	}

	private static void setupLogging() throws IOException {
		LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
	}
}
