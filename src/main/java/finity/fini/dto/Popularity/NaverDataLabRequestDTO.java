package finity.fini.dto.Popularity;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class NaverDataLabRequestDTO {
    private String startDate; // 예: "2023-01-01"
    private String endDate;   // 예: "2023-12-31"
    private String timeUnit;  // "date"
    private List<KeywordGroup> keywordGroups;

    @Getter
    @Builder
    public static class KeywordGroup {
        private String groupName;
        private List<String> keywords;
    }
}