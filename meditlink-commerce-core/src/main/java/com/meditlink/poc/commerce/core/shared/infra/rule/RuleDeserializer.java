package com.meditlink.poc.commerce.core.shared.infra.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSONB(JsonNode 또는 Map) → Rule 변환.
 * field 키가 있으면 LeafRule, type 키가 있으면 CompositeRule로 판별.
 */
public final class RuleDeserializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RuleDeserializer() {
    }

    /**
     * Map 형태의 JSONB 데이터를 Rule로 변환한다.
     */
    public static Rule deserialize(Object jsonData) {
        if (jsonData == null) return null;
        JsonNode node = MAPPER.valueToTree(jsonData);
        return deserialize(node);
    }

    /**
     * JsonNode를 Rule로 변환한다.
     */
    public static Rule deserialize(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.has("field")) return parseLeafRule(node);
        if (node.has("type")) return parseCompositeRule(node);
        throw new IllegalArgumentException("알 수 없는 Rule 구조: " + node);
    }

    private static LeafRule parseLeafRule(JsonNode node) {
        String field = node.get("field").asText();
        String op = node.get("op").asText();
        Object value = convertValue(node.get("value"));
        return new LeafRule(field, op, value);
    }

    private static CompositeRule parseCompositeRule(JsonNode node) {
        String type = node.get("type").asText();
        ArrayNode rulesNode = (ArrayNode) node.get("rules");
        List<Rule> rules = new ArrayList<>();
        if (rulesNode != null) {
            for (JsonNode ruleNode : rulesNode) {
                rules.add(deserialize(ruleNode));
            }
        }
        return new CompositeRule(type, rules);
    }

    /**
     * Rule → Map (JSONB 저장용).
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> serialize(Rule rule) {
        if (rule == null) return null;
        return MAPPER.convertValue(serializeToRaw(rule), Map.class);
    }

    private static Object serializeToRaw(Rule rule) {
        if (rule instanceof LeafRule leaf) {
            var map = new LinkedHashMap<String, Object>();
            map.put("field", leaf.field());
            map.put("op", leaf.op());
            map.put("value", leaf.value());
            return map;
        } else if (rule instanceof CompositeRule composite) {
            var map = new LinkedHashMap<String, Object>();
            map.put("type", composite.type());
            map.put("rules", composite.rules().stream().map(RuleDeserializer::serializeToRaw).toList());
            return map;
        }
        throw new IllegalArgumentException("알 수 없는 Rule 타입: " + rule.getClass());
    }

    private static Object convertValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble() || node.isFloat()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : node) {
                list.add(convertValue(element));
            }
            return list;
        }
        return node.toString();
    }
}
