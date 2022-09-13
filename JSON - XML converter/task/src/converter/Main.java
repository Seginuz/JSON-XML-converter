package converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Main {

    public static final String FILE_NAME = "C:\\Users\\Stefan Koelewijn\\IdeaProjects\\JSON - XML converter\\JSON - XML converter\\task\\src\\converter\\input.txt";
//    public static final String FILE_NAME = "test.txt";

    public static void main(String[] args) {
        String input;

        try (FileReader fr = new FileReader(FILE_NAME);
             BufferedReader br = new BufferedReader(fr)) {
            input = br.lines().reduce((s1, s2) -> s1 + s2).orElse("").strip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Element> elements;
        if (input.startsWith("<")) {
            elements = parseXml(input);
            out.println(convertToJson(elements));
        } else if (input.startsWith("{")) {
            elements = parseJson(input);
            out.println(convertToXml(elements));
        } else {
            out.println("This isn't XML or JSON");
        }
    }

    private static List<Element> parseXml(String xml) {
        Matcher openingTagMatcher = Pattern.compile("<[\\w\"'= .]+/*>").matcher(xml);
        List<Element> elements = new ArrayList<>();
        Element element = null;

        if (openingTagMatcher.find()) {
            String openingTag = openingTagMatcher.group().replaceAll("[<>/]+", "");
            element = new Element(openingTag.split(" ")[0]);

            if (openingTagMatcher.group().contains("=")) {
                parseXmlAttributes(openingTag, element);
            }

            if (!openingTagMatcher.group().contains("/")) {
                int closingTagIndex = findXmlClosingTag(element.getKey(), xml);

                String content = xml.substring(openingTagMatcher.end(), closingTagIndex);
                if (content.strip().startsWith("<")) {
                    content = content.strip();

                    while (!content.isEmpty()) {
                        Element child = parseXml(content).get(0);
                        element.getChildren().add(child);

                        if (child.getValue() != null || !child.getChildren().isEmpty()) {
                            content = content.substring(findXmlClosingTag(child.getKey(), content)
                                    + child.getKey().length() + 3);
                        } else {
                            String[] split = content.split("/>", 2);
                            content = (split.length > 1) ? split[1] : "";
                        }
                    }
                } else {
                    element.setValue(content);
                }
            }
        }

        elements.add(element);
        return elements;
    }

    private static int findXmlClosingTag(String key, String xml) {
        int closingTagIndex = -1;

        int openingTagCount = 0;
        int closingTagCount = 0;

        Matcher tagMatcher = Pattern.compile(String.format("(<%s|</%s>)", key, key)).matcher(xml);
        if (tagMatcher.find()) {
            openingTagCount++;
            while (tagMatcher.find()) {
                if (tagMatcher.group().equals(String.format("<%s", key))) {
                    openingTagCount++;
                } else if (tagMatcher.group().equals(String.format("</%s>", key))) {
                    closingTagCount++;
                }
//                out.println(key + " opening: " + openingTagCount);
//                out.println(key + " closing: " + closingTagCount);

                if (openingTagCount == closingTagCount) {
                    closingTagIndex = tagMatcher.start();
                    break;
                }
            }
        }

        return closingTagIndex;
    }

    private static void parseXmlAttributes(String openingTag, Element element) {
        Matcher attrMatcher = Pattern.compile("\\w+ *= *[\"'][\\w. ]*[\"']").matcher(openingTag);

        while (attrMatcher.find()) {
            Matcher attrKeyMatcher = Pattern.compile("\\w+").matcher(attrMatcher.group());
            String attrKey = "";
            if (attrKeyMatcher.find()) {
                attrKey = attrKeyMatcher.group();
            }

            Matcher attrValMatcher = Pattern.compile("[\"'][\\w. ]*[\"']").matcher(attrMatcher.group());
            String attrVal = "";
            if (attrValMatcher.find()) {
                attrVal = attrValMatcher.group().replaceAll("[\"']", "");
            }

            element.getAttributes().put(attrKey, attrVal);
        }
    }

    private static String convertToXml(List<Element> elements) {
        StringBuilder builder = new StringBuilder();
        if (elements.size() == 1) {
            builder.append(convertChildrenToXml(elements));
        } else {
            builder.append("<root>\n\t");
            builder.append(convertChildrenToXml(elements).replaceAll("\n", "\n\t"));
            builder.replace(builder.length() - 1, builder.length(), "");
            builder.append("</root>");
        }
        return builder.toString();
    }

    private static String convertChildrenToXml(List<Element> elements) {
        StringBuilder builder = new StringBuilder();

        elements.forEach(element -> {
            builder.append(String.format("<%s", element.getKey()));
            if (!element.getAttributes().isEmpty()) {
                element.getAttributes().forEach((key, value) ->
                        builder.append(String.format(" %s=\"%s\"", key, value))
                );
            }
            if (!element.getChildren().isEmpty()) {
                builder.append(">\n\t");
                builder.append(convertChildrenToXml(element.getChildren()).replaceAll("\n", "\n\t"));
                builder.replace(builder.length() - 1, builder.length(), "");
                builder.append(String.format("</%s>\n", element.getKey()));
            } else {
                if (element.getValue() != null) {
                    builder.append(">").append(element.getValue()).append(String.format("</%s>\n", element.getKey()));
                } else {
                    builder.append("/>\n");
                }
            }
        });

        return builder.toString();
    }

    private static List<Element> parseJson(String json) {
        List<String> rawElements = splitJsonElements(json);
        List<Element> elements = new ArrayList<>();

        for (String elementJson : rawElements) {
            Matcher keyMatcher = Pattern.compile("\"[@#]?[\\w.]*\" *:\\s*").matcher(elementJson);

            if (keyMatcher.find()) {
                String key = keyMatcher.group().replaceAll("[{\":\\s]+", "");
                Element element = new Element(key);

                switch (elementJson.charAt(keyMatcher.end())) {
                    case 'n' -> element.setValue(null);
                    case '{' -> {
                        List<Element> children = parseJson(elementJson.substring(keyMatcher.end()));
                        if (children.isEmpty()) {
                            element.setValue("");
                            break;
                        }

                        for (Element c : children) {
                            if (c.getKey().startsWith("@") && !c.getChildren().isEmpty()) {
                                if (children.stream().noneMatch(fc -> fc.getKey().equals(c.getKey().replace("@", "")))) {
                                    c.setKey(c.getKey().replace("@", ""));
                                } else {
                                    children.remove(c);
                                }
                            }
                        }

                        boolean brokenAttributes = false;
                        for (Element child : children) {
                            if (child.getKey().startsWith("@") || child.getKey().isEmpty()) {
                                if (child.getKey().replace("@", "").isEmpty()
                                        || !child.getChildren().isEmpty()) {
                                    brokenAttributes = true;
                                    element.getAttributes().clear();
                                    break;
                                }
                                element.getAttributes().put(
                                        child.getKey().replace("@", ""),
                                        child.getValue() == null ? "" : child.getValue()
                                );
                            }
                        }

                        boolean brokenValue = true;
                        for (Element child : children) {
                            if (child.getKey().startsWith("#")) {
                                if (child.getKey().contains(key) && !brokenAttributes) {
                                    brokenValue = false;
                                    if (child.getChildren().isEmpty()) {
                                        element.setValue(child.getValue());
                                    }
                                }
                            } else if (!child.getKey().startsWith("@") || !child.getChildren().isEmpty()) {
                                brokenValue = true;
                                break;
                            }
                        }

                        if (!brokenValue) {
                            children.forEach(c -> {
                                if (c.getKey().startsWith("#") && !c.getChildren().isEmpty()) {
                                    element.getChildren().addAll(c.getChildren());
                                }
                            });
                        }

                        if (brokenValue) {
                            element.getAttributes().clear();
                            element.setValue(null);
                            for (Element child : children) {
                                if (child.getKey().startsWith("@")) {
                                    if (children.stream().noneMatch(c -> c.getKey().equals(child.getKey().replace("@", "")))) {
                                        child.setKey(child.getKey().replace("@", ""));
                                    }
                                } else if (child.getKey().startsWith("#")) {
                                    if (children.stream().noneMatch(c -> c.getKey().equals(child.getKey().replace("#", "")))) {
                                        child.setKey(child.getKey().replace("#", ""));
                                    }
                                }
                            }
                        }

                        children = children.stream()
                                .filter(Objects::nonNull)
                                .filter(c -> !(c.getKey().startsWith("@") || c.getKey().startsWith("#")))
                                .filter(c -> !c.getKey().isEmpty())
                                .collect(Collectors.toList());

                        if (children.isEmpty() && brokenValue) {
                            element.setValue("");
                        }
                        element.getChildren().addAll(children);
                    }
                    case '[' -> {
                        List<String> children = splitJsonElements(elementJson.substring(keyMatcher.end()))
                                .stream()
                                .filter(c -> !c.isEmpty())
                                .collect(Collectors.toList());
                        if (children.isEmpty()) {
                            element.setValue("");
                        } else {
                            parseJsonArray(element, children);
                        }
                    }
                    case '"' -> {
                        Matcher endQuoteMatcher = Pattern.compile("\"").matcher(elementJson);
                        if (endQuoteMatcher.find(keyMatcher.end() + 1)) {
                            String value = elementJson.substring(keyMatcher.end() + 1, endQuoteMatcher.end() - 1);
                            element.setValue(value);
                        }
                    }
                    default -> {
                        Matcher numberMatcher = Pattern.compile("([\\d.]+|true|false)").matcher(elementJson);
                        if (numberMatcher.find(keyMatcher.end())) {
                            element.setValue(numberMatcher.group());
                        }
                    }
                }
                elements.add(element);
            }
        }

        return elements;
    }

    private static void parseJsonArray(Element element, List<String> children) {
        children.forEach(child -> {
            if (!"".equals(child)) {
                switch (child.charAt(0)) {
                    case '{' -> {
                        if (!child.matches("\\{\\s*}")) {
                            element.getChildren().addAll(parseJson("\"element\" : " + child));
                        } else {
                            element.getChildren().add(new Element("element", ""));
                        }
                    }
                    case '[' -> {
                        Element subArray = new Element("element", "");
                        element.getChildren().add(subArray);
                        parseJsonArray(subArray, splitJsonElements(child));
                    }
                    default -> {
                        if ("null".equals(child)) {
                            element.getChildren().add(new Element("element"));
                        } else if ("\"\"".equals(child)) {
                            element.getChildren().add(new Element("element", ""));
                        } else {
                            element.getChildren().add(new Element("element", child));
                        }
                    }
                }
            }
        });
    }

    private static List<String> splitJsonElements(String json) {
        if ((json.startsWith("{") && json.endsWith("}")) || (json.startsWith("[") && json.endsWith("]"))) {
            json = json.substring(1, json.length() - 1).strip();
        }

        List<String> parts = new ArrayList<>(Arrays.stream(json.split(",")).toList());
        List<String> elements = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            long openingBrackets = Arrays.stream(parts.get(i).split("")).filter("{"::equals).count();
            long closingBrackets = Arrays.stream(parts.get(i).split("")).filter("}"::equals).count();
            long openingArray = Arrays.stream(parts.get(i).split("")).filter("["::equals).count();
            long closingArray = Arrays.stream(parts.get(i).split("")).filter("]"::equals).count();

            if (openingBrackets > closingBrackets || openingArray > closingArray) {
                parts.set(i + 1, parts.get(i) + "," + parts.get(i + 1));
            } else {
                elements.add(parts.get(i));
            }
        }

        return elements.stream().map(String::strip).collect(Collectors.toList());
    }

    private static String convertToJson(List<Element> elements) {
        StringBuilder builder = new StringBuilder("{\n");
        elements.forEach(element -> {
            builder.append(String.format("\t\"%s\": ", element.getKey()));

            String value = (element.getValue() != null) ? String.format("\"%s\"", element.getValue()) : "null";
            if (!element.getAttributes().isEmpty()) {
                builder.append("{\n");
                element.getAttributes().forEach((k, v) ->
                        builder.append(String.format("\t\t\"@%s\": \"%s\",%n", k, v))
                );
                builder.append(String.format("\t\t\"#%s\": ", element.getKey()));

                if (!element.getChildren().isEmpty()) {
                    List<Element> children = element.getChildren();
                    String childrenString = convertToJson(children);
                    if (children.size() > 1 && (children.get(0).getKey().equals(children.get(1).getKey()))) {
                        childrenString = childrenString.replaceFirst("\\{", "[")
                                .replaceAll("\"" + children.get(0).getKey() + "\"\\s*:\\s*", "")
                                .replaceAll("}$", "]");
                    }
                    childrenString = childrenString.replace("\n", "\n\t\t");
                    builder.append(childrenString).append("\n");
                } else {
                    builder.append(String.format("%s%n", value));
                }

                builder.append("\t}");
            } else if (!element.getChildren().isEmpty()) {
                List<Element> children = element.getChildren();
                String childrenString = convertToJson(children);
                if (children.size() > 1 && (children.get(0).getKey().equals(children.get(1).getKey()))) {
                    childrenString = childrenString.replaceFirst("\\{", "[")
                            .replaceAll("\"" + children.get(0).getKey() + "\"\\s*:\\s*", "")
                            .replaceAll("}$", "]");
                }
                childrenString = childrenString.replace("\n", "\n\t");
                builder.append(childrenString);
            } else {
                builder.append(value);
            }
            builder.append(",\n");
        });
        builder.replace(builder.length() - 2, builder.length() - 1, "");
        builder.append("}");
        return builder.toString();
    }
}
