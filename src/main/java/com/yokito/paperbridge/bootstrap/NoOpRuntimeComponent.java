package com.yokito.paperbridge.bootstrap;

/**
 * Null object used when a runtime component is intentionally disabled.
 */
public final class NoOpRuntimeComponent implements RuntimeComponent {

    public static final NoOpRuntimeComponent INSTANCE = new NoOpRuntimeComponent();

    private NoOpRuntimeComponent() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
