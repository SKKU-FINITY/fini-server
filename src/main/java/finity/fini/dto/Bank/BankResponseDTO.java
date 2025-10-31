package finity.fini.dto.Bank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import finity.fini.dto.Product.FssProductDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BankResponseDTO {

    @JsonProperty("result") // JSON의 "result" 키와 매핑
    private Result result;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("baseList")
        private List<BankDto> baseList;

        @JsonProperty("total_count")
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    public static class BankDto {

         @JsonProperty("dcls_month")
         private String dclsMonth;

         @JsonProperty("fin_co_no")
         private String finCoNo;

         @JsonProperty("kor_co_nm")
         private String korCoNm;

         @JsonProperty("dcls_chrg_man")
         private String dclsChrgMan;

         @JsonProperty("homp_url")
         private String hompUrl;

         @JsonProperty("cal_tel")
         private String calTel;

    }
}
