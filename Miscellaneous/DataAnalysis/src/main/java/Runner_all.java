import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Runner_all {

    public static String getLogMessage(String logStmt){
        String items[] = logStmt.split(" ");

        int counter_start = 0 ;

        for(String token: items){
            if(!token.equals("(")){
                counter_start += 1;
            }
            else{
                break;
            }
        }

        int counter_end = items.length-1;
        for(int i=items.length-1;i>=0;i--){
            if(!items[i].equals(")")){
                counter_end -= 1;
            }
            else{
                break;
            }
        }

        String result = "";
        for(int i=counter_start;i<=counter_end;i++){
            result += items[i] + " ";
        }
        return result;
    }

    public static int getDifferenceLogLevel(String logStmtTarget, String logStmtPrediction){
        //Trace < Debug < Info < Warn < Error < Fatal.

        int cardinalTarget = 0;

        switch(logStmtTarget) {

            case "trace":
                // code block
                cardinalTarget = 1;
                break;

            case "debug":
                cardinalTarget = 2;
                break;

            case "info":
                cardinalTarget = 3;
                break;

            case "warn":
                cardinalTarget = 4;
                break;

            case "error":
                cardinalTarget = 5;
                break;

            case "fatal":
                cardinalTarget = 6;
                break;

            default:
                break;
        }

        int cardinalPrediction = 0;
        switch(logStmtPrediction) {

            case "trace":
                // code block
                cardinalPrediction = 1;
                break;

            case "debug":
                cardinalPrediction = 2;
                break;

            case "info":
                cardinalPrediction = 3;
                break;

            case "warn":
                cardinalPrediction = 4;
                break;

            case "error":
                cardinalPrediction = 5;
                break;

            case "fatal":
                cardinalPrediction = 6;
                break;

            default:
                break;
        }


        return Math.abs(cardinalTarget-cardinalPrediction);
    }


    /*
      log.trace("Trace Message!");
      log.debug("Debug Message!");
      log.info("Info Message!");
      log.warn("Warn Message!");
      log.error("Error Message!");
      log.fatal("Fatal Message!");
     */
    public static String getLevelLog(String logStmt){

        String extractedLevel = "";
        String [] tokensLogStmt = logStmt.split(" ");

        for(String token: tokensLogStmt){
            switch(token) {

                case "trace":
                    // code block
                    extractedLevel = "trace";
                    break;

                case "debug":
                    extractedLevel = "debug";
                    break;

                case "info":
                    extractedLevel = "info";
                    break;

                case "warn":
                    extractedLevel = "warn";
                    break;

                case "error":
                    extractedLevel = "error";
                    break;

                case "fatal":
                    extractedLevel = "fatal";
                    break;

//                case "log":
//                    extractedLevel = "log";
//                    break;
//
//                case "warning":
//                    extractedLevel = "warning";
//                    break;

                default:
                    break;
            }

        }



        return extractedLevel;
    }


    public static void main(String[] args) throws IOException {
        // Create a diff comparator with two inputs strings.

        Run.initGenerators(); // registers the available parsers

        List<String> input_list = Files.readAllLines(Paths.get(""));    // input method .txt file
        List<String> target_list = Files.readAllLines(Paths.get(""));   // target method .txt file
        List<String> prediction_list = Files.readAllLines(Paths.get(""));   // output method .txt file
        List<String> log_stmt_list = Files.readAllLines(Paths.get("")); // target log statement .txt file
        

        int matchingLevel = 0;
        int matchingPosition = 0;
        int matchingMessage = 0;
        int unparsable_counter = 0;
        int unparsable_input = 0;
        int unparsable_target = 0;
        int outofbound_counter = 0;
        int exception_counter = 0;


        // Res files

        // create new FileWriter object with file as parameter
        FileWriter perfectLog = new FileWriter("perfectLogOnly.csv");
        FileWriter unparsableLog = new FileWriter("unparsableLogOnly.csv");
        FileWriter allLogPosition = new FileWriter("logPositionALL.csv");
        FileWriter predictionMask = new FileWriter("predictionMask.csv");
        FileWriter wrongLogPosition = new FileWriter("wrongLogPosition.csv");
//        FileWriter predictionAll = new FileWriter("predictionAll.csv");

        CSVWriter writerPerfectLog = new CSVWriter(perfectLog);
        String[] headerN1 = { "Instance Number", "Prediction", "Target" };
        writerPerfectLog.writeNext(headerN1);

        CSVWriter writerUnparsableLog = new CSVWriter(unparsableLog);
        String[] headerN2 = { "Instance Number", "Prediction", "Target" };
        writerUnparsableLog.writeNext(headerN2);

        CSVWriter writerAllPosition = new CSVWriter(allLogPosition);
        String[] headerN3 = { "Instance Number", "Prediction", "Target" };
        writerAllPosition.writeNext(headerN3);

        CSVWriter writerMask = new CSVWriter(predictionMask);
        String[] headerN4 = { "Instance Number", "Level", "Position", "Message" };
        writerMask.writeNext(headerN4);

        CSVWriter writerwrongAllPosition = new CSVWriter(wrongLogPosition);
        String[] headerN5 = { "Instance Number", "Prediction", "Target" };
        writerwrongAllPosition.writeNext(headerN5);
//        CSVWriter writerPrediction = new CSVWriter(predictionAll);
//        String[] headerN5 = { "Instance Number", "Prediction", "Target", "Parsable"};
//        writerPrediction.writeNext(headerN5);

        // create FileWriter object with file as parameter
        FileWriter predictedLog = new FileWriter("predictedLogsOnly.csv");
        FileWriter outputFileDistance = new FileWriter("logLevelDistance.csv");

        FileWriter correctLogLevelWriter = new FileWriter("correctLogLevel.csv");
        FileWriter correctLogPositionWriter = new FileWriter("correctLogPosition.csv");

        CSVWriter writerCorrectLevel = new CSVWriter(correctLogLevelWriter);
        String[] headerX = { "Instance Number", "Prediction", "Target" };
        writerCorrectLevel.writeNext(headerX);

        CSVWriter writerCorrectPosition = new CSVWriter(correctLogPositionWriter);
        String[] headerX1 = { "Instance Number", "Prediction", "Target" };
        writerCorrectPosition.writeNext(headerX1);

        FileWriter logLevelWriter = new FileWriter("logLevel.csv");
        FileWriter logPositionWriter = new FileWriter("logPosition.csv");

        FileWriter correctLogMessageWriter = new FileWriter("correctLogMessage.csv");


        // create CSVWriter object filewriter object as parameter
        CSVWriter writerLogPrediction = new CSVWriter(predictedLog);
        String[] header0 = { "Instance Number", "Prediction", "Target" };
        writerLogPrediction.writeNext(header0);

        CSVWriter writerLogLevel = new CSVWriter(logLevelWriter);
        String[] header1 = { "Instance Number", "Prediction", "Target" };
        writerLogLevel.writeNext(header1);

        CSVWriter writerLogPosition = new CSVWriter(logPositionWriter);
        String[] header2 = { "Instance Number", "Prediction", "Target" };
        writerLogPosition.writeNext(header2);

        CSVWriter writerLogMessage = new CSVWriter(correctLogMessageWriter);
        String[] header3 = { "Instance Number", "Prediction", "Target" };
        writerLogMessage.writeNext(header3);

        CSVWriter writerDistance = new CSVWriter(outputFileDistance);
        String[] header6 = { "Instance Number", "Prediction", "Target", "Distance" };
        writerDistance.writeNext(header6);



        ////////////////////


        List<String> logLevels = new ArrayList<>();
        logLevels.add("trace");
        logLevels.add("debug");
        logLevels.add("info");
        logLevels.add("warn");
        logLevels.add("error");
        logLevels.add("fatal");

        int itemsSize = prediction_list.size();

        int nonActionNums = 0;
        int perfectPredictionNums = 0;
        for(int j = 0; j < itemsSize; j++) {
            System.out.println("Counter:" + j);
            String inputItem = input_list.get(j);
            String targetItem = target_list.get(j);
            String predItem = prediction_list.get(j);
            String logStmt = log_stmt_list.get(j);

            int level_mask = 0;
            int position_mask = 0;
            int message_mask = 0;

            if (predItem.equals(targetItem)){
                String[] dataN1 = {String.valueOf(j), predItem, targetItem};
                writerPerfectLog.writeNext(dataN1);

                level_mask = 1;
                position_mask = 1;
                message_mask = 1;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);

                perfectPredictionNums += 1;
                continue;
            }



            String flattenedLogStmt = String.join("",logStmt.split(" "));

            //Get logMessage for the target
            String targetLogMessage = String.join("", Runner_all.getLogMessage(logStmt).split(" "));

            //Get logLevel for the target
            String logLevelTarget = String.join("", Runner_all.getLevelLog(logStmt).split(" "));

//            System.out.println(flattenedLogStmt);
//            System.out.println(targetLogMessage);
//            System.out.println(logLevelTarget);

            //Create folder for each input, target and prediction file
            String basePath = "/Users/honglin/Desktop/Denoise-Files/Instance_"+j;

            File f1 = new File(basePath);
            boolean bool = f1.mkdir();

//            String basePath = "/Users/honglin/Desktop/NoPretraining-Files";

            String inputFile = basePath+"/input.java";
            String targetFile = basePath+"/target.java";
            String predFile = basePath+"/prediction.java";

            String classInputToWrite = "public class A { " +inputItem + " }";
            FileWriter myWriter = new FileWriter(inputFile,false);
            myWriter.write(classInputToWrite);
            myWriter.close();

            String classTargetToWrite="public class A { " + targetItem + " }";
            myWriter = new FileWriter(targetFile,false);
            myWriter.write(classTargetToWrite);
            myWriter.close();

            String classPredToWrite="public class A { " + predItem + " }";
            myWriter = new FileWriter(predFile,false);
            myWriter.write(classPredToWrite);
            myWriter.close();

//            Tree input = TreeGenerators.getInstance().getTree(inputFile).getRoot();
//            Tree target = TreeGenerators.getInstance().getTree(targetFile).getRoot();

            Tree input;
            try {
                input = TreeGenerators.getInstance().getTree(inputFile).getRoot();
            }catch (Exception e){
//                unparsable_counter += 1;
                String[] dataN2 = {String.valueOf(j), predItem, targetItem};
                writerUnparsableLog.writeNext(dataN2);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);

                unparsable_input += 1;
                continue;
            }

            Tree target;
            try {
                target = TreeGenerators.getInstance().getTree(targetFile).getRoot();
            }catch (Exception e){
//                unparsable_counter += 1;
                String[] dataN2 = {String.valueOf(j), predItem, targetItem};
                writerUnparsableLog.writeNext(dataN2);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);

                unparsable_target += 1;
                continue;
            }

            Tree prediciton;
            try {
                prediciton = TreeGenerators.getInstance().getTree(predFile).getRoot();
            }catch (Exception e){
                // Cannot construct the tree for the prediction, therefore we analyze this one with the CodeBleu
                String[] dataN2 = {String.valueOf(j), predItem, targetItem};
                writerUnparsableLog.writeNext(dataN2);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);

                unparsable_counter += 1;
                continue;
            }

            Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher

            MappingStore mappingsToTarget = defaultMatcher.match(input, target); // computes the mappings between the trees
            EditScriptGenerator editScriptGeneratorTarget = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
            EditScript actionsTarget = editScriptGeneratorTarget.computeActions(mappingsToTarget); // computes the edit script

            MappingStore mappingsToPrediction = defaultMatcher.match(input, prediciton); // computes the mappings between the trees
            EditScriptGenerator editScriptGeneratorPrediction = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
            EditScript actionsPrediction = editScriptGeneratorPrediction.computeActions(mappingsToPrediction); // computes the edit scrip

            if (actionsPrediction.size()==0){
                String[] data0 = {String.valueOf(j), "", logStmt};
                writerLogPrediction.writeNext(data0);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);
                nonActionNums+=1;
                continue;
            }

            int difference = Integer.MAX_VALUE;
            int targetEditAction = 0;

            int startPosTarget = -1;
            int endPosTarget = -1;


            for (int i = 0; i < actionsTarget.size(); i++) {

                int posStart1 = actionsTarget.get(i).getNode().getPos();
                int posEnd1 = actionsTarget.get(i).getNode().getEndPos();
                String sub = String.join("",classTargetToWrite.substring(posStart1,posEnd1).split(" "));
                if(sub.equals(flattenedLogStmt)){
                    startPosTarget = posStart1;
                    endPosTarget = posEnd1;
                    break;
                }

            }

            //picking the nearest edit action to the target one
            for (int i = 0; i < actionsPrediction.size(); i++) {

                int newRelativePosition = Math.abs(startPosTarget-actionsPrediction.get(i).getNode().getPos());
                if (newRelativePosition<difference);
                    difference = newRelativePosition;
                    targetEditAction = i;
            }

            int startPosPrediction = -1;
            int endPosPrediction = -1;

            startPosPrediction = actionsPrediction.get(targetEditAction).getNode().getPos();
            endPosPrediction = actionsPrediction.get(targetEditAction).getNode().getEndPos();

            // writing all position
            String[] dataN3 = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
            writerAllPosition.writeNext(dataN3);

            String wrappedPred = "";
            String finalString = "";
            String[] itemsString = null;
            try {
                wrappedPred = "public class A { " + predItem + " }";
                finalString = wrappedPred.substring(startPosPrediction, endPosPrediction);
                itemsString = finalString.split(" ");
            }catch (Exception e){
                String[] data0 = {String.valueOf(j), finalString, logStmt};
                writerLogPrediction.writeNext(data0);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);
                exception_counter += 1;
                continue;
            }

            String item0="";
            String item1="";
            String item2="";
            String itemLast="";

            // CHECK HERE @Honglin
            try {
                item0 = itemsString[0].toLowerCase();
                item1 = itemsString[1].toLowerCase();
                item2 = itemsString[2].toLowerCase();
                itemLast = itemsString[itemsString.length - 1];
            }catch(Exception e){
                String[] data0 = {String.valueOf(j), finalString, logStmt};
                writerLogPrediction.writeNext(data0);

                level_mask = 0;
                position_mask = 0;
                message_mask = 0;
                String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                writerMask.writeNext(dataN5);
                exception_counter += 1;
                continue;

//                if (itemsString.length < 3) {
//                    String[] data0 = {String.valueOf(j), finalString, logStmt};
//                    writerLogPrediction.writeNext(data0);
//                    continue;
//                }
//                else {
//                    exception_counter += 1;
//                    continue;
//                }
            }


            //writing The predicted log on txt File
            String[] data0 = {String.valueOf(j), finalString, logStmt};
            writerLogPrediction.writeNext(data0);

            if (((item2.contains("log") && item1.contains(".")) || (item0.contains("log") || item1.contains("log"))) && itemLast.contains(";")) {

                String logLevelPrediction = String.join("", Runner_all.getLevelLog(finalString).split(" "));
                String predLogMessage = String.join("", Runner_all.getLogMessage(finalString).split(" "));

                if(logLevelPrediction.equals("")) {

                    int startFrom = 0;
                    for (String token : finalString.split(" ")) {

                        if (token.equals("log") || token.equals("warning")) {
                            break;
                        }
                        startFrom += 1;
                    }
                    String [] subStr = finalString.substring(startFrom).split(" ");
                    String joinedString = "";

                    for(int k=startFrom+1; k<subStr.length-1; k++){
                        joinedString = joinedString + " " + subStr[k];
                    }

                    //Check here
                    int diff=-1;
                    String[] subArray = finalString.split(" ");
                    try {
                        for (String token : Arrays.copyOfRange(subArray, startFrom + 1, subArray.length - 1)) {
                            if (logLevels.contains(token)) {
                                diff = getDifferenceLogLevel(logLevelTarget, token);
                                logLevelPrediction = token;
                            }
                        }
                    }catch(Exception e){
                        logLevelPrediction="";
                        System.out.println(j);
                    }

                }

                //Retrieve correct cardinal difference only for those log for which there is a Log4J mapping
                if(!logLevelTarget.equals("log") && !logLevelTarget.equals("warning") && !logLevelPrediction.equals("")) {

//                if(!logLevelTarget.equals("log") && !logLevelTarget.equals("warning") && !logLevelPrediction.equals("")) {

                    int distance = getDifferenceLogLevel(logLevelTarget, logLevelPrediction);
                    String[] data1 = {String.valueOf(j), logLevelPrediction, logLevelTarget, String.valueOf(distance)};
                    writerDistance.writeNext(data1);


                    if (startPosPrediction == startPosTarget) {
                        String[] data2 = {String.valueOf(j), predItem, targetItem};
//                        writerLogPosition.writeNext(data2);
                        writerCorrectPosition.writeNext(data2);

                        String[] data_corrPos = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
                        writerLogPosition.writeNext(data_corrPos);

                        position_mask = 1;

                        matchingPosition += 1;
                    } else {
                        String[] data_wrongPos = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
                        writerwrongAllPosition.writeNext(data_wrongPos);

//                        String offsetTarget = classTargetToWrite.substring(17,startPosTarget);
                        String offsetTarget;
                        try {
                            offsetTarget = classTargetToWrite.substring(17, startPosTarget);
                        } catch (Exception e) {
//                            String[] dataN2 = {String.valueOf(j), predItem, targetItem};
//                            writerUnparsableLog.writeNext(dataN2);
                            level_mask = 0;
                            position_mask = 0;
                            message_mask = 0;
                            String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
                            writerMask.writeNext(dataN5);
                            outofbound_counter += 1;
                            continue;
                        }
                        String offsetPrediction = "";
                        if (startPosPrediction == 0) {
                            offsetPrediction = wrappedPred.substring(17);
                            System.out.println(j);
                        } else {
                            offsetPrediction = wrappedPred.substring(17, startPosPrediction);
                        }
                        String[] data_incorrPos = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
                        writerLogPosition.writeNext(data_incorrPos);
//                        String[] data3 = {String.valueOf(j), predItem, targetItem};
//                        writerLogPosition.writeNext(data3);
                    }

                    // We have a matching level. Keep track of those one
                    if (logLevelPrediction.equals(logLevelTarget)) {
                        level_mask = 1;
                        matchingLevel += 1;
                        String[] data = {String.valueOf(j), finalString, logLevelTarget};
                        writerLogLevel.writeNext(data);
                        writerCorrectLevel.writeNext(data);
                    }

                    if (predLogMessage.equals(targetLogMessage)) {
                        message_mask = 1;
                        matchingMessage += 1;
                        String[] data = {String.valueOf(j), finalString, targetLogMessage};
                        writerLogMessage.writeNext(data);
                    }

//                    String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask)};
//                    writerMask.writeNext(dataN5);
                }
                else{
                    String[] data_incorrPos = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
                    writerLogPosition.writeNext(data_incorrPos);
                }

//                // We have a matching level. Keep track of those one
//                if (logLevelPrediction.equals(logLevelTarget)) {
//                    matchingLevel += 1;
//                    String[] data1 = {String.valueOf(j), finalString, logLevelTarget};
//                    writerLogLevel.writeNext(data1);
//                }
//
//                if (predLogMessage.equals(targetLogMessage)) {
//                    matchingMessage += 1;
//                    String[] data1 = {String.valueOf(j), finalString, targetLogMessage};
//                    writerLogMessage.writeNext(data1);
//                }
            }
