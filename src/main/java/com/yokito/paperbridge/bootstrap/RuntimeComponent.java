package com.yokito.paperbridge.bootstrap;

/**
 * Represents a runtime-managed component that starts with the plugin and stops on shutdown.
 */
public interface RuntimeComponent {

    void start();

    void stop();
}
