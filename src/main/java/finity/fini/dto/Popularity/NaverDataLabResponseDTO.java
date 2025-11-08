package finity.fini.dto.Popularity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class NaverDataLabResponseDTO {
    private List<Result> results;

    @Getter
    @NoArgsConstructor
    public static class Result {
        private String title;
        private List<Data> data;
    }

    @Getter
    @NoArgsConstructor
    public static class Data {
        private Double ratio; // 상대적 검색량
    }
}