package com.yokito.paperbridge.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperBridgeRuntimeTest {

    @Test
    void shouldStartAndStopAllComponentsInOrder() {
        List<String> calls = new ArrayList<>();
        PaperBridgeRuntime runtime = new PaperBridgeRuntime(
                new RecordingComponent("minecraft", calls),
                new RecordingComponent("placeholder", calls),
                new RecordingComponent("discord", calls)
        );

        runtime.start();
        runtime.stop();

        assertEquals(List.of(
                "minecraft:start",
                "placeholder:start",
                "discord:start",
                "discord:stop",
                "placeholder:stop",
                "minecraft:stop"
        ), calls);
    }

    @Test
    void shouldAllowNoOpDiscordComponent() {
        List<String> calls = new ArrayList<>();
        PaperBridgeRuntime runtime = new PaperBridgeRuntime(
                new RecordingComponent("minecraft", calls),
                new RecordingComponent("placeholder", calls),
                NoOpRuntimeComponent.INSTANCE
        );

        runtime.start();
        runtime.stop();

        assertEquals(List.of(
                "minecraft:start",
                "placeholder:start",
                "placeholder:stop",
                "minecraft:stop"
        ), calls);
    }

    private record RecordingComponent(String name, List<String> calls) implements RuntimeComponent {

        @Override
        public void start() {
            calls.add(name + ":start");
        }

        @Override
        public void stop() {
            calls.add(name + ":stop");
        }
    }
}
