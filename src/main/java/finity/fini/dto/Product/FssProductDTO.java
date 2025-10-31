package finity.fini.dto.Product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor

// 금융감독원 API 응답을 파싱하기 위한 DTO
public class FssProductDTO {

    @JsonProperty("result") // JSON의 "result" 키와 매핑
    private Result result;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("baseList")
        private List<BaseInfo> baseList;

        @JsonProperty("optionList")
        private List<Option> optionList;
    }

    @Getter
    @NoArgsConstructor
    public static class BaseInfo {
        @JsonProperty("fin_co_no")
        private String finCoNo;
        @JsonProperty("kor_co_nm")
        private String korCoNm;
        @JsonProperty("dcls_month")
        private String dclsMonth;
        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd;
        @JsonProperty("fin_prdt_nm")
        private String finPrdtNm;
        @JsonProperty("join_way")
        private String joinWay;
        @JsonProperty("mtrt_int")
        private String mtrtInt;
        @JsonProperty("spcl_cnd")
        private String spclCnd;
        @JsonProperty("join_deny")
        private String joinDeny;
        @JsonProperty("join_member")
        private String joinMember;
        @JsonProperty("etc_note")
        private String etcNote;
        @JsonProperty("max_limit")
        private Long maxLimit;
    }

    @Getter
    @NoArgsConstructor
    public static class Option {
        @JsonProperty("fin_co_no")
        private String finCoNo;
        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd;
        @JsonProperty("intr_rate_type")
        private String intrRateType;
        @JsonProperty("intr_rate_type_nm")
        private String intrRateTypeNm;
        @JsonProperty("rsrv_type") // 적금에만 존재
        private String rsrvType;
        @JsonProperty("rsrv_type_nm") // 적금에만 존재
        private String rsrvTypeNm;
        @JsonProperty("save_trm")
        private String saveTrm; // API에서 문자열로 오므로 파싱 필요
        @JsonProperty("intr_rate")
        private Double intrRate;
        @JsonProperty("intr_rate2")
        private Double intrRate2;
    }
}