package converter;

import java.util.*;

public class Element {
    private String key;
    private String value = null;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<Element> children = new ArrayList<>();

    public Element(String key) {
        this.key = key;
    }

    public Element(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<Element> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Element: \n");
        s.append(String.format("path = %s%n", this.key));

        if (this.value != null) {
            s.append(String.format("value = \"%s\"%n", this.value));
        } else if (this.getChildren().isEmpty()) {
            s.append("value = null\n");
        }

        if (!this.attributes.isEmpty()) {
            s.append("attributes:\n");
            StringBuilder a = new StringBuilder();
            attributes.forEach((key, val) -> a.append(String.format(key + " = \"" + val + "\"%n")));
            s.append(a);
        }
        s.append("\n");

        if (!this.children.isEmpty()) {
            StringBuilder a = new StringBuilder();
            children.forEach(c -> a.append(c.toString()));
            s.append(
                    a.toString().replaceAll("path = ", "path = " + this.getKey() + ", ")
            );
        }
        return s.toString();
    }
}
