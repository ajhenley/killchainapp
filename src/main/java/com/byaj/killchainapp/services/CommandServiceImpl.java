package com.byaj.killchainapp.services;

import com.byaj.killchainapp.models.Player;
import com.byaj.killchainapp.models.Vm;
import com.byaj.killchainapp.repositories.PlayerRepository;
import com.byaj.killchainapp.repositories.VmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("commandService")
public class CommandServiceImpl implements CommandService {
    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    VmRepository vmRepository;

    @Autowired
    Messages messages;

    private String acceptedOutsideCommands = "Commands recognized by killchain include: [[b;white;]rules], [[b;white;]man], [[b;white;]help], ping, scan, networkmap, patch, install, train, inject, do \n" +
            "To get more information about a command, type man <<command>> \n" +
            "For example, you could type [[b;white;]man rules].\n"+
            "If you need a list of all commands. Type [[b;white;]help].";
    private String acceptedInsideCommands = "Commands recognized by killchain include: [[b;white;]rules], [[b;white;]man], [[b;white;]help], ping, scan, networkmap, patch, install, train, inject, do \n" +
            "To get more information about a command, type man <<command>> \n" +
            "For example, you could type [[b;white;]man rules].\n"+
            "If you need a list of all commands. Type [[b;white;]help].";


    public String processOutside(String command){
        return outsideCommands(command, "outside");
    }

    public String processInside(String command, Long userid){
        String temp = outsideCommands(command, "inside");
        if (temp.equals( "|||" )){
            temp = insideCommands(command, userid);
        }
        return temp;
    }

    public String outsideCommands(String command, String where){
        // Commands that work when not logged in
        if (command.equals("help")) {
            if (where.equals("outside")){
                return acceptedOutsideCommands;
            } else {
                return acceptedInsideCommands;
            }
        } else if (command.length() > 3 && command.substring(0,4).equals("time")){
            return new Date().toString();
        } else if (command.length() > 3 && command.substring(0,4).equals("date")){
            return new Date().toString();
        } else if (command.length() > 4 && command.substring(0,4).equals("man ")){
            String infoRequest = command.substring(4);
            return manPage(infoRequest);
        } else if (command.length() > 4 && command.substring(0,5).equals("rules")){
            return rules();
        } else if (command.length() > 6 && command.substring(0,7).equals("version")){
            return version();
        } else {
            if (where.equals("outside")) {
                if (command.length() > 5 && command.substring(0,6).equals("status")) {
                    return "You are not currently in-game...";
                } else {
                    return "Please enter a recognized command. If you need help, use the [[b;white;]man <<command>>]. \n" +
                            "For example, you could type [[b;white;]man rules].\n" +
                            "If you need a list of all commands. Type [[b;white;]help].";
                }
            } else {
                return "|||";
            }
        }
    }
    public String insideCommands(String command, Long userid){

        // Commands that work when logged in
        if (command.equals("help")) {
            return acceptedInsideCommands;
        } else if (command.length() > 2 && command.substring(0,4).equals("ssh ")){
            String target = command.substring(4);
            return ssh(target);
        } else if (command.length() > 3 && command.substring(0,4).equals("time")){
            return new Date().toString();
        } else if (command.length() > 3 && command.substring(0,4).equals("date")){
            return new Date().toString();
        } else if (command.length() > 4 && command.substring(0,4).equals("man ")){
            String infoRequest = command.substring(4);
            return manPage(infoRequest);
        } else if (command.length() > 4 && command.substring(0,5).equals("rules")){
            return rules();
        } else if (command.length() > 5 && command.substring(0,6).equals("status")){
            return status(userid);
        } else {
            return "Please enter a recognized command. If you need help, use the [[b;white;]man <<command>>]. \n" +
                    "For example, you could type [[b;white;]man rules].\n"+
                    "If you need a list of all commands. Type [[b;white;]help].";
        }
    }

    private String ssh(String command){
        int portstart = command.indexOf("-i");
        int passstart = command.indexOf("-p");

        String ip = command.substring(portstart +3 ,passstart - 1);
        String password = command.substring(passstart + 3);
        return "";
    }

    private String manPage(String command){
        if (command.equals("rules")) {
            return "Use the command [[b;white;]rules] to get the rules of Killchain";
        } else if (command.equals("help")) {
            return "Okay, that is a little redundant.";
        } else if (command.equals("time")) {
            return " The [[b;white;]time] command gives you the current network time.";
        } else if (command.equals("date")) {
            return " The [[b;white;]date] command gives you the current network date.";
        } else {
            return command + " is not a command that I can help you with.";
        }
    }

    private static String rules(){
        String myRules = "These are the rules of killchain: \n"+
                "1. Never talk about killchain. \n"+
                "2. Do damage onto others before others do damage onto you. \n";
        return myRules;
    }

    private  String status(Long userid){
        Player player = new Player();
        player = playerRepository.findOne(userid);
        List<Vm> vms = vmRepository.findByOwner(userid);
        String response = "";
        String vmlist = "";

        // name
        response += "[[b;white;]Name:         ]" + player.getName();
        // machine name
        response += "\n[[b;white;]Machine Name: ]" + player.getMachinename();
        // ip address
        response += "\n[[b;white;]IP Address:   ]" + player.getIpaddress();
        // score
        response += "\n[[b;white;]Funds:        ]" + player.getBitcoin();
        // vms
        if (vms.size() > 0){
            for (Vm vm : vms){
                vmlist += vm.getIpaddress() + " | ";
            }
            response += "\n[[b;white;]VM's:         ]" + vmlist;
        } else {
            response += "\n[[b;white;]VM's:         ]" +  "Player has no virtual machines.";
        }

        // protections
        if (player.getMitlist().length() > 0) {
            response += "\n[[b;white;]Protections:  ]" + player.getMitlist();
        } else {
            response += "\n[[b;white;]Protections:  ]" + "Player has no installed protections";
        }

        return response;
    }

    private String version(){
        return "Killchain [[b;white;]v" + messages.get("static.version") + "]";
    }
}


