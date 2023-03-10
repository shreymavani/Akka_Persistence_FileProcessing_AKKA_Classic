package org.example;

//import akka.actor.typed.ActorRef;
//import akka.actor.typed.ActorSystem;
//import akka.actor.typed.MailboxSelector;
//import akka.actor.typed.Props;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Main {
    public static void main(String[] args) {


        Config regularConfig = ConfigFactory.load();
        Config file = ConfigFactory.load("config.conf");
        Config combined = file.withFallback(regularConfig);
        Config complete = ConfigFactory.load(combined);

        ActorSystem system = ActorSystem.create( "FileProcessingSystem",complete);

        ActorRef putfileActorRef =
                system.actorOf(Props.create(PutFileActor.class,"/Users/smavani/INPUT_OUTPUT_FOR_TESTING/OUTPUT/output"), "persistentActor-4-java8");

        ActorRef filterFileActorRef = system
                .actorOf(
                        Props.create(FilterFileActor.class,putfileActorRef),
                        "filterFileActor");

        ActorRef getFileActorRef = system.actorOf(Props.create(GetActor.class,filterFileActorRef),"getfileactor");

        getFileActorRef.tell(("/Users/smavani/INPUT_OUTPUT_FOR_TESTING/INPUT"),null);
    }
}