package com.practice.java_refresher;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class PracticeProblems {

    @Test
    public void testSumTwo() {
        int [] nums = {2, 7, 11, 15};
        int target = 9;

        int[] result = new int[2];
        Map<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < nums.length; i++){
            int key = nums[i];
            int hasKey = target - key ;
            if(map.containsKey(hasKey)){
                result[0] = map.get(hasKey);
                result[1] = i;
                break;
            }
            map.put(key, i);
        }
        System.out.println(result[0] + " " + result[1]);
    }

    @Test
    public void isValid( ){
        boolean isValid = isValid("()[]{}");
        System.out.println(isValid);
        isValid = isValid("(]");
        System.out.println(isValid);
    }

    public boolean isValid(String s){
        Stack<Character> stack = new Stack<>();
        for(int i = 0; i <s.length(); i++){
            char c = s.charAt(i);
            if(c == ')'){
                if(stack.isEmpty() || stack.pop() != '('){
                    return false;
                }
            }else if(c == '}'){
                if(stack.isEmpty() || stack.pop() != '{'){
                    return false;
                }
            }else if(c == ']'){
                if(stack.isEmpty() || stack.pop() != '['){
                    return false;
                }
            } else {
                stack.push(c);
            }
        }
        return stack.isEmpty();
    }

}
