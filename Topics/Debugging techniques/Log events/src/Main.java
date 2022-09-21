class Util {
    public static String capitalize(String str) {
        System.out.println("Before: " + str);

        String capitalizedString;
        if (str == null || str.isBlank()) {
            capitalizedString = str;
        } else if (str.length() == 1) {
            capitalizedString = str.toUpperCase();
        } else {
            capitalizedString = Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }

        System.out.println("After: " + capitalizedString);
        return capitalizedString;
    }
}