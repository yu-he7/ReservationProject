package heej.net.domain.holiday.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class PublicHolidayApiResponse {

    @JsonProperty("response")
    private Response response;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Body {
        @JsonProperty("items")
        @JsonDeserialize(using = ItemsFieldDeserializer.class)
        private List<Item> items = new ArrayList<>();  // items를 직접 List로 변경

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }


    @Getter
    @NoArgsConstructor
    @ToString
    public static class Item {
        @JsonProperty("dateKind")
        private String dateKind;

        @JsonProperty("dateName")
        private String dateName;

        @JsonProperty("isHoliday")
        private String isHoliday;

        @JsonProperty("locdate")
        private Integer locdate;

        @JsonProperty("seq")
        private Integer seq;
    }

    // 커스텀 역직렬화기: items 필드가 빈 문자열, 단일 객체, 배열 등 다양한 형태로 올 수 있음
    public static class ItemsFieldDeserializer extends JsonDeserializer<List<Item>> {
        @Override
        public List<Item> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<Item> result = new ArrayList<>();
            JsonNode node = p.getCodec().readTree(p);

            // 1. 빈 문자열이거나 null인 경우 (공휴일 없음)
            if (node.isTextual() || node.isNull()) {
                return result; // 빈 리스트 반환
            }

            // 2. 객체인 경우: {"item": {...}} 또는 {"item": [...]}
            if (node.isObject()) {
                JsonNode itemNode = node.get("item");
                if (itemNode != null && !itemNode.isNull()) {
                    if (itemNode.isArray()) {
                        // 2-1. item이 배열인 경우: {"item": [{...}, {...}]}
                        for (JsonNode element : itemNode) {
                            Item item = p.getCodec().treeToValue(element, Item.class);
                            result.add(item);
                        }
                    } else if (itemNode.isObject()) {
                        // 2-2. item이 단일 객체인 경우: {"item": {...}}
                        Item item = p.getCodec().treeToValue(itemNode, Item.class);
                        result.add(item);
                    }
                }
            }

            return result;
        }
    }
}

