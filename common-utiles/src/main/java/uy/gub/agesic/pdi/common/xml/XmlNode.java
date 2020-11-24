package uy.gub.agesic.pdi.common.xml;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ToString(exclude = {"parent"})
@NoArgsConstructor
@AllArgsConstructor
@Data
public class XmlNode {

    private String qName;
    private String xPath;
    private String value;
    private Map<ExpressionType, ExpressionResult> expressions = new EnumMap<>(ExpressionType.class);

    @JsonBackReference
    private XmlNode parent;
    private List<NodeAttr> attrs;
    @Builder.Default
    private List<XmlNode> children = new LinkedList<>();

    public XmlNode(String qName, String xPath, XmlNode parent, List<NodeAttr> attrs) {
        this.qName = qName;
        this.xPath = xPath;
        this.attrs = attrs;
        this.parent = parent;
    }

    public List<String> xPathTraversal() {
        List<String> result = new LinkedList<>();
        visit(this, result);
        return result;
    }

    public void addExpression(ExpressionType type, ExpressionResult expression) {
        expressions.put(type, expression);
    }

    private void visit(XmlNode node, List<String> result) {
        result.add(node.getXPath());
        node.getAttrs().forEach(a -> result.addAll(a.getExpressions().values().stream().map(ExpressionResult::getExpression).collect(Collectors.toList())));
        node.getChildren().forEach(n -> visit(n, result));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NodeAttr {
        private String name;
        private String value;
        private String xPath;
        @Builder.Default
        private Map<ExpressionType, ExpressionResult> expressions = new EnumMap<>(ExpressionType.class);


        public void addExpression(ExpressionType type, ExpressionResult expression) {
            expressions.put(type, expression);
        }
    }

    public enum ExpressionType {
        VALUE, COUNT
    }

    @Data
    @Builder
    public static class ExpressionResult {
        private String expression;
        private String value;
    }
}
