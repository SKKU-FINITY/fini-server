package finity.fini.apiPayload.code.status;

import finity.fini.apiPayload.code.BaseErrorCode;
import finity.fini.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 회원 관련 에러
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH4001", "이미 사용 중인 아이디입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4011", "아이디 또는 비밀번호가 잘못되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4041", "해당 사용자를 찾을 수 없습니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT4041", "해당 금융상품을 찾을 수 없습니다."),
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT4042", "해당 상품 옵션을 찾을 수 없습니다."),
    PRODUCT_SYNC_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT5001", "금융상품 정보 동기화에 실패했습니다. 관리자에게 문의 바랍니다."),

    NAVER_API_ERROR(HttpStatus.BAD_GATEWAY, "POPULARITY5001", "네이버 검색 API 호출 중 오류가 발생했습니다."),
    GEMINI_API_ERROR(HttpStatus.BAD_GATEWAY, "POPULARITY5002", "Gemini AI API 호출 중 오류가 발생했습니다."),
    RANKING_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POPULARITY5003", "인기 순위 업데이트 배치 작업 중 오류가 발생했습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
