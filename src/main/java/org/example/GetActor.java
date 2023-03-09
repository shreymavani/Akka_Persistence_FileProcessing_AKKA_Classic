package org.example;

import akka.actor.*;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetActor extends AbstractPersistentActor {

    private final ActorRef filterActor;
    private static final int MAX_BATCH_SIZE = 1024;

    public static class State {
        private final List<String> items;

        private State(List<String> items) {
            this.items = items;
        }

        public State() {
            this.items = new ArrayList<>();
        }

        public State addItem(String data) {
            List<String> newItems = new ArrayList<>(items);
            newItems.add(0, data);
            // keep 5 items
            List<String> latest = newItems.subList(0, Math.min(5, newItems.size()));
            return new State(latest);
        }
    }

    private State state = new State();

    public GetActor(ActorRef filterActor) {
        super();
        this.filterActor = filterActor;
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(String.class, this::onRecoverItem)
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::onHandleDirectory)
                .build();
    }

    private void onRecoverItem(String data) {
        state = state.addItem(data);
    }

    private void onHandleDirectory(String path) {
        File directory = new File(path);                                                //path to directory

        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {

            try {

                long fileSize = file.length();
                String data = "";
                if (fileSize > MAX_BATCH_SIZE) {

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                        int numBatches = (int) Math.ceil((double) fileSize / MAX_BATCH_SIZE);   // Calculate the number of batches

                        for (int i = 0; i < numBatches; i++) {
                            // Read the next batch
                            char[] batch = new char[MAX_BATCH_SIZE];
                            int read = reader.read(batch, 0, MAX_BATCH_SIZE);


                            String batchString = new String(batch, 0, read);              // Process the batch
                            data += (batchString);

                        }
                    }
                } else {

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {      // File size is less than or equal to the maximum batch size
                        // Read the entire file in one go
                        String entireFile = "";
                        String line;
                        while ((line = reader.readLine()) != null) {
                            entireFile += (line +"\n");
                        }
                        data = entireFile ;
                    }
                }
                filterActor.tell(data,getSelf());

                // persist the data
                persist(data, item -> {
                    state = state.addItem(item);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
        }
    }

    @Override
    public String persistenceId() {
        return "persistenceId_0102";
    }

}

