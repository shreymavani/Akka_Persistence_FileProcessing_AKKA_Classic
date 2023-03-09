package org.example;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import akka.persistence.journal.Tagged;
import akka.persistence.typed.PersistenceId;
import org.example.GetActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterFileActor extends AbstractPersistentActor {

    private final List<String> items = new ArrayList<>();
    private final ActorRef putFileActorRef;

//    public static Props props(S ActorRef putFileActorRef) {
//        return Props.create(FilterFileActor.class, () -> new FilterFileActor( putFileActorRef));
//    }

    public FilterFileActor(ActorRef putFileActorRef) {
        this.putFileActorRef = putFileActorRef;
    }


    @Override
    public String persistenceId() {
        return "persistenceId_for_filterfile";
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::filterFile)
                .build();
    }

    private void filterFile(String data) {
        putFileActorRef.tell(data,getSelf());
        persist(new Tagged(data, Collections.singleton("filter-file")), evt -> {
            // Log successful persist event
            //getContext().getLog().info("File {} persisted successfully", file);
            items.add(0, data);
            if (items.size() > 5) {
                items.remove(5);
            }
        });
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(String.class, evt -> {
                    items.add(0, evt);
                    if (items.size() > 5) {
                        items.remove(5);
                    }
                })
                .match(SnapshotOffer.class, ss -> {
                    items.clear();
                    items.addAll((List<String>) ss.snapshot());
                })
                .build();
    }

//    @Override
//    public void onPersistFailure(Throwable cause, Object event, long sequenceNr) {
//        // Handle persist failure
//    }
//
//    @Override
//    public void onPersistRejected(Throwable cause, Object event, long sequenceNr) {
//        // Handle persist rejection
//    }
}

