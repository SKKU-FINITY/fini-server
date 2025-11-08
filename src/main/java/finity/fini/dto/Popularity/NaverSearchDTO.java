package finity.fini.dto.Popularity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NaverSearchDTO {
    private Integer total; // 총 검색 결과 수 (랭킹용)
    private List<Item> items; // 뉴스 기사 목록 (RAG용)

    @Getter
    @NoArgsConstructor
    public static class Item {
        private String description; // 뉴스 기사 요약 (스니펫)
    }
}