//        String [] data_incorrPos = {String.valueOf(j), String.valueOf(startPosPrediction), String.valueOf(startPosTarget)};
//        writerLogPosition.writeNext(data_incorrPos);
        String[] dataN5 = {String.valueOf(j), String.valueOf(level_mask), String.valueOf(position_mask), String.valueOf(message_mask)};
        writerMask.writeNext(dataN5);
        }


        logLevelWriter.close();
        logPositionWriter.close();
        writerDistance.close();
        writerLogPrediction.close();
        writerCorrectLevel.close();
        writerCorrectPosition.close();
        writerAllPosition.close();
        writerUnparsableLog.close();
        writerPerfectLog.close();
        correctLogMessageWriter.close();
        writerMask.close();
        writerwrongAllPosition.close();
//        writerLogPosition.close();


        //Perfect predictions denoise-task
        //int perfectPredictionNums = 2312;

        //Perfect predictions logstmt-task
        //int perfectPredictionNums = 1939;

        //Perfect predictions multi-task
        //int perfectPredictionNums = 1986;

        //Perfect predictions no-pretraining
//        int perfectPredictionNums = 1940;
//        int perfectPredictionNums = 0;

//        int total = 2882;
        int total = itemsSize;

        int unparsable_iot = unparsable_input + unparsable_target;

        double percentagePerfectPrediction = ( (double) perfectPredictionNums / total) * 100;
        double percentagePositionCorrect = ( (perfectPredictionNums + (double) matchingPosition) / total) * 100;
        double percentageLevelCorrect = ( (perfectPredictionNums + (double )matchingLevel) / total) * 100;
        double percentageMessageCorrect = ( (perfectPredictionNums + (double )matchingMessage) / total) * 100;
        double percentageUnparsable =  ( ( (double) unparsable_counter) /total) * 100;
        double percentageOutofBound =  ( ( (double) outofbound_counter) /total) * 100;
        double perecentageUnparsableIO = ( ( (double) unparsable_iot) /total) * 100;
        double perecentageUnparsableIn = ( ( (double) unparsable_input) /total) * 100;
        double perecentageUnparsableOut = ( ( (double) unparsable_input) /total) * 100;

        System.out.println("Num Non Actions:" + nonActionNums);
        System.out.println("Not Parsable Input:" + unparsable_input + "/" + total + " :" + perecentageUnparsableIn);
        System.out.println("Not Parsable Target:" + unparsable_target + "/" + total + " :" + perecentageUnparsableOut);
        System.out.println("Not Parsable Input/Target:" + unparsable_iot + "/" + total + " :" + perecentageUnparsableIO);
        System.out.println("Out of Bound Case: " + outofbound_counter + "/" + total + " :" + percentageOutofBound);
        System.out.println("Not Parsable: " + unparsable_counter + "/" + total + " :" + percentageUnparsable);
        System.out.println("Perfect prediction LEVEL: "+ (perfectPredictionNums+matchingLevel) + "/" + total + " :" + percentageLevelCorrect);
        System.out.println("Perfect prediction POS: "+(perfectPredictionNums+matchingPosition) + "/" + total + " :" + percentagePositionCorrect);
        System.out.println("Perfect prediction MESSAGE: "+(perfectPredictionNums + matchingMessage) + "/" + total + " :" + percentageMessageCorrect);
        System.out.println("Perfect prediction: "+ (perfectPredictionNums) + "/" + total + " :" + percentagePerfectPrediction);
        System.out.println("Num of exception: "+ exception_counter);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("log_info.txt");
            PrintStream printStream = new PrintStream(fileOutputStream);
            System.setOut(printStream);
            System.out.println("Not Parsable Input:" + unparsable_input + "/" + total + " :" + perecentageUnparsableIn);
            System.out.println("Not Parsable Target:" + unparsable_target + "/" + total + " :" + perecentageUnparsableOut);
            System.out.println("Not Parsable Input/Target:" + unparsable_iot + "/" + total + " :" + perecentageUnparsableIO);
            System.out.println("Out of Bound Case: " + outofbound_counter + "/" + total + " :" + percentageOutofBound);
            System.out.println("Not Parsable: " + unparsable_counter + "/" + total + " :" + percentageUnparsable);
            System.out.println("Perfect prediction LEVEL: "+ (perfectPredictionNums+matchingLevel) + "/" + total + " :" + percentageLevelCorrect);
            System.out.println("Perfect prediction POS: "+(perfectPredictionNums+matchingPosition) + "/" + total + " :" + percentagePositionCorrect);
            System.out.println("Perfect prediction MESSAGE: "+(perfectPredictionNums + matchingMessage) + "/" + total + " :" + percentageMessageCorrect);
            System.out.println("Perfect prediction: "+ (perfectPredictionNums) + "/" + total + " :" + percentagePerfectPrediction);
            System.out.println("Num of exception: "+ exception_counter);
            printStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
