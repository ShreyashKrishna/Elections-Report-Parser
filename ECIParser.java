import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;


/**
 * Created by Paul on 4/14/2016.
 */
class Constituency
{
    String name;//name of the constituency in that state
    String sNumber;//Serial Number of the constituency as per the list
    List<Candidate> candidates = new ArrayList<Candidate>();//Array list for candidates in each of these constituencies
}
class Candidate
{
    String name;//name of the candidate
    String sNumber;//serial number in that constituency
    String sex;//gender
    String age;//age of the candidate
    String category;//General/SC/St
    String party;//what party they belong to
    String general;//general number of votes
    String postal;//those votes received through post
    String total;//total number of votes
}
public class ECIParser
{
    private static final String newLineSeparator = "\n";
    private static final Object[] fileHeader = {"Constituency No.", "Constituency", "Candidate No.", "Name", "Sex", "Age", "Category", "Party", "General", "Postal", "Total"};//different categories for classifying information
    private static final CSVFormat csvf = CSVFormat.DEFAULT.withRecordSeparator(newLineSeparator);
    private static int[] EXCLUSIONS = new int[5];
    /*
    Method that takes an object of type constituency and a filewriter as inputs and
    writes the constituency name and the names of all the candidates under the
    constituency into a csv file. This method is called by the readResultsFromFile() method.
     */
    public static void writeResultsIntoFile(Constituency constituency, CSVPrinter csvp)
    {

        try {
            for (Candidate c : constituency.candidates)//candidate c is in constituency
            {
                List candidateData = new ArrayList();//a new array list
                candidateData.add(constituency.sNumber);
                candidateData.add(constituency.name);
                candidateData.add(c.sNumber);
                candidateData.add(c.name);
                candidateData.add(c.sex);
                candidateData.add(c.age);
                candidateData.add(c.category);
                candidateData.add(c.party);
                candidateData.add(c.general);
                candidateData.add(c.postal);
                candidateData.add(c.total);
                csvp.printRecord(candidateData);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /*
    Method that takes a file as input and reads the details of each constituency and
    all the candidates under that constituency and writes all these details to a
    csv file. This method works for files that follow layout 1.
    */

    public static void readResultsFromLayout1(File resultFile) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("C:/Users/SHREYASH/Desktop/Scalable/res2.csv");    //Output csv file
            CSVPrinter csvp = null;
            csvp = new CSVPrinter(fileWriter, csvf);
            csvp.printRecord(fileHeader);               //Printing the file header
            Constituency constituency = new Constituency();
            Candidate candidate = null;
            int flag = 0, constituencyFlag = 0, fieldCounter = 0;
            LineNumberReader lnr = new LineNumberReader(new java.io.FileReader(resultFile));
            LineNumberReader nextReader;
            String line, nextLine;
            //Regular Expression patterns for different fields
            Pattern candidatePattern = Pattern.compile("(\\A)(\\d+)(\\s)(\\w*)");
            Pattern constituencyPattern = Pattern.compile("(\\A)(\\d+)([.])(\\s)(\\w+)");
            Pattern sexPattern = Pattern.compile("(\\A)([M F])");
            Pattern agePattern = Pattern.compile("(\\A)^([0-9]){2}$");
            Pattern categoryPattern = Pattern.compile("(\\A)([SC GEN ST]{1,3}$)");
            Pattern partyPattern = Pattern.compile("(\\A)^(?!GEN|TURNOUT|!ST|!SC)([A-Za-z()(\\s)]+){2,10}$");//ensure the party pattern doesnt read categories
            Pattern generalPattern = Pattern.compile("(\\A)^([0-9]){1,10}$");
            Pattern postalPattern = Pattern.compile("(\\A)^[0-9]{1,4}$");
            Pattern totalPattern = Pattern.compile("(\\A)^([0-9]{1,10}$)");
            //Reading from the file
            while ((line = lnr.readLine()) != null) {
                if (line.toLowerCase().contains("detailed results")) {
                    flag++;
                }
                if (flag >= 2) {   //If "detailed results" has been found 2 or more times in the file
                    if (line.contains("NOTA"))
                    {    //NOTA does not have sex, age, category and party
                        fieldCounter = 4;           //So these fields are skipped
                    }
                    Matcher candidateMatcher = candidatePattern.matcher(line);
                    Matcher constituencyMatcher = constituencyPattern.matcher(line);
                    if (candidateMatcher.find()) {  //If a candidate name has been found
                        if (candidate == null) {    //If it is a new candidate
                            candidate = new Candidate();
                            fieldCounter = 1;
                        }
                        candidate.name = line;

                        constituencyFlag = 1;   //The flag signals that a candidate has been found
                        nextReader = lnr;       //in the current constituency
                        if (!(line.contains("None of the Above"))) {
                            while (!(nextLine = nextReader.readLine()).isEmpty()) { //Dealing with cases where long
                                nextLine = nextLine.trim();                         //names spill over to the next line
                                if (nextLine.contains("TURNOUT")) { //Dealing with exceptions
                                    continue;
                                }
                                candidate.name = candidate.name.concat(" " + nextLine);
                            }
                        }
                        // System.out.println(candidate.name);
                    }
                    if (constituencyMatcher.find()) {   //If a new constituency name has been found
                        if (constituencyFlag == 1) {    //If this isn't the first constituency
                            String[] s2 = new String[2];
                            s2 = constituency.name.split(" ", 2);//split the serial number and constituency name
                            constituency.sNumber = s2[0];//this keeps the serial number
                            constituency.name = s2[1];//the name is stored here
                            writeResultsIntoFile(constituency, csvp);//Writing the details of the previous constituency
                            constituency = new Constituency();      //onto the file
                        }
                        constituency.name = line;
                        constituencyFlag = 0;
                    }
                    if (fieldCounter > 0 && fieldCounter <= 7) {    //If the number of fields found is at least 1
                        if (!line.isEmpty()) {                      //but less than 7
                            switch (fieldCounter) {
                                case 1:
                                    if(EXCLUSIONS[0] == 1){
                                        candidate.sex = "N/A";
                                        fieldCounter++;
                                        break;
                                    }
                                    Matcher sexMatcher = sexPattern.matcher(line);
                                    if (sexMatcher.find()) {
                                        if (candidate == null) {
                                            candidate = new Candidate();
                                        }
                                        candidate.sex = line;
                                        fieldCounter++;
                                    }
                                    break;
                                case 2:
                                    if(EXCLUSIONS[1] == 1){
                                        candidate.age = "N/A";
                                        fieldCounter++;
                                        break;
                                    }
                                    Matcher agematcher = agePattern.matcher(line);
                                    if (agematcher.find()) {
                                        candidate.age = line;
                                        fieldCounter++;
                                    }
                                    break;
                                case 3:
                                    if(EXCLUSIONS[2] == 1){
                                        candidate.category = "N/A";
                                        fieldCounter++;
                                        break;
                                    }
                                    Matcher categorymatcher = categoryPattern.matcher(line);
                                    if (categorymatcher.find()) {
                                        candidate.category = line;
                                        fieldCounter++;
                                    }
                                    break;
                                case 4:
                                    Matcher partymatcher = partyPattern.matcher(line);
                                    if (partymatcher.find()) {
                                        if (candidate == null) {
                                            candidate = new Candidate();
                                            candidate.sex = "N/A";
                                            candidate.age = "N/A";
                                            candidate.category = "N/A";
                                        }
                                        candidate.party = line;
                                        nextReader = lnr;
                                        while (!(nextLine = nextReader.readLine()).isEmpty()) {
                                            nextLine = nextLine.trim();
                                            if (nextLine.toLowerCase().contains("page")) {
                                                continue;
                                            }
                                            candidate.party = candidate.party.concat(" " + nextLine);
                                        }
                                        fieldCounter++;
                                    }
                                    break;
                                case 5:
                                    if(EXCLUSIONS[3] == 1){
                                        candidate.general = "N/A";
                                        fieldCounter++;
                                        break;
                                    }
                                    Matcher generalmatcher = generalPattern.matcher(line);
                                    if (generalmatcher.find()) {
                                        candidate.general = line;
                                        fieldCounter++;
                                    }
                                    break;
                                case 6:
                                    if(EXCLUSIONS[4] == 1){
                                        candidate.postal = "N/A";
                                        fieldCounter++;
                                        break;
                                    }
                                    Matcher postalmatcher = postalPattern.matcher(line);
                                    if (postalmatcher.find()) {
                                        candidate.postal = line;
                                        fieldCounter++;
                                    }
                                    break;
                                case 7:
                                    Matcher totalmatcher = totalPattern.matcher(line);
                                    if (totalmatcher.find()) {
                                        candidate.total = line;
                                        fieldCounter++;
                                    }
                                    break;
                            }
                        }
                    }
                    if (fieldCounter > 7 && candidate.name != null) {
                        String[] s1 = new String[2];
                        s1 = candidate.name.split(" ", 2);
                        candidate.sNumber = s1[0];
                        candidate.name = s1[1];
                        constituency.candidates.add(candidate);
                        fieldCounter = 1;
                        candidate = null;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {

                System.out.println("Error while flushing/closing file");
            }
        }
    }
    /*
        Method that takes a file as input and reads the details of each constituency and
        all the candidates under that constituency and writes all these details to a
        csv file. This method works for files that follow layout 2.
     */
    public static void readResultsFromLayout2(File resultfile) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("C:/Users/SHREYASH/Desktop/Scalable/res3.csv");
            CSVPrinter csvp = null;
            CSVFormat csvf = CSVFormat.DEFAULT.withRecordSeparator(newLineSeparator);
            csvp = new CSVPrinter(fileWriter, csvf);
            csvp.printRecord(fileHeader);
            Constituency constituency = new Constituency();
            Candidate candidate = null;
            LineNumberReader lnr = new LineNumberReader(new java.io.FileReader(resultfile));
            LineNumberReader nextLineReader;
            String line, nextLine = null;
            int flag = 0, constituencyFlag = 0, arrayCounter = 0, nextLineFlag = 0;
            Pattern constituencyPattern1 = Pattern.compile("(\\A)(\\s{25,200})(\\d+)(\\s*)([.])(\\s)(\\w+)");
            Pattern constituencyPattern2 = Pattern.compile("(\\A)([Constituency])(\\w+)");
            Pattern candidatePattern = Pattern.compile("(\\A)(\\s*)(\\d+)(\\s)([.])(\\s)(\\w+)");
            Pattern candidateSpilloverPattern = Pattern.compile("(\\A)(\\s){1,10}(\\w+)");
            Pattern partySpilloverPattern = Pattern.compile("(\\A)(\\s){30,100}(\\w+)");
            line = lnr.readLine();
            while (line != null) {
                nextLineFlag = 0;
                if (line.toLowerCase().contains("detailed results")) {
                    flag++;
                }
                if (flag >= 1) {
                    nextLineReader = lnr;
                    Matcher constituencyMatcher1 = constituencyPattern1.matcher(line);
                    Matcher constituencyMatcher2 = constituencyPattern2.matcher(line);
                    Matcher candidateMatcher = candidatePattern.matcher(line);
                    if (candidateMatcher.find()) {
                        candidate = new Candidate();
                        constituencyFlag = 1;
                        String[] candidateArray = line.split("(\\s{2,100})", 8);
                        nextLine = nextLineReader.readLine();
                        nextLineFlag = 1;
                        Matcher candidateSpilloverMatcher = candidateSpilloverPattern.matcher(nextLine);
                        Matcher partySpilloverMatcher = partySpilloverPattern.matcher(nextLine);
                        if (candidateSpilloverMatcher.find()) {
                            candidateArray[0] = candidateArray[0].concat(" " + nextLine.trim());
                        }
                        else if (partySpilloverMatcher.find()) {
                            candidateArray[4] = candidateArray[4].concat(nextLine.trim());
                        }
                        for(String str:candidateArray){
                            if(!str.isEmpty()){
                                candidate.name = str;
                                arrayCounter++;
                                break;
                            }
                            arrayCounter++;
                        }
                        String[] s1;
                        s1 = candidate.name.split("([.])", 2);
                        candidate.sNumber = s1[0];
                        candidate.name = s1[1];
                        if(EXCLUSIONS[0] == 1){
                            candidate.sex = "N/A";
                        }
                        else {
                            candidate.sex = candidateArray[arrayCounter];
                            arrayCounter++;
                        }
                        if(EXCLUSIONS[1] == 1){
                            candidate.age = "N/A";
                        }
                        else {
                            candidate.age = candidateArray[arrayCounter];
                            arrayCounter++;
                        }
                        if(EXCLUSIONS[2] == 1){
                            candidate.category = "N/A";
                        }
                        else {
                            candidate.category = candidateArray[arrayCounter];
                            arrayCounter++;
                        }
                        candidate.party = candidateArray[arrayCounter];
                        arrayCounter++;
                        if(EXCLUSIONS[3] == 1){
                            candidate.general = "N/A";
                        }
                        else {
                            candidate.general = candidateArray[arrayCounter];
                            arrayCounter++;
                        }
                        if(EXCLUSIONS[4] == 1){
                            candidate.postal = "N/A";
                        }
                        else {
                            candidate.postal = candidateArray[arrayCounter];
                            arrayCounter++;
                        }
                        candidate.total = candidateArray[arrayCounter];
                        constituency.candidates.add(candidate);
                        arrayCounter = 0;
                        candidate = null;
                    }
                    if (constituencyMatcher1.find() || constituencyMatcher2.find()) {
                        if (line.contains(":")) {
                            String[] lineArray = line.split("[:]", 2);
                            line = lineArray[1];
                        }
                        if (constituencyFlag == 1) {
                            String[] s2;
                            s2 = constituency.name.split("[.]", 2);
                            constituency.sNumber = s2[0].trim();
                            constituency.name = s2[1].trim();
                            writeResultsIntoFile(constituency, csvp);
                            constituency = new Constituency();
                        }
                        constituency.name = line;
                        constituencyFlag = 0;
                    }
                }
                if(nextLineFlag == 0){
                    line = lnr.readLine();
                }
                else if(nextLine != null){
                    line = nextLine;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing file");
            }
        }
    }

    public static void main(String[] args) {
        String s = new String();
        int layout = 0;
        try {
            File electionResults = new File("C:/Users/SHREYASH/Desktop/Scalable/Delhi2003L1.txt"); //Path of file to be parsed
            File exclusionsFile = new File("C:/Users/SHREYASH/Desktop/Scalable/FileDetails.txt");
            LineNumberReader fileDetailReader = new LineNumberReader(new java.io.FileReader(exclusionsFile));
            while ((s = fileDetailReader.readLine()) != null) {
                if (s.toLowerCase().contains("sex")) {
                    EXCLUSIONS[0] = 1;
                }
                if (s.toLowerCase().contains("age")) {
                    EXCLUSIONS[1] = 1;
                }
                if (s.toLowerCase().contains("category")) {
                    EXCLUSIONS[2] = 1;
                }
                if (s.toLowerCase().contains("general")) {
                    EXCLUSIONS[3] = 1;
                }
                if (s.toLowerCase().contains("postal")) {
                    EXCLUSIONS[4] = 1;
                }
                if (s.toLowerCase().contains("layout1")) {
                    layout = 1;
                }
                if (s.toLowerCase().contains("layout2")) {
                    layout = 2;
                }
            }
            if (layout == 1) {
                readResultsFromLayout1(electionResults);
            } else if (layout == 2) {
                readResultsFromLayout2(electionResults);
            } else {
                System.out.println("Error! Layout not specified");
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}