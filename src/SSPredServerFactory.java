/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mrahman
 */
public class SSPredServerFactory {
    public static SSPredServer getSSPredServer(String strName) {
        SSPredServer predictor = null;
        if (0 == strName.compareToIgnoreCase("spot1d")) {
            predictor = new Spot1D();
        } else if (0 == strName.compareToIgnoreCase("netsurfp")) {
            predictor = new NetSurfP();
        } else {
            throw new IllegalArgumentException("Unknown SS predictor requested.");
        }
        
        return predictor;        
    }
    
}
