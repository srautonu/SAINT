
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mrahman
 */
public class LogToListConverter {
    public static void main(String args[]) {
         Scanner in = new Scanner(System.in);
         
         while (in.hasNextLine()) {
             String strId, strJobId;
             String strLine = in.nextLine().trim();
             if (strLine.isEmpty())
                 break;
             
             int i = strLine.indexOf("Job Id for");
             int j = strLine.indexOf("is");
             
             strId = strLine.substring(i + 11, j - 1).trim();
             strJobId = strLine.substring(j + 2).trim();
             System.out.println(strId + "," + strJobId);           
         }
    }
}
