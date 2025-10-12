package finity.fini.service.Bank;

public interface BankService {
    /**
     * 금융감독원 API를 호출하여 전체 은행 목록을 DB에 동기화합니다.
     * 이미 존재하는 은행은 건너뜁니다.
     */
    void syncBanks();
}
