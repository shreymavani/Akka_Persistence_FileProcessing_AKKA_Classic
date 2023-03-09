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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        data = stringFiltering(data,"Shrey");
        putFileActorRef.tell(data,getSelf());
        String finalData = data;
        persist(new Tagged(data, Collections.singleton("filter-file")), evt -> {
            // Log successful persist event
            //getContext().getLog().info("File {} persisted successfully", file);
            items.add(0, finalData);
            if (items.size() > 5) {
                items.remove(5);
            }
        });
    }

    public String stringFiltering(String inputData, String regex) {
        String[] splitData = inputData.split("\n");
        String filterData = "";
//        System.out.println(splitData.length);
        for (String line : splitData) {

            Pattern pattern = Pattern.compile(regex);            // Compile the pattern

            Matcher matcher = pattern.matcher(line);             // Replace the password with the string ""

            filterData += (matcher.replaceAll("Kuldeep") + "\n");     // Replace the word "password" and password details with the string ""
        }

        return filterData;

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

