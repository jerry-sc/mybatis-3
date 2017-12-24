/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 解析SQL语句之前，首先解析其中的include节点，该过程会将include节点替换成sql节点的片段，并将其遍历替换为实际值
 * @author Frank D. Martinez [mnesarco]
 */
public class XMLIncludeTransformer {

    private final Configuration configuration;
    private final MapperBuilderAssistant builderAssistant;

    public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    public void applyIncludes(Node source) {
        Properties variablesContext = new Properties();
        Properties configurationVariables = configuration.getVariables();
        if (configurationVariables != null) {
            variablesContext.putAll(configurationVariables);
        }
        applyIncludes(source, variablesContext, false);
    }

    /**
     * Recursively apply includes through all SQL fragments.
     * @param source Include node in DOM tree
     * @param variablesContext Current context for static variables with values
     */
    private void applyIncludes(Node source, final Properties variablesContext, boolean included) {
        if (source.getNodeName().equals("include")) {
            // 找到refId指向的sql片段节点
            Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);
            // 解析<include>节点下的<property>节点，将得到的键值对放入variablesContext，用于占位符替换
            Properties toIncludeContext = getVariablesContext(source, variablesContext);
            // 递归处理 <include> 节点（可能是sql节点）,因为sql节点仍然可以使用include引用其他节点
            applyIncludes(toInclude, toIncludeContext, true);
            if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                toInclude = source.getOwnerDocument().importNode(toInclude, true);
            }
            // DOM 操作
            // 将include节点替换成sql节点
            source.getParentNode().replaceChild(toInclude, source);
            while (toInclude.hasChildNodes()) { // 将sql节点的子节点添加到sql节点之前
                toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
            }
            // 删除sql节点
            toInclude.getParentNode().removeChild(toInclude);
        } else if (source.getNodeType() == Node.ELEMENT_NODE) {
            if (included && !variablesContext.isEmpty()) {
                // replace variables in attribute values
                NamedNodeMap attributes = source.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    attr.setNodeValue(PropertyParser.parse(attr.getNodeValue(), variablesContext));
                }
            }
            NodeList children = source.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {    // 处理所有子节点，可能有多个include节点
                applyIncludes(children.item(i), variablesContext, included);
            }
        } else if (included && source.getNodeType() == Node.TEXT_NODE
                && !variablesContext.isEmpty()) {
            // replace variables in text node
            // 将sql节点中的占位符进行替换
            source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
        }
    }

    /**
     * 找到refId对应的SQL片段
     * @param refid
     * @param variables
     * @return
     */
    private Node findSqlFragment(String refid, Properties variables) {
        refid = PropertyParser.parse(refid, variables);
        refid = builderAssistant.applyCurrentNamespace(refid, true);
        try {
            XNode nodeToInclude = configuration.getSqlFragments().get(refid);
            // 返回该节点的深拷贝
            return nodeToInclude.getNode().cloneNode(true);
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
        }
    }

    private String getStringAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    /**
     * Read placeholders and their values from include node definition.
     * @param node Include node instance
     * @param inheritedVariablesContext Current context used for replace variables in new variables values
     * @return variables context from include instance (no inherited values)
     */
    private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {
        Map<String, String> declaredProperties = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = getStringAttribute(n, "name");
                // Replace variables inside
                String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);
                if (declaredProperties == null) {
                    declaredProperties = new HashMap<String, String>();
                }
                if (declaredProperties.put(name, value) != null) {
                    throw new BuilderException("Variable " + name + " defined twice in the same include definition");
                }
            }
        }
        if (declaredProperties == null) {
            return inheritedVariablesContext;
        } else {
            Properties newProperties = new Properties();
            newProperties.putAll(inheritedVariablesContext);
            newProperties.putAll(declaredProperties);
            return newProperties;
        }
    }
}
