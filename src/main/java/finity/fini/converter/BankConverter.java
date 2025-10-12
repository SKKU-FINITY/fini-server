package finity.fini.converter;

import finity.fini.domain.Bank;
import finity.fini.dto.Bank.BankResponseDTO;

public class BankConverter {

    /**
     * FSS API 응답(BankDto)을 Bank 엔티티로 변환합니다.
     * @param bankDto FSS API의 은행 기본 정보 DTO
     * @return Bank 엔티티
     */
    public static Bank toBank(BankResponseDTO.BankDto bankDto) {
        return Bank.builder()
                .finCoNo(bankDto.getFinCoNo())
                .korCoNm(bankDto.getKorCoNm())
                .dclsMonth(bankDto.getDclsMonth())
                .hompUrl(bankDto.getHompUrl())
                .calTel(bankDto.getCalTel())
                .dclsChrgMan(bankDto.getDclsChrgMan())
                .build();
    }
}
