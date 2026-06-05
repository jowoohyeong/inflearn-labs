package com.example.elastic.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * QWERTY 영문 입력을 한글 조합형 입력으로 변환하는 유틸리티 클래스
 * 예: "skfrem" -> "나랑드"
 */
public class HangulKeyConverter {

    // 초성 19자
    private static final String[] CHOSUNG = {
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };

    // 중성 21자
    private static final String[] JOONGSUNG = {
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
    };

    // 종성 28자 (0번은 비어있음)
    private static final String[] JONGSUNG = {
        "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };

    // QWERTY 키 -> 한글 자모 매핑
    private static final Map<Character, String> KEY_MAP = new HashMap<>();
    static {
        String eng = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        String kor = "ㅂㅈㄷㄱㅅㅛㅕㅑㅐㅔㅁㄴㅇㄹㅎㅗㅓㅏㅣㅋㅌㅊㅍㅠㅜㅡㅃㅉㄸㄲㅆㅛㅕㅑㅒㅖㅁㄴㅇㄹㅎㅗㅓㅏㅣㅋㅌㅊㅍㅠㅜㅡ";
        for (int i = 0; i < eng.length(); i++) {
            KEY_MAP.put(eng.charAt(i), String.valueOf(kor.charAt(i)));
        }
    }

    // 복합 자음 조합 규칙 (종성에 사용)
    private static final Map<String, String> DOUBLE_CONS = new HashMap<>();
    static {
        DOUBLE_CONS.put("ㄱㅅ", "ㄳ");
        DOUBLE_CONS.put("ㄴㅈ", "ㄵ");
        DOUBLE_CONS.put("ㄴㅎ", "ㄶ");
        DOUBLE_CONS.put("ㄹㄱ", "ㄺ");
        DOUBLE_CONS.put("ㄹㅁ", "ㄻ");
        DOUBLE_CONS.put("ㄹㅂ", "ㄼ");
        DOUBLE_CONS.put("ㄹㅅ", "ㄽ");
        DOUBLE_CONS.put("ㄹㅌ", "ㄾ");
        DOUBLE_CONS.put("ㄹㅍ", "ㄿ");
        DOUBLE_CONS.put("ㄹㅎ", "ㅀ");
        DOUBLE_CONS.put("ㅂㅅ", "ㅄ");
    }

    // 복합 모음 조합 규칙 (중성에 사용)
    private static final Map<String, String> DOUBLE_VOWELS = new HashMap<>();
    static {
        DOUBLE_VOWELS.put("ㅗㅏ", "ㅘ");
        DOUBLE_VOWELS.put("ㅗㅐ", "ㅙ");
        DOUBLE_VOWELS.put("ㅗㅣ", "ㅚ");
        DOUBLE_VOWELS.put("ㅜㅓ", "ㅝ");
        DOUBLE_VOWELS.put("ㅜㅔ", "ㅞ");
        DOUBLE_VOWELS.put("ㅜㅣ", "ㅟ");
        DOUBLE_VOWELS.put("ㅡㅣ", "ㅢ");
    }

    /**
     * 영문 입력 문자열을 한글 문자열로 변환합니다.
     */
    public static String convert(String englishText) {
        if (englishText == null || englishText.isEmpty()) {
            return englishText;
        }

        StringBuilder result = new StringBuilder();
        
        // 입력 문자열을 한글 자모로 변환
        StringBuilder jamoList = new StringBuilder();
        for (int i = 0; i < englishText.length(); i++) {
            char c = englishText.charAt(i);
            if (KEY_MAP.containsKey(c)) {
                jamoList.append(KEY_MAP.get(c));
            } else {
                jamoList.append(c);
            }
        }

        // 오토마타를 이용해 자모 결합
        int state = 0; // 0:초성 대기, 1:초성 입력됨, 2:중성 입력됨, 3:종성 입력됨
        int choIdx = -1, jungIdx = -1, jongIdx = 0;
        String doubleJung = "", doubleJong = "";

        for (int i = 0; i < jamoList.length(); i++) {
            char c = jamoList.charAt(i);
            String charStr = String.valueOf(c);
            boolean isVowel = isVowel(c);
            boolean isConsonant = isConsonant(c);

            if (!isVowel && !isConsonant) {
                // 한글 자모가 아님 (공백, 숫자, 특수문자 등)
                if (state > 0) {
                    result.append(combine(choIdx, jungIdx, jongIdx));
                    state = 0;
                    choIdx = -1; jungIdx = -1; jongIdx = 0;
                    doubleJung = ""; doubleJong = "";
                }
                result.append(c);
                continue;
            }

            switch (state) {
                case 0: // 초성 입력 대기
                    if (isConsonant) {
                        choIdx = getChosungIndex(charStr);
                        state = 1;
                    } else {
                        // 초성 없이 모음만 오는 경우
                        result.append(c);
                    }
                    break;

                case 1: // 초성이 입력된 상태
                    if (isVowel) {
                        jungIdx = getJoongsungIndex(charStr);
                        state = 2;
                    } else {
                        // 초성이 입력되었는데 또 자음이 온 경우 (자음 연속: 예 "ㄱㄱ")
                        result.append(CHOSUNG[choIdx]);
                        choIdx = getChosungIndex(charStr);
                    }
                    break;

                case 2: // 초성 + 중성이 입력된 상태
                    if (isVowel) {
                        // 모음 결합 시도 (예: ㅗ + ㅏ = ㅘ)
                        String combinedVowel = DOUBLE_VOWELS.get(JOONGSUNG[jungIdx] + charStr);
                        if (combinedVowel != null) {
                            jungIdx = getJoongsungIndex(combinedVowel);
                        } else {
                            // 모음 조합 불가한 경우 분리
                            result.append(combine(choIdx, jungIdx, 0));
                            state = 0;
                            i--; // 현재 모음 재처리
                        }
                    } else {
                        // 자음이 오면 종성 후보로 대입
                        int tempJong = getJongsungIndex(charStr);
                        if (tempJong > 0) {
                            jongIdx = tempJong;
                            doubleJong = charStr;
                            state = 3;
                        } else {
                            // 종성으로 쓰일 수 없는 자음인 경우 (예: ㄸ, ㅃ, ㅉ)
                            result.append(combine(choIdx, jungIdx, 0));
                            choIdx = getChosungIndex(charStr);
                            state = 1;
                        }
                    }
                    break;

                case 3: // 초성 + 중성 + 종성이 입력된 상태
                    if (isVowel) {
                        // 종성이 다음 글자의 초성으로 넘어가야 함 (예: 한 + ㅡ -> 하 + 느, 닭 + ㅡ -> 달 + 그)
                        if (doubleJong.length() == 2) {
                            int prevJongIdx = getJongsungIndex(String.valueOf(doubleJong.charAt(0)));
                            result.append(combine(choIdx, jungIdx, prevJongIdx));
                            choIdx = getChosungIndex(String.valueOf(doubleJong.charAt(1)));
                        } else {
                            result.append(combine(choIdx, jungIdx, 0));
                            choIdx = getChosungIndex(doubleJong);
                        }
                        jungIdx = getJoongsungIndex(charStr);
                        jongIdx = 0;
                        doubleJong = "";
                        state = 2;
                    } else {
                        // 자음이 더 오면 겹받침 시도 (예: ㄱ + ㅅ = ㄳ)
                        String combinedCons = DOUBLE_CONS.get(doubleJong + charStr);
                        if (combinedCons != null) {
                            jongIdx = getJongsungIndex(combinedCons);
                            doubleJong = doubleJong + charStr; // 겹받침 기억
                        } else {
                            // 겹받침이 안 되면 기존 글자 완성하고 새 초성 시작
                            result.append(combine(choIdx, jungIdx, jongIdx));
                            choIdx = getChosungIndex(charStr);
                            jungIdx = -1;
                            jongIdx = 0;
                            doubleJong = "";
                            state = 1;
                        }
                    }
                    break;
            }
        }

        // 마지막 글자 처리
        if (state > 0) {
            result.append(combine(choIdx, jungIdx, jongIdx));
        }

        return result.toString();
    }

    private static boolean isVowel(char c) {
        String s = String.valueOf(c);
        for (String v : JOONGSUNG) {
            if (v.equals(s)) return true;
        }
        return "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅛㅜㅠㅡㅣ".contains(s);
    }

    private static boolean isConsonant(char c) {
        String s = String.valueOf(c);
        for (String co : CHOSUNG) {
            if (co.equals(s)) return true;
        }
        for (String jo : JONGSUNG) {
            if (jo.equals(s)) return true;
        }
        return "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".contains(s);
    }

    private static int getChosungIndex(String s) {
        for (int i = 0; i < CHOSUNG.length; i++) {
            if (CHOSUNG[i].equals(s)) return i;
        }
        return 0;
    }

    private static int getJoongsungIndex(String s) {
        for (int i = 0; i < JOONGSUNG.length; i++) {
            if (JOONGSUNG[i].equals(s)) return i;
        }
        return 0;
    }

    private static int getJongsungIndex(String s) {
        for (int i = 0; i < JONGSUNG.length; i++) {
            if (JONGSUNG[i].equals(s)) return i;
        }
        return 0;
    }

    private static char combine(int cho, int jung, int jong) {
        if (cho == -1 || jung == -1) {
            // 결합 불가
            if (cho != -1) return CHOSUNG[cho].charAt(0);
            if (jung != -1) return JOONGSUNG[jung].charAt(0);
            return ' ';
        }
        return (char) ((cho * 21 + jung) * 28 + jong + 0xAC00);
    }
}
