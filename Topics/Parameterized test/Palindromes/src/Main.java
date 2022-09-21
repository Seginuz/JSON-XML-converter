class StringUtils {
    public static boolean isPalindrome(String str) {
        StringBuilder cleanedString = new StringBuilder();
        for (String s : str.split("")) {
            if (s.matches("[a-zA-Z]")) {
                cleanedString.append(s.toLowerCase());
            }
        }

        if (!cleanedString.isEmpty()) {
            return cleanedString.toString().equals(cleanedString.reverse().toString());
        } else {
            return false;
        }
    }
}