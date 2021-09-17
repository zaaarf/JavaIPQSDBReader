package com.ipqualityscore.JavaIPQSDBReader;

import java.util.HashMap;

public class FraudScore {
    public int forStrictness(int strictnesslevel){
        return strictness.get(strictnesslevel);
    }

    private HashMap<Integer, Integer> strictness = new HashMap<Integer, Integer>();
    public void setFraudScore(int fraudscore, int strictnesslevel){
        strictness.put(strictnesslevel, fraudscore);
    }
}