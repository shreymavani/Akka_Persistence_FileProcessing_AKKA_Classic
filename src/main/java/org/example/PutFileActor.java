package org.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.typed.PersistenceId;

import javax.naming.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PutFileActor extends AbstractPersistentActor {

    private final String outputDir;

    private final List<String> items = new ArrayList<>();


    public PutFileActor(String outputDir) {
        super();
        this.outputDir = outputDir;
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(String.class, this::addItem)
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::putFile)
                .build();
    }

    private void putFile(String data) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(outputDir, true);
            fw.write(data);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        persist(data, this::addItem);
    }

    private void addItem(String data) {
        items.add(0, data);
        // keep 5 items
        if (items.size() > 5) {
            items.remove(items.size() - 1);
        }
    }

    @Override
    public String persistenceId() {
        return "put-file-actor-" + outputDir;
    }
}
