package uy.gub.agesic.pdi.common.xml;


import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class XPathContentHandler extends DefaultHandler {

    private String xPath = "/";
    private XMLReader reader;
    private XPathContentHandler parent;
    private StringBuilder characters = new StringBuilder();
    private Map<String, Integer> elementNameCount = new HashMap<>();
    private XmlNode root;

    public XPathContentHandler(XMLReader reader) {
        this.reader = reader;
    }

    private XPathContentHandler(String xPath, XMLReader reader, XPathContentHandler parent, XmlNode root) {
        this(reader);
        this.xPath = xPath;
        this.parent = parent;
        this.root = root;
    }

    public XmlNode getRoot() {
        return root;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Integer count = elementNameCount.merge(qName, 1, Integer::sum);
        String childXPathWithIndex = String.format("%s/%s[%d]", xPath, qName, count);
        String currentXPath = String.format("%s/%s", xPath, qName);

        String finalCurrentXPath = currentXPath;
        List<XmlNode.NodeAttr> attrs = IntStream.range(0, attributes.getLength())
                .mapToObj(i -> getNodeAttr(attributes, finalCurrentXPath, i))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        if (!attrs.isEmpty()) {
            currentXPath = String.format("%s/%s[%s]", xPath, qName, buildNodeAttrsXpathExp(attrs));
        }

        XmlNode xmlNode = new XmlNode(qName, currentXPath, root, attrs);
        if (root == null) {
            root = xmlNode;
        } else {
            root.getChildren().add(xmlNode);
        }
        XPathContentHandler child = new XPathContentHandler(currentXPath, reader, this, xmlNode);
        reader.setContentHandler(child);
    }

    private String buildNodeAttrsXpathExp(List<XmlNode.NodeAttr> attrs) {
        return attrs.stream()
                .map(a -> String.format("@%s='%s'", a.getName(), a.getValue()))
                .collect(Collectors.joining(" and "));
    }

    private XmlNode.NodeAttr getNodeAttr(Attributes attributes, String currentXPath, int i) {

        if (!attributes.getQName(i).contains(":")) {
            String path = currentXPath + "/@" + attributes.getQName(i);
            log.debug(path);
            return XmlNode.NodeAttr.builder()
                    .name(attributes.getQName(i))
                    .value(attributes.getValue(i))
                    .xPath(path)
                    .build();
        }
        log.debug("found namespace-> {}:{}", attributes.getQName(i), attributes.getValue(i));
        return null;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String value = characters.toString().trim();
        if (value.length() > 0) {
            root.setValue(characters.toString());
            log.debug("{} = '{}'", xPath, characters.toString());
        }

        reader.setContentHandler(parent);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        characters.append(ch, start, length);
    }

}
