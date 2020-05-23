package com.task;


import java.math.BigDecimal;
import java.sql.Wrapper;
import java.util.*;

public class Main {

    public static BigDecimal calc(String sourceString, Integer scale) {

        // 1. zamiana sourceString na listę znaków
        List<Character> sourceStringToChars = new ArrayList<>();
        for (int j = 0; j < sourceString.length(); j++) {
            if (sourceString.charAt(j) != ' ') {
                sourceStringToChars.add(sourceString.charAt(j));
            }
        }

        // 2. zapisanie do nowej listy wyodrębnionych z sourceStringToChars liczb i znaków
        List<String> numbersAndCharsList = new ArrayList<>();

        int lastListIndex = 0;
        for (int i = 0; i < sourceStringToChars.size(); i = i) {
            if (sourceStringToChars.get(i).toString().matches("[0-9]") | sourceStringToChars.get(i) == '.') {
                do {
                    String numberToAddToList = "";
                    numbersAndCharsList.add(numberToAddToList);
                    String partialNumber = numbersAndCharsList.get(lastListIndex);
                    numberToAddToList = partialNumber + sourceStringToChars.get(i);
                    numbersAndCharsList.set(lastListIndex, numberToAddToList);
                    if (i < sourceStringToChars.size() - 1) {
                        if (!(sourceStringToChars.get(i + 1).toString().matches("[0-9]") | sourceStringToChars.get(i + 1) == '.')) {
                            lastListIndex++;
                        }
                    }
                    i++;
                    if (i == sourceStringToChars.size()) break;
                } while (sourceStringToChars.get(i).toString().matches("[0-9]") | sourceStringToChars.get(i) == '.');
            } else {
                do {
                    String charToAddToList = "";
                    numbersAndCharsList.add(charToAddToList);
                    charToAddToList = sourceStringToChars.get(i).toString();
                    numbersAndCharsList.set(lastListIndex, charToAddToList);
                    if (i < sourceStringToChars.size() - 1) {
                        if (!(sourceStringToChars.get(i + 1).toString().matches("[0-9]") | sourceStringToChars.get(i + 1) == '.')) {
                            lastListIndex++;
                        }
                    }
                    i++;
                    if (i == sourceStringToChars.size()) break;
                } while (!(sourceStringToChars.get(i).toString().matches("[0-9]") | sourceStringToChars.get(i) == '.'));
                lastListIndex++;
            }
        }

        // 3. utworzenie mapy liczb- KEY: index liczby w numbersAndCharsList, VALUE: wartość double liczby
        TreeMap<Integer, Double> numbersMap = new TreeMap<>();

        for (int i = 0; i < numbersAndCharsList.size(); i++) {
            String stringToParse = numbersAndCharsList.get(i);
            if (stringToParse.matches("[0-9]+.*[0-9]*")) {
                double parseString = Double.parseDouble(stringToParse);
                numbersMap.put(i, parseString);
            }
        }

        // 4. znalezienie równania w nawiasach: najbardziej zagnieżdżonego lub ostatniego
        if (numbersAndCharsList.contains("{")) {

            do {
                int openingBracketIndex1 = numbersAndCharsList.lastIndexOf("{");
                int closingBracketIndex = 0;
                for (int i = openingBracketIndex1; i < numbersAndCharsList.size(); i++) {
                    if (numbersAndCharsList.get(i).equals("}")) {
                        closingBracketIndex = i;
                        break;
                    }
                }

                double partResult = 0;

                List<String> contentInBrackets1 = new ArrayList<>();
                for (int i = openingBracketIndex1 + 1; i < closingBracketIndex; i++) {
                    contentInBrackets1.add(numbersAndCharsList.get(i));
                }

                for (int i = openingBracketIndex1 + 1; i < closingBracketIndex; i++) {

                    // 4A. wykonanie działań mnożenia i dzielenia w nawiasach od lewej do prawej jeśli występują
                    if (contentInBrackets1.contains("/") | contentInBrackets1.contains("*")) {
                        contentInBrackets1.remove(0);
                        if (numbersAndCharsList.get(i).equals("/")) {

                            int floor = numbersMap.floorKey(i);
                            int ceiling = numbersMap.ceilingKey(i);
                            partResult = numbersMap.get(floor) / (numbersMap.get(ceiling));
                            numbersMap.replace(floor, partResult);

                            // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                            // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                            if (ceiling != numbersMap.lastKey()) {
                                for (Integer v : numbersMap.keySet()) {
                                    if (v >= ceiling & v < numbersMap.lastKey()) {
                                        int temp = numbersMap.higherKey(v);
                                        double tempor = numbersMap.get(temp);
                                        numbersMap.replace(v, tempor);
                                        v = temp;
                                    } else if (v == numbersMap.lastKey()) {
                                        numbersMap.remove(v);
                                    }
                                }
                            } else {
                                numbersMap.remove(ceiling);
                            }

                            numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                            numbersAndCharsList.remove(i);
                            numbersAndCharsList.remove(i);
                            closingBracketIndex = closingBracketIndex - 2;

                        } else if (numbersAndCharsList.get(i).equals("*")) {

                            int floor = numbersMap.floorKey(i);
                            int ceiling = numbersMap.ceilingKey(i);

                            partResult = numbersMap.get(floor) * (numbersMap.get(ceiling));
                            numbersMap.replace(floor, partResult);

                            // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                            // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                            if (ceiling != numbersMap.lastKey()) {
                                for (Integer v : numbersMap.keySet()) {
                                    if (v >= ceiling & v < numbersMap.lastKey()) {
                                        int temp = numbersMap.higherKey(v);
                                        double tempor = numbersMap.get(temp);
                                        numbersMap.replace(v, tempor);
                                        v = temp;
                                    } else if (v == numbersMap.lastKey()) {
                                        numbersMap.remove(v);
                                    }
                                }
                            } else {
                                numbersMap.remove(ceiling);
                            }

                            numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                            numbersAndCharsList.remove(i);
                            numbersAndCharsList.remove(i);
                            closingBracketIndex = closingBracketIndex - 2;
                        }

                    }
                }


                int openingBracketIndex2 = numbersAndCharsList.lastIndexOf("{");

                // utworzenie kolejnej listy chyba jest niepotrzebne ?
                List<String> contentInBrackets2 = new ArrayList<>();
                for (int i = openingBracketIndex2 + 1; i < closingBracketIndex; i++) {
                    contentInBrackets2.add(numbersAndCharsList.get(i));
                }

                // 4B. wykonanie działań dodawania i odejmowania w nawiasach od lewej do prawej jeśli występują
                for (int i = openingBracketIndex2 + 1; i < closingBracketIndex; i++) {
                    if (contentInBrackets2.contains("+") | contentInBrackets2.contains("-")) {
                        contentInBrackets2.remove(0);
                        if (numbersAndCharsList.get(i).equals("+")) {
                            int floor = numbersMap.floorKey(i);
                            int ceiling = numbersMap.ceilingKey(i);
                            partResult = numbersMap.get(floor) + (numbersMap.get(ceiling));
                            numbersMap.replace(floor, partResult);

                            // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                            // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                            if (ceiling != numbersMap.lastKey()) {
                                for (Integer v : numbersMap.keySet()) {
                                    if (v >= ceiling & v < numbersMap.lastKey()) {
                                        int temp = numbersMap.higherKey(v);
                                        double tempor = numbersMap.get(temp);
                                        numbersMap.replace(v, tempor);
                                        v = temp;
                                    } else if (v == numbersMap.lastKey()) {
                                        numbersMap.remove(v);
                                    }
                                }
                            } else {
                                numbersMap.remove(ceiling);
                            }

                            numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                            numbersAndCharsList.remove(i);
                            numbersAndCharsList.remove(i);
                            closingBracketIndex = closingBracketIndex - 2;
                        } else if (numbersAndCharsList.get(i).equals("-")) {

                            int floor = numbersMap.floorKey(i);
                            int ceiling = numbersMap.ceilingKey(i);
                            partResult = numbersMap.get(floor) - (numbersMap.get(ceiling));

                            numbersMap.replace(floor, partResult);
                            // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                            // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                            if (ceiling != numbersMap.lastKey()) {
                                for (Integer v : numbersMap.keySet()) {
                                    if (v >= ceiling & v < numbersMap.lastKey()) {
                                        int temp = numbersMap.higherKey(v);
                                        double tempor = numbersMap.get(temp);
                                        numbersMap.replace(v, tempor);
                                        v = temp;
                                    } else if (v == numbersMap.lastKey()) {
                                        numbersMap.remove(v);
                                    }
                                }
                            } else {
                                numbersMap.remove(ceiling);
                            }

                            numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                            numbersAndCharsList.remove(i);
                            numbersAndCharsList.remove(i);
                            closingBracketIndex = closingBracketIndex - 2;

                        }
                    }
                }

                numbersAndCharsList.remove(openingBracketIndex2);
                numbersAndCharsList.remove(closingBracketIndex - 1);

                // nowa mapa ponieważ odpowiadające liczbom index-y w liście i KEY w mapie nie są już równe
                numbersMap = new TreeMap<>();
                for (int i = 0; i < numbersAndCharsList.size(); i++) {
                    String tt = numbersAndCharsList.get(i);
                    if (tt.matches("-*[0-9]+.*[0-9]*")) {
                        double parseString = Double.parseDouble(tt);
                        numbersMap.put(i, Double.parseDouble(tt));
                    }
                }

            } while (numbersAndCharsList.contains("{"));

        }

        //===============================================================================================

        // Exception in thread "main" java.util.ConcurrentModificationException

        // 1.  foreach
        // 2.  for (Iterator<String> it = numbersAndCharsList.iterator(); it.hasNext();) {
        //        String str = it.next();
        ////        if(next % 10 == 0)
        ////            numbers.remove(next);
        //
        //    }


        // 5. wykonanie działań mnożenia i dzielenia bez nawiasów od lewej do prawej
        if (numbersAndCharsList.contains("*") | numbersAndCharsList.contains("/")) {


            for (int i = 0; i < numbersAndCharsList.size(); i++) {

                if (numbersAndCharsList.get(i).equals("*")) {

                    double partResult = 0;
                    int floor = numbersMap.floorKey(i);
                    int ceiling = numbersMap.ceilingKey(i);
                    partResult = numbersMap.get(floor) * (numbersMap.get(ceiling));

                    numbersMap.replace(floor, partResult);
                    // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                    // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                    if (ceiling != numbersMap.lastKey()) {
                        for (Integer v : numbersMap.keySet()) {
                            if (v >= ceiling & v < numbersMap.lastKey()) {
                                int temp = numbersMap.higherKey(v);
                                double tempor = numbersMap.get(temp);
                                numbersMap.replace(v, tempor);
                                v = temp;
                            } else if (v == numbersMap.lastKey()) {
                                numbersMap.remove(v);
                            }
                        }
                    } else {
                        numbersMap.remove(ceiling);
                    }

                    numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                    numbersAndCharsList.remove(i);
                    numbersAndCharsList.remove(i);
                    i = 0;

                } else if (numbersAndCharsList.get(i).equals("/")) {

                    double partResult = 0;
                    int floor = numbersMap.floorKey(i);
                    int ceiling = numbersMap.ceilingKey(i);
                    partResult = numbersMap.get(floor) / (numbersMap.get(ceiling));
                    numbersMap.replace(floor, partResult);
                    // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                    // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                    if (ceiling != numbersMap.lastKey()) {
                        for (Integer v : numbersMap.keySet()) {
                            if (v >= ceiling & v < numbersMap.lastKey()) {
                                int temp = numbersMap.higherKey(v);
                                double tempor = numbersMap.get(temp);
                                numbersMap.replace(v, tempor);
                                v = temp;
                            } else if (v == numbersMap.lastKey()) {
                                numbersMap.remove(v);
                            }
                        }
                    } else {
                        numbersMap.remove(ceiling);
                    }
                    numbersAndCharsList.set(i - 1, String.valueOf(partResult));
                    numbersAndCharsList.remove(i);
                    numbersAndCharsList.remove(i);
                    i = 0;
                }
            }
        }


        // 6. wykonanie działań mnożenia i dzielenia bez nawiasów od lewej do prawej
        if (numbersAndCharsList.contains("+") | numbersAndCharsList.contains("-")) {

            for (int i = 0; i < numbersAndCharsList.size(); i++) {
                if (numbersAndCharsList.get(i).equals("+")) {
                    double partResult = 0;
                    int lastPlusIndex = numbersAndCharsList.lastIndexOf("+");
                    int floor = numbersMap.floorKey(lastPlusIndex);
                    int ceiling = numbersMap.ceilingKey(lastPlusIndex);
                    partResult = numbersMap.get(floor) + (numbersMap.get(ceiling));

                    numbersMap.replace(floor, partResult);
                    if (ceiling != numbersMap.lastKey()) {
                        // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                        // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                        for (Integer v : numbersMap.keySet()) {
                            if (v >= ceiling & v < numbersMap.lastKey()) {
                                int temp = numbersMap.higherKey(v);
                                double tempor = numbersMap.get(temp);
                                numbersMap.replace(v, tempor);
                                v = temp;
                            } else if (v == numbersMap.lastKey()) {
                                numbersMap.remove(v);
                            }
                        }
                    } else {
                        numbersMap.remove(ceiling);
                    }
                    numbersAndCharsList.set(lastPlusIndex - 1, String.valueOf(partResult));
                    numbersAndCharsList.remove(lastPlusIndex);
                    numbersAndCharsList.remove(lastPlusIndex);
                    i = 0;

                } else if (numbersAndCharsList.get(i).equals("-")) {

                    double partResult = 0;
                    int lastMinusIndex = numbersAndCharsList.indexOf("-");
                    int floor = numbersMap.floorKey(lastMinusIndex);
                    int ceiling = numbersMap.ceilingKey(lastMinusIndex);
                    partResult = numbersMap.get(floor) - (numbersMap.get(ceiling));
                    numbersMap.replace(floor, partResult);
                    if (ceiling != numbersMap.lastKey()) {
                        // zmiana KEY w numbersMap na wartość równą index-owi w numbersAndCharsList wartości odpowiadającej VALUE
                        // umieszczenie wyniku działania w numbersAndCharsList powoduje zmianę indexu wartości będących liczbami w mapie
                        for (Integer v : numbersMap.keySet()) {
                            if (v >= ceiling & v < numbersMap.lastKey()) {
                                int temp = numbersMap.higherKey(v);
                                double tempor = numbersMap.get(temp);
                                numbersMap.replace(v, tempor);
                                v = temp;
                            } else if (v == numbersMap.lastKey()) {
                                numbersMap.remove(v);
                            }
                        }
                    } else {
                        numbersMap.remove(ceiling);
                    }
                    numbersAndCharsList.set(lastMinusIndex - 1, String.valueOf(partResult));
                    numbersAndCharsList.remove(lastMinusIndex);
                    numbersAndCharsList.remove(lastMinusIndex);

                    i = 0;
                }

            }
        }

        // 7. zaokrąglenie wyniku do liczby miejsc po przecinku równej scale
        String substringResult = "";
        int dotIndex = 0;
        double doubleResult = 0;
        int intResult = 0;
        String stringResult = numbersAndCharsList.get(0);
        if (stringResult.contains(".") & scale != 0) {
            dotIndex = stringResult.indexOf(".");
            int numAfterDot = stringResult.length() - (dotIndex + 1);
            if (scale < numAfterDot) {
//                stringResultWithSubstring = stringResult.substring(0, dotIndex + scale + 1);
                substringResult = stringResult.substring(0, dotIndex + scale + 2);
                int lastCharIndex = substringResult.length() - 1;
                char lastChar = substringResult.charAt(lastCharIndex);
                char penultimateChar = substringResult.charAt(lastCharIndex - 1);
                String substringResultRound = "";
                BigDecimal roundResult = null;
                if (lastChar == '5' | lastChar == '6' | lastChar == '7' | lastChar == '8' | lastChar == '9') {
                    int intPenultimateChar = Character.getNumericValue(penultimateChar);
                    for (int i = 0; i < 9; i++) {

                        if (intPenultimateChar == i) {
                            int iter = i + 1;
                            penultimateChar = Character.forDigit(iter, 10);
                            break;
                        }
                    }

                    substringResultRound = substringResult.substring(0, substringResult.length() - 2);
                    substringResultRound = substringResultRound + penultimateChar;
                    Double doubleAroundResult = Double.parseDouble(substringResultRound);
                    roundResult = BigDecimal.valueOf(doubleAroundResult);
                } else {

                    substringResult = stringResult.substring(0, dotIndex + scale + 1);
                    Double doubleAroundResult = Double.parseDouble(substringResult);
                    roundResult = BigDecimal.valueOf(doubleAroundResult);
                }

                return roundResult;

            } else {

                String stringResultWithoutDot = "";
                for (int i = 0; i < stringResult.length(); i++) {
                    if (stringResult.charAt(i) != '.') {
                        stringResultWithoutDot = stringResultWithoutDot + stringResult.charAt(i);
                    }
                }

                int zerosToScale = scale - dotIndex;
                String zeros = "";
                do {
                    zeros = zeros + "0";
                } while (zeros.length() == zerosToScale - 1);

                stringResultWithoutDot = stringResultWithoutDot + zeros;
                Long longResult = Long.parseLong(stringResultWithoutDot);
                BigDecimal bigdecimalResult = BigDecimal.valueOf(longResult, scale);

                return bigdecimalResult;
            }

        } else if (stringResult.contains(".") & scale == 0) {
            dotIndex = stringResult.indexOf(".");
            substringResult = stringResult.substring(0, dotIndex);
        } else {
            substringResult = stringResult;
        }

        BigDecimal result = null;
        if (substringResult.contains(".")) {
            doubleResult = Double.parseDouble(substringResult);
            result = BigDecimal.valueOf(doubleResult);
        } else {
            intResult = Integer.parseInt(substringResult);
            result = BigDecimal.valueOf(intResult);
        }

        return result;
    }

    public static void main(String[] args) {


        double a = 2 * 3 / 6 - 1 * 2 + 7;
        double b = 6 * 3 / 6.01 - 2 * 2.01 + 7.01 / 2;
        double c = 6 * 3 / 6.01 / 33 * 6 - 2 * (2.01 - 7.01 / 2);
        double d = 6 * (3 / 6.01) / 33 * (6 - 2) * 2.01 - 7.01 / 2;
        double e = 6 * 3 / 6.01 / (33 * (6 - 12)) - 2 * 2.01 - 7.01 / 2;

//        System.out.println("a: " + a);
//        System.out.println("b: " + b);
//        System.out.println("c: " + c);
//        System.out.println("d: " + d);
//        System.out.println("e: " + e);

//        System.out.println(calc("2 * 3 / 6 - 1 * 2 + 7", 2));  // a

//        System.out.println(calc("6 * 3 / 6.01 - 2 * 2.01 + 7.01 / 2", 2)); // b

//        System.out.println(calc("6 * 3 / 6.01 / 33 * 6 - 2 * {  2.01 - 7.01 / 2 }", 5));

//        System.out.println(calc("6 * { 3 / 6.01 } / 33 * { 6 - 2 } * 2.01 - 7.01 / 2", 2)); // d

        System.out.println(calc("6 * 3 / 6.01 / { 33 * { 6 - 12 } } - 2 * 2.01 - 7.01 / 2", 2)); // e


    }
}
