package ph.codeia.shiv.demo;

/*
 * This file is a part of the Shiv project.
 */


public interface AppFlow {
	void handle(Throwable error, Runnable retry);
	void handle(Throwable error);
	void quit();
}
