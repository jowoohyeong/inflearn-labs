package com.example.elastic.common.utils;

/**
 * 한글 문자열을 자소(초성, 중성, 종성) 단위로 완전히 분해하는 유틸리티 클래스
 * 예: "노트북" -> "ㄴㅗㅌㅡㅂㅜㄱ"
 */
public class JasoDecomposer {

    // 초성 19자
    private static final char[] CHOSUNG = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    // 중성 21자
    private static final char[] JOONGSUNG = {
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    // 종성 28자 (0번 인덱스는 종성 없음)
    private static final char[] JONGSUNG = {
        '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 한글 문자열을 자모 단위로 분해합니다.
     * 예: "갤럭시" -> "ㄱㅐㄹㄹㅓㄱㅅㅣ"
     */
    public static String decompose(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 한글 유니코드 범위 (AC00 ~ D7A3)
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int baseCode = c - 0xAC00;
                
                int cho = baseCode / (21 * 28);
                int jung = (baseCode % (21 * 28)) / 28;
                int jong = baseCode % 28;

                result.append(CHOSUNG[cho]);
                
                // 복합 모음 분해 처리 (선택사항이나 오타율 개선에 매우 좋음)
                decomposeVowel(JOONGSUNG[jung], result);
                
                if (jong > 0) {
                    decomposeJongsung(JONGSUNG[jong], result);
                }
            } else {
                // 한글 완성형이 아닌 경우 (영문, 숫자, 기호, 자모 낱개 등)
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * 복합 모음(ㅘ, ㅙ, ㅝ 등)을 단일 모음 조합으로 분해하여 입력합니다.
     */
    private static void decomposeVowel(char vowel, StringBuilder sb) {
        switch (vowel) {
            case 'ㅘ': sb.append("ㅗㅏ"); break;
            case 'ㅙ': sb.append("ㅗㅐ"); break;
            case 'ㅚ': sb.append("ㅗㅣ"); break;
            case 'ㅝ': sb.append("ㅜㅓ"); break;
            case 'ㅞ': sb.append("ㅜㅔ"); break;
            case 'ㅟ': sb.append("ㅜㅣ"); break;
            case 'ㅢ': sb.append("ㅡㅣ"); break;
            default: sb.append(vowel); break;
        }
    }

    /**
     * 복합 자음 받침(ㄳ, ㄶ, ㄺ 등)을 단일 자음 조합으로 분해하여 입력합니다.
     */
    private static void decomposeJongsung(char jong, StringBuilder sb) {
        switch (jong) {
            case 'ㄳ': sb.append("ㄱㅅ"); break;
            case 'ㄵ': sb.append("ㄴㅈ"); break;
            case 'ㄶ': sb.append("ㄴㅎ"); break;
            case 'ㄺ': sb.append("ㄹㄱ"); break;
            case 'ㄻ': sb.append("ㄹㅁ"); break;
            case 'ㄼ': sb.append("ㄹㅂ"); break;
            case 'ㄽ': sb.append("ㄹㅅ"); break;
            case 'ㄾ': sb.append("ㄹㅌ"); break;
            case 'ㄿ': sb.append("ㄹㅍ"); break;
            case 'ㅀ': sb.append("ㄹㅎ"); break;
            case 'ㅄ': sb.append("ㅂㅅ"); break;
            default: sb.append(jong); break;
        }
    }
}
