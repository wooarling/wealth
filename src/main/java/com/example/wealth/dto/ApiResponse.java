package com.example.wealth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ApiResponse {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private List<Item> items;
        private String totalCount;
        private String numOfRows;
        private String pageNo;
    }

    @Data
    public static class Item {
        @JsonProperty("mrhst_nm")
        private String mrhstNm;

        private String rdnmadr;
        private String latitude;
        private String longitude;

        @JsonProperty("ctprvn_nm")
        private String ctprvnNm;

        @JsonProperty("signgu_nm")
        private String signguNm;
    }
}
